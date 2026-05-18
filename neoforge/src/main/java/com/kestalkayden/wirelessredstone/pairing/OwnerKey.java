package com.kestalkayden.wirelessredstone.pairing;

import java.util.UUID;

public sealed interface OwnerKey {
    OwnerKey PUBLIC = new Public();

    record Public() implements OwnerKey {}
    record Owned(UUID uuid) implements OwnerKey {}
}
