package com.kestalkayden.wirelessredstone.component;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class TransmitterComponents {
    private static DataComponentType<TransmitterConfig> transmitterConfig;

    private TransmitterComponents() {}

    public static void register() {
        transmitterConfig = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "transmitter_config"),
            DataComponentType.<TransmitterConfig>builder()
                .persistent(TransmitterConfig.CODEC)
                .networkSynchronized(TransmitterConfig.STREAM_CODEC)
                .build()
        );
    }

    public static DataComponentType<TransmitterConfig> TRANSMITTER_CONFIG() {
        return transmitterConfig;
    }
}
