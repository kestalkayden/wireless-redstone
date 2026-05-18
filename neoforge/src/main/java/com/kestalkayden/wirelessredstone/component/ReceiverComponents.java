package com.kestalkayden.wirelessredstone.component;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReceiverComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, WirelessRedstoneNeoForge.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ReceiverConfig>> RECEIVER_CONFIG_HOLDER =
        COMPONENTS.registerComponentType("receiver_config", builder -> builder
            .persistent(ReceiverConfig.CODEC)
            .networkSynchronized(ReceiverConfig.STREAM_CODEC));

    private ReceiverComponents() {}

    public static DataComponentType<ReceiverConfig> RECEIVER_CONFIG() {
        return RECEIVER_CONFIG_HOLDER.get();
    }
}
