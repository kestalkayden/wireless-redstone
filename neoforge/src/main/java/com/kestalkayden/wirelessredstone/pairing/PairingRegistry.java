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

    /** Registration path — creates the per-level registry on demand. */
    public static PairingRegistry get(ServerLevel level) {
        return PER_LEVEL.computeIfAbsent(level, l -> new PairingRegistry());
    }

    /** Removal path — never resurrects a level's registry after {@link #onLevelUnload}.
     *  BE teardown can run after the level-unload event, so removals must not re-insert a
     *  now-dead ServerLevel (which would leak it and its whole object graph in PER_LEVEL). */
    public static PairingRegistry getIfPresent(ServerLevel level) {
        return PER_LEVEL.get(level);
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
        if (!cfg.rangeLocked) return maxStrength(b);
        return maxStrengthWithin(b, receiverPos, (long) cfg.maxRange * cfg.maxRange);
    }

    private static int maxStrength(PairBucket b) {
        int max = 0;
        for (WirelessNode.Transmitter tx : b.transmitters) {
            int s = tx.currentStrength();
            if (s > max) max = s;
            if (max == 15) return 15;
        }
        return max;
    }

    private static int maxStrengthWithin(PairBucket b, BlockPos receiverPos, long maxSq) {
        int max = 0;
        for (WirelessNode.Transmitter tx : b.transmitters) {
            if (tx.pos().distSqr(receiverPos) > maxSq) continue;
            int s = tx.currentStrength();
            if (s > max) max = s;
            if (max == 15) return 15;
        }
        return max;
    }

    /** Transmitters on this key that can actually reach {@code near} (range-gated), for GUI. */
    public int transmitterCount(PairKey key, BlockPos near) {
        PairBucket b = buckets.get(key);
        if (b == null) return 0;
        ModConfig cfg = ModConfig.get();
        if (!cfg.rangeLocked) return b.transmitters.size();
        long maxSq = (long) cfg.maxRange * cfg.maxRange;
        int n = 0;
        for (WirelessNode.Transmitter tx : b.transmitters) {
            if (tx.pos().distSqr(near) <= maxSq) n++;
        }
        return n;
    }

    /** Receivers on this key that this block can actually reach (range-gated), for GUI. */
    public int receiverCount(PairKey key, BlockPos near) {
        PairBucket b = buckets.get(key);
        if (b == null) return 0;
        ModConfig cfg = ModConfig.get();
        if (!cfg.rangeLocked) return b.receivers.size();
        long maxSq = (long) cfg.maxRange * cfg.maxRange;
        int n = 0;
        for (WirelessNode.Receiver rx : b.receivers) {
            if (rx.pos().distSqr(near) <= maxSq) n++;
        }
        return n;
    }

    public void notifyTransmitterChanged(PairKey key) {
        PairBucket b = buckets.get(key);
        if (b == null || b.receivers.isEmpty()) return;

        // Snapshot to avoid ConcurrentModificationException — notifyNeighbors() triggers
        // vanilla updates that can cause OTHER BEs to call setRemoved (e.g. during
        // chunk-unload-on-shutdown), which mutates b.receivers via removeReceiver.
        List<WirelessNode.Receiver> snapshot = new ArrayList<>(b.receivers);

        ModConfig cfg = ModConfig.get();
        boolean limited = cfg.rangeLocked;
        long maxSq = (long) cfg.maxRange * cfg.maxRange;
        // Unlimited range: the max is identical for every receiver — compute it once (O(T))
        // instead of re-scanning all transmitters per receiver (O(R*T)).
        int sharedMax = limited ? 0 : maxStrength(b);

        // Two-pass: update ALL receivers' strength + POWERED state FIRST (without firing
        // neighbor waves), then fire neighbor updates AFTER — and only for receivers whose
        // emitted strength actually changed. The split avoids stale weak-power reads through
        // solid conductors shared by sibling receivers; skipping unchanged receivers avoids
        // firing pointless neighbor waves (most receivers are unchanged on any given edge,
        // since strengths MAX-combine across transmitters).
        List<WirelessNode.Receiver> changed = null;
        for (WirelessNode.Receiver rx : snapshot) {
            int strength = limited ? maxStrengthWithin(b, rx.pos(), maxSq) : sharedMax;
            if (rx.setStrengthSilent(strength)) {
                if (changed == null) changed = new ArrayList<>();
                changed.add(rx);
            }
        }
        if (changed != null) {
            for (WirelessNode.Receiver rx : changed) rx.notifyNeighbors();
        }
    }
}
