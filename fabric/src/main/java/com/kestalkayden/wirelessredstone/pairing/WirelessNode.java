package com.kestalkayden.wirelessredstone.pairing;

import net.minecraft.core.BlockPos;

public interface WirelessNode {
    PairKey pairKey();
    BlockPos pos();

    interface Transmitter extends WirelessNode {
        int currentStrength();
    }

    interface Receiver extends WirelessNode {
        /** Update internal strength + visual POWERED state. Does NOT fire neighbor
         *  updates — that's split into {@link #notifyNeighbors()} so the registry
         *  can update all receivers' state before firing any neighbor wave.
         *  This avoids stale weak-power propagation through redstone-conductor
         *  neighbors (e.g., adjacent lamps reading each other) during iteration. */
        void setStrengthSilent(int newStrength);

        /** Fire vanilla neighbor updates so downstream redstone re-evaluates. Called
         *  after all receivers have had {@link #setStrengthSilent} applied. */
        void notifyNeighbors();
    }
}
