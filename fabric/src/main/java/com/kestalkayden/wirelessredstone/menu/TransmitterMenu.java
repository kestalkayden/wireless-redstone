package com.kestalkayden.wirelessredstone.menu;

import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntity;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;

public class TransmitterMenu extends AbstractContainerMenu {

    public static final int DATA_FREQ           = 0;
    public static final int DATA_CHAN           = 1;
    public static final int DATA_LOCK           = 2;
    public static final int DATA_RECEIVER_COUNT = 3;
    public static final int DATA_SIZE           = 4;

    public static final int CMD_FREQ = 0;
    public static final int CMD_CHAN = 1;
    public static final int CMD_LOCK = 2;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final TransmitterBlockEntity be;  // null on client side

    public TransmitterMenu(int containerId, Inventory playerInv) {
        this(containerId, playerInv, null, new SimpleContainerData(DATA_SIZE), ContainerLevelAccess.NULL);
    }

    public TransmitterMenu(int containerId, Inventory playerInv, TransmitterBlockEntity be, ContainerLevelAccess access) {
        this(containerId, playerInv, be, new ContainerData() {
            @Override public int get(int i) {
                if (be == null) return 0;
                return switch (i) {
                    case DATA_FREQ -> be.frequency();
                    case DATA_CHAN -> be.channel();
                    case DATA_LOCK -> be.ownerLock() ? 1 : 0;
                    case DATA_RECEIVER_COUNT -> be.getLevel() instanceof ServerLevel sl
                        ? PairingRegistry.get(sl).receiverCount(be.pairKey())
                        : 0;
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return DATA_SIZE; }
        }, access);
    }

    private TransmitterMenu(int containerId, Inventory playerInv, TransmitterBlockEntity be,
                            ContainerData data, ContainerLevelAccess access) {
        super(TransmitterMenus.TRANSMITTER_MENU, containerId);
        this.be = be;
        this.data = data;
        this.access = access;

        addDataSlots(data);
    }

    public int frequency()     { return data.get(DATA_FREQ); }
    public int channel()       { return data.get(DATA_CHAN); }
    public boolean ownerLock() { return data.get(DATA_LOCK) != 0; }
    public int receiverCount() { return data.get(DATA_RECEIVER_COUNT); }

    public static int encodeFreq(int v)     { return (CMD_FREQ << 24) | (v & 0xFFFFFF); }
    public static int encodeChan(int v)     { return (CMD_CHAN << 24) | (v & 0xFFFFFF); }
    public static int encodeLock(boolean v) { return (CMD_LOCK << 24) | (v ? 1 : 0); }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (be == null) return false;
        int cmd = (id >>> 24) & 0xFF;
        int value = id & 0xFFFFFF;
        switch (cmd) {
            case CMD_FREQ -> { be.setFrequency(value); return true; }
            case CMD_CHAN -> { be.setChannel(value); return true; }
            case CMD_LOCK -> { be.setOwnerLock(value != 0); return true; }
            default -> { return false; }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) ->
            player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0, true);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
