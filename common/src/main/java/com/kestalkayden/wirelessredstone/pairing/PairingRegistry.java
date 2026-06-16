package com.kestalkayden.wirelessredstone.pairing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.kestalkayden.wirelessredstone.config.ModConfig;

import net.minecraft.core.BlockPos;
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

    /** Max transmitter strength on this key visible from {@code receiverPos}. Honors
     *  ModConfig.rangeLocked / maxRange — out-of-range transmitters are filtered out. */
    public int currentStrengthFor(PairKey key, BlockPos receiverPos) {
        PairBucket b = buckets.get(key);
        if (b == null) return 0;
        ModConfig cfg = ModConfig.get();
        boolean limited = cfg.rangeLocked;
        long maxSq = (long) cfg.maxRange * cfg.maxRange;
        int max = 0;
        for (WirelessNode.Transmitter tx : b.transmitters) {
            if (limited && tx.pos().distSqr(receiverPos) > maxSq) continue;
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
        // Two-pass: update ALL receivers' strength + POWERED state FIRST (without
        // firing neighbor waves), then fire neighbor updates AFTER. Critical when
        // receivers have adjacent solid-block neighbors (e.g., redstone lamps next
        // to each other): vanilla's hasNeighborSignal checks weak power through
        // solid neighbors, so a stale strength on a later-iterated rx would make
        // the earlier rx's lamp think it still has signal.
        // Snapshot to avoid ConcurrentModificationException — notifyNeighbors()
        // triggers vanilla updates that can cause OTHER BEs to call setRemoved
        // (e.g., during chunk-unload-on-shutdown), which mutates b.receivers
        // via removeReceiver while we iterate.
        List<WirelessNode.Receiver> snapshot = new ArrayList<>(b.receivers);
        for (WirelessNode.Receiver rx : snapshot) {
            rx.setStrengthSilent(currentStrengthFor(key, rx.pos()));
        }
        for (WirelessNode.Receiver rx : snapshot) {
            rx.notifyNeighbors();
        }
    }
}
