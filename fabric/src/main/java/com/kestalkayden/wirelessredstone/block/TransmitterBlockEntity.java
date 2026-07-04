package com.kestalkayden.wirelessredstone.block;

import java.util.UUID;

import com.kestalkayden.wirelessredstone.component.ManualMode;
import com.kestalkayden.wirelessredstone.component.SourceMode;
import com.kestalkayden.wirelessredstone.component.TransmitterComponents;
import com.kestalkayden.wirelessredstone.component.TransmitterConfig;
import com.kestalkayden.wirelessredstone.menu.TransmitterMenu;
import com.kestalkayden.wirelessredstone.pairing.OwnerKey;
import com.kestalkayden.wirelessredstone.pairing.PairKey;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;
import com.kestalkayden.wirelessredstone.pairing.WirelessNode;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
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

public class TransmitterBlockEntity extends BlockEntity implements WirelessNode.Transmitter, MenuProvider {

    private int frequency = 0;
    private int channel = 1;
    private UUID ownerUuid = null;
    private boolean privateMode = false;
    private boolean editLock = false;
    private SourceMode sourceMode = SourceMode.ECHO;
    private int fixedStrength = 15;
    private ManualMode manualMode = ManualMode.TOGGLE;

    private int lastInputStrength = 0;

    public TransmitterBlockEntity(BlockPos pos, BlockState state) {
        super(TransmitterBlockEntities.TRANSMITTER_BE, pos, state);
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

    @Override
    public int currentStrength() {
        return switch (manualMode) {
            case ALWAYS_OFF -> 0;
            case ALWAYS_ON -> 15;
            case TOGGLE -> switch (sourceMode) {
                case ECHO -> lastInputStrength;
                case FIXED -> lastInputStrength > 0 ? fixedStrength : 0;
            };
        };
    }

    public int frequency() { return frequency; }
    public int channel() { return channel; }
    public UUID ownerUuid() { return ownerUuid; }
    public boolean privateMode() { return privateMode; }
    public boolean editLock() { return editLock; }
    public SourceMode sourceMode() { return sourceMode; }
    public int fixedStrength() { return fixedStrength; }
    public ManualMode manualMode() { return manualMode; }

    public void setOwner(UUID owner) {
        if (java.util.Objects.equals(this.ownerUuid, owner)) return;
        PairKey oldKey = pairKey();
        this.ownerUuid = owner;
        PairKey newKey = pairKey();
        rebindIfChanged(oldKey, newKey);
        setChanged();
        syncClient();
    }

    public void setFrequency(int frequency) {
        int clamped = Math.max(0, Math.min(65535, frequency));
        if (this.frequency == clamped) return;
        PairKey oldKey = pairKey();
        this.frequency = clamped;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
        syncClient();
    }

    public void setChannel(int channel) {
        int clamped = Math.max(1, Math.min(128, channel));
        if (this.channel == clamped) return;
        PairKey oldKey = pairKey();
        this.channel = clamped;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
        syncClient();
    }

    public void setPrivateMode(boolean privateMode) {
        if (this.privateMode == privateMode) return;
        PairKey oldKey = pairKey();
        this.privateMode = privateMode;
        rebindIfChanged(oldKey, pairKey());
        setChanged();
        syncClient();
    }

    public void setEditLock(boolean editLock) {
        if (this.editLock == editLock) return;
        this.editLock = editLock;
        setChanged();
        syncClient();
    }

    public void setSourceMode(SourceMode mode) {
        if (this.sourceMode == mode) return;
        int before = currentStrength();
        this.sourceMode = mode;
        if (currentStrength() != before) notifyCurrentKey();
        setChanged();
    }

    public void setFixedStrength(int strength) {
        int clamped = Math.max(1, Math.min(15, strength));
        if (this.fixedStrength == clamped) return;
        int before = currentStrength();
        this.fixedStrength = clamped;
        if (currentStrength() != before) notifyCurrentKey();
        setChanged();
    }

    /** Bumps manualMode to the next state and broadcasts the change. Returns the new mode. */
    public ManualMode cycleManualMode() {
        int before = currentStrength();
        this.manualMode = manualMode.next();
        if (currentStrength() != before) notifyCurrentKey();
        setChanged();
        syncClient();
        return this.manualMode;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    @Nullable
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        if (level == null) return null;
        return new TransmitterMenu(containerId, playerInv, this,
            ContainerLevelAccess.create(level, worldPosition));
    }

    public void onRedstoneInputChanged(int newInput) {
        if (this.lastInputStrength == newInput) return;
        int before = currentStrength();
        this.lastInputStrength = newInput;
        // Only re-broadcast when the emitted value actually changes. FIXED / ALWAYS_ON /
        // ALWAYS_OFF absorb input changes (e.g. 5->10 stays fixedStrength) with no change
        // to what receivers see, so skip the whole-network notify in that case.
        if (currentStrength() != before) notifyCurrentKey();
    }

    private void rebindIfChanged(PairKey oldKey, PairKey newKey) {
        if (oldKey.equals(newKey)) {
            notifyCurrentKey();
            return;
        }
        if (level instanceof ServerLevel sl) {
            PairingRegistry registry = PairingRegistry.get(sl);
            registry.removeTransmitter(oldKey, this);
            registry.addTransmitter(newKey, this);
            registry.notifyTransmitterChanged(oldKey);
            registry.notifyTransmitterChanged(newKey);
        }
    }

    private void notifyCurrentKey() {
        if (level instanceof ServerLevel sl) {
            PairingRegistry.get(sl).notifyTransmitterChanged(pairKey());
        }
        updatePoweredState();
    }

    /** Broadcasts BE NBT to clients so they see config changes (channel, freq,
     *  lock, private, manual mode). Called from setters after field updates. */
    private void syncClient() {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void updatePoweredState() {
        if (level == null || level.isClientSide()) return;
        BlockState state = getBlockState();
        if (!state.hasProperty(TransmitterBlock.POWERED)) return;
        boolean nowPowered = currentStrength() > 0;
        if (state.getValue(TransmitterBlock.POWERED) != nowPowered) {
            level.setBlock(worldPosition,
                state.setValue(TransmitterBlock.POWERED, nowPowered),
                Block.UPDATE_CLIENTS);
        }
    }

    /** Called from the Fabric ServerBlockEntityEvents.BLOCK_ENTITY_LOAD hook in the entrypoint. */
    public void registerInRegistry() {
        if (!(level instanceof ServerLevel sl)) return;
        // Register synchronously so other BEs loading in the same tick see us in the bucket.
        PairingRegistry.get(sl).addTransmitter(pairKey(), this);
        // Defer chunk-touchy work to end of next server tick via the shared queue.
        // Doing it here would deadlock the chunk loader (we're called during chunk load).
        // server.execute runs inline from server thread; scheduleTick only fires for TICKING
        // chunks; TickTask(currentTick+1) hangs shutdown. ServerTickEvents.END_SERVER_TICK
        // is the right hook — fires reliably each tick, doesn't block shutdown.
        com.kestalkayden.wirelessredstone.WirelessRedstoneFabric.PENDING_TICK_INITS.add(this::tickInit);
    }

    /** Runs from WirelessRedstoneFabric.PENDING_TICK_INITS drain on END_SERVER_TICK. */
    public void tickInit() {
        if (isRemoved() || !(level instanceof ServerLevel sl)) return;
        int signal = sl.getBestNeighborSignal(worldPosition);
        if (signal != lastInputStrength) {
            onRedstoneInputChanged(signal);
        } else {
            // Signal unchanged but receivers loaded after us need our current contribution.
            PairingRegistry.get(sl).notifyTransmitterChanged(pairKey());
            updatePoweredState();
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel sl) {
            // getIfPresent (not get) so teardown after level-unload doesn't resurrect the registry.
            PairingRegistry registry = PairingRegistry.getIfPresent(sl);
            if (registry != null) {
                PairKey key = pairKey();
                registry.removeTransmitter(key, this);
                // Skip neighbor notify during shutdown / chunk-unload teardown — calling
                // updateNeighborsAt during chunk clearing hangs the shutdown. Receivers
                // re-sync from the registry on next load via currentStrengthFor.
                if (sl.getServer() != null && sl.getServer().isRunning()) {
                    registry.notifyTransmitterChanged(key);
                }
            }
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
        output.putString("SourceMode", sourceMode.getSerializedName());
        output.putInt("FixedStrength", fixedStrength);
        output.putString("ManualMode", manualMode.getSerializedName());
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
        String modeName = input.getStringOr("SourceMode", SourceMode.ECHO.getSerializedName());
        this.sourceMode = modeName.equals(SourceMode.FIXED.getSerializedName()) ? SourceMode.FIXED : SourceMode.ECHO;
        this.fixedStrength = input.getIntOr("FixedStrength", 15);
        this.manualMode = ManualMode.fromName(input.getStringOr("ManualMode", ManualMode.TOGGLE.getSerializedName()));
        long most = input.getLongOr("OwnerMost", 0L);
        long least = input.getLongOr("OwnerLeast", 0L);
        this.ownerUuid = (most == 0L && least == 0L) ? null : new UUID(most, least);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(TransmitterComponents.TRANSMITTER_CONFIG(),
            new TransmitterConfig(frequency, channel, privateMode, editLock, sourceMode, fixedStrength, manualMode));
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) {
        super.applyImplicitComponents(componentGetter);
        TransmitterConfig config = componentGetter.getOrDefault(
            TransmitterComponents.TRANSMITTER_CONFIG(), TransmitterConfig.DEFAULT);
        // Components are applied AFTER the BE has already registered under its default key
        // (on load/placement). Capture that key so we can move the registry entry to the
        // configured key — otherwise a configured block placed from an item (break+replace,
        // hopper, pick-block) stays in the (0,1,PUBLIC) bucket while pairKey() reports the
        // configured key, silently transmitting/receiving nothing.
        PairKey oldKey = level instanceof ServerLevel ? pairKey() : null;
        this.frequency = config.frequency();
        this.channel = config.channel();
        this.privateMode = config.privateMode();
        this.editLock = config.editLock();
        this.sourceMode = config.sourceMode();
        this.fixedStrength = config.fixedStrength();
        this.manualMode = config.manualMode();
        if (oldKey != null) rebindIfChanged(oldKey, pairKey());
    }
}
