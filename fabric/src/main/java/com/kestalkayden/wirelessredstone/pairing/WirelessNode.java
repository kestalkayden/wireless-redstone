package com.kestalkayden.wirelessredstone.pairing;

import net.minecraft.core.BlockPos;

public interface WirelessNode {
    PairKey pairKey();
    BlockPos pos();

    interface Transmitter extends WirelessNode {
        int currentStrength();
    }

    interface Receiver extends WirelessNode {
        void onExternalStrengthChanged(int newStrength);
    }
}
