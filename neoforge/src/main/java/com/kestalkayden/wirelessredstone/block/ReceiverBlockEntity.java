package com.kestalkayden.wirelessredstone.block;

import java.util.UUID;

import com.kestalkayden.wirelessredstone.component.ReceiverComponents;
import com.kestalkayden.wirelessredstone.component.ReceiverConfig;
import com.kestalkayden.wirelessredstone.menu.ReceiverMenu;
import com.kestalkayden.wirelessredstone.pairing.OwnerKey;
import com.kestalkayden.wirelessredstone.pairing.PairKey;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;
import com.kestalkayden.wirelessredstone.pairing.WirelessNode;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class ReceiverBlockEntity extends BlockEntity implements WirelessNode.Receiver, MenuProvider {

    private int frequency = 0;
    private int channel = 1;
    private UUID ownerUuid = null;
    private boolean privateMode = false;
    private boolean editLock = false;

    private int currentStrength = 0;

    public ReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(ReceiverBlockEntities.RECEIVER_BE, pos, state);
    }

    @Override
    public PairKey pairKey() {
        OwnerKey owner = privateMode && ownerUuid != null
            ? new OwnerKey.Owned(ownerUuid)
            : OwnerKey.PUBLIC;
        return new PairKey(frequency, channel, owner);
    }

    @Override
    public BlockPos pos() {
        return getBlockPos();
    }

    public int currentStrength() { return currentStrength; }

    public int frequency()     { return frequency; }
    public int channel()       { return channel; }
    public UUID ownerUuid()    { return ownerUuid; }
    public boolean privateMode() { return privateMode; }
    public boolean editLock() { return editLock; }

    public void setOwner(UUID owner) {
        if (java.util.Objects.equals(this.ownerUuid, owner)) return;
        PairKey oldKey = pairKey();
        this.ownerUuid = owner;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
    }

    public void setFrequency(int frequency) {
        int clamped = Math.max(0, Math.min(65535, frequency));
        if (this.frequency == clamped) return;
        PairKey oldKey = pairKey();
        this.frequency = clamped;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
    }

    public void setChannel(int channel) {
        int clamped = Math.max(1, Math.min(128, channel));
        if (this.channel == clamped) return;
        PairKey oldKey = pairKey();
        this.channel = clamped;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
    }

    public void setPrivateMode(boolean privateMode) {
        if (this.privateMode == privateMode) return;
        PairKey oldKey = pairKey();
        this.privateMode = privateMode;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
    }

    public void setEditLock(boolean editLock) {
        if (this.editLock == editLock) return;
        this.editLock = editLock;
        setChanged();
    }

    @Override
    public void onExternalStrengthChanged(int newStrength) {
        if (this.currentStrength == newStrength) return;
        this.currentStrength = newStrength;
        if (level != null && !level.isClientSide()) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
        updatePoweredState();
    }

    private void updatePoweredState() {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(ReceiverBlock.POWERED)) return;
        boolean nowPowered = currentStrength > 0;
        if (state.getValue(ReceiverBlock.POWERED) != nowPowered) {
            level.setBlock(worldPosition,
                state.setValue(ReceiverBlock.POWERED, nowPowered),
                Block.UPDATE_CLIENTS);
        }
    }

    private void rebindIfChanged(PairKey oldKey, PairKey newKey) {
        if (oldKey.equals(newKey)) return;
        if (level instanceof ServerLevel sl) {
            PairingRegistry registry = PairingRegistry.get(sl);
            registry.removeReceiver(oldKey, this);
            registry.addReceiver(newKey, this);
            onExternalStrengthChanged(registry.currentStrength(newKey));
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel sl)) return;
        PairingRegistry registry = PairingRegistry.get(sl);
        // Register synchronously so other BEs loading in the same tick see us in the bucket.
        registry.addReceiver(pairKey(), this);
        // Sync BE.currentStrength synchronously from the registry — pure BE field access, no
        // chunk queries. Receiver reports correct redstone signal even if chunk is non-ticking.
        this.currentStrength = registry.currentStrength(pairKey());
        // Defer chunk-touchy work (updateNeighborsAt + setBlock) to end of next server tick.
        com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge.PENDING_TICK_INITS.add(this::tickInit);
    }

    /** Runs from WirelessRedstoneNeoForge.PENDING_TICK_INITS drain on ServerTickEvent.Post. */
    public void tickInit() {
        if (isRemoved() || !(level instanceof ServerLevel sl)) return;
        sl.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        updatePoweredState();
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel sl) {
            PairingRegistry.get(sl).removeReceiver(pairKey(), this);
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Frequency", frequency);
        output.putInt("Channel", channel);
        output.putBoolean("PrivateMode", privateMode);
        output.putBoolean("EditLock", editLock);
        if (ownerUuid != null) {
            output.putLong("OwnerMost", ownerUuid.getMostSignificantBits());
            output.putLong("OwnerLeast", ownerUuid.getLeastSignificantBits());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.frequency = input.getIntOr("Frequency", 0);
        this.channel = input.getIntOr("Channel", 1);
        this.privateMode = input.getBooleanOr("PrivateMode", false);
        this.editLock = input.getBooleanOr("EditLock", false);
        long most = input.getLongOr("OwnerMost", 0L);
        long least = input.getLongOr("OwnerLeast", 0L);
        this.ownerUuid = (most == 0L && least == 0L) ? null : new UUID(most, least);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(ReceiverComponents.RECEIVER_CONFIG(),
            new ReceiverConfig(frequency, channel, privateMode, editLock));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        ReceiverConfig config = componentGetter.getOrDefault(
            ReceiverComponents.RECEIVER_CONFIG(), ReceiverConfig.DEFAULT);
        this.frequency = config.frequency();
        this.channel = config.channel();
        this.privateMode = config.privateMode();
        this.editLock = config.editLock();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        if (level == null) return null;
        return new ReceiverMenu(containerId, playerInv, this,
            ContainerLevelAccess.create(level, worldPosition));
    }
}
