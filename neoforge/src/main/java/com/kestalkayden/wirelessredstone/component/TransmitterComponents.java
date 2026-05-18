package com.kestalkayden.wirelessredstone.component;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TransmitterComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WirelessRedstoneNeoForge.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TransmitterConfig>> TRANSMITTER_CONFIG_HOLDER =
        COMPONENTS.registerComponentType("transmitter_config", builder -> builder
            .persistent(TransmitterConfig.CODEC)
            .networkSynchronized(TransmitterConfig.STREAM_CODEC));

    private TransmitterComponents() {}

    public static DataComponentType<TransmitterConfig> TRANSMITTER_CONFIG() {
        return TRANSMITTER_CONFIG_HOLDER.get();
    }
}
