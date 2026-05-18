package com.kestalkayden.wirelessredstone.block;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReceiverBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BES =
        DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, WirelessRedstoneNeoForge.MOD_ID);

    public static BlockEntityType<ReceiverBlockEntity> RECEIVER_BE;

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReceiverBlockEntity>> RECEIVER_BE_HOLDER =
        BES.register("receiver", () -> {
            RECEIVER_BE = new BlockEntityType<>(ReceiverBlockEntity::new,
                ReceiverBlocks.RECEIVER.get());
            return RECEIVER_BE;
        });

    private ReceiverBlockEntities() {}
}
