package com.kestalkayden.wirelessredstone.pairing;

import java.util.ArrayList;
import java.util.List;

final class PairBucket {
    final List<WirelessNode.Transmitter> transmitters = new ArrayList<>(2);
    final List<WirelessNode.Receiver> receivers = new ArrayList<>(4);

    boolean isEmpty() {
        return transmitters.isEmpty() && receivers.isEmpty();
    }
}
