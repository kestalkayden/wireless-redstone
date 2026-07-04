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
         *  updates — that's split into {@link #notifyNeighbors()} so the registry can
         *  update all receivers' state before firing any neighbor wave, avoiding stale
         *  weak-power propagation through redstone-conductor neighbors during iteration.
         *  @return true iff the emitted strength actually changed, so the caller can skip
         *          firing a neighbor wave for receivers whose output did not change. */
        boolean setStrengthSilent(int newStrength);

        /** Fire vanilla neighbor updates so downstream redstone re-evaluates. Called
         *  after all receivers have had {@link #setStrengthSilent} applied. */
        void notifyNeighbors();
    }
}
