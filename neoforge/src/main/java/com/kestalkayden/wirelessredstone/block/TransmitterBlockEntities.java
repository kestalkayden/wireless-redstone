package com.kestalkayden.wirelessredstone.block;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TransmitterBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WirelessRedstoneNeoForge.MOD_ID);

    public static BlockEntityType<TransmitterBlockEntity> TRANSMITTER_BE;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TransmitterBlockEntity>> TRANSMITTER_BE_HOLDER =
        BES.register("transmitter", () -> {
            TRANSMITTER_BE = new BlockEntityType<>(TransmitterBlockEntity::new,
                TransmitterBlocks.TRANSMITTER.get());
            return TRANSMITTER_BE;
        });

    private TransmitterBlockEntities() {}
}
