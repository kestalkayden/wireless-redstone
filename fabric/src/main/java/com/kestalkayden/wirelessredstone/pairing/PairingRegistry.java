package com.kestalkayden.wirelessredstone.pairing;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.server.level.ServerLevel;

public final class PairingRegistry {
    private static final Map<ServerLevel, PairingRegistry> PER_LEVEL = new IdentityHashMap<>();

    public static PairingRegistry get(ServerLevel level) {
        return PER_LEVEL.computeIfAbsent(level, l -> new PairingRegistry());
    }

    public static void onLevelUnload(ServerLevel level) {
        PER_LEVEL.remove(level);
    }

    private final Map<PairKey, PairBucket> buckets = new HashMap<>();

    private PairingRegistry() {}

    public void addTransmitter(PairKey key, WirelessNode.Transmitter tx) {
        buckets.computeIfAbsent(key, k -> new PairBucket()).transmitters.add(tx);
    }

    public void removeTransmitter(PairKey key, WirelessNode.Transmitter tx) {
        PairBucket b = buckets.get(key);
        if (b == null) return;
        b.transmitters.remove(tx);
        if (b.isEmpty()) buckets.remove(key);
    }

    public void addReceiver(PairKey key, WirelessNode.Receiver rx) {
        buckets.computeIfAbsent(key, k -> new PairBucket()).receivers.add(rx);
    }

    public void removeReceiver(PairKey key, WirelessNode.Receiver rx) {
        PairBucket b = buckets.get(key);
        if (b == null) return;
        b.receivers.remove(rx);
        if (b.isEmpty()) buckets.remove(key);
    }

    public int currentStrength(PairKey key) {
        PairBucket b = buckets.get(key);
        if (b == null) return 0;
        int max = 0;
        for (WirelessNode.Transmitter tx : b.transmitters) {
            int s = tx.currentStrength();
            if (s > max) max = s;
            if (max == 15) return 15;
        }
        return max;
    }

    public int transmitterCount(PairKey key) {
        PairBucket b = buckets.get(key);
        return b == null ? 0 : b.transmitters.size();
    }

    public int receiverCount(PairKey key) {
        PairBucket b = buckets.get(key);
        return b == null ? 0 : b.receivers.size();
    }

    public void notifyTransmitterChanged(PairKey key) {
        PairBucket b = buckets.get(key);
        if (b == null || b.receivers.isEmpty()) return;
        int newMax = currentStrength(key);
        for (WirelessNode.Receiver rx : b.receivers) {
            rx.onExternalStrengthChanged(newMax);
        }
    }
}
