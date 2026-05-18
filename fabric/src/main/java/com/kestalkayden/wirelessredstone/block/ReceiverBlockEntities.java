package com.kestalkayden.wirelessredstone.block;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ReceiverBlockEntities {
    public static BlockEntityType<ReceiverBlockEntity> RECEIVER_BE;

    private ReceiverBlockEntities() {}

    public static void register() {
        RECEIVER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "receiver"),
            FabricBlockEntityTypeBuilder.create(ReceiverBlockEntity::new,
                ReceiverBlocks.RECEIVER
            ).build());
    }
}
