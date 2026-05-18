package com.kestalkayden.wirelessredstone.component;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ReceiverComponents {
    private static DataComponentType<ReceiverConfig> receiverConfig;

    private ReceiverComponents() {}

    public static void register() {
        receiverConfig = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "receiver_config"),
            DataComponentType.<ReceiverConfig>builder()
                .persistent(ReceiverConfig.CODEC)
                .networkSynchronized(ReceiverConfig.STREAM_CODEC)
                .build()
        );
    }

    public static DataComponentType<ReceiverConfig> RECEIVER_CONFIG() {
        return receiverConfig;
    }
}
