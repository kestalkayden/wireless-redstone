package com.kestalkayden.wirelessredstone.block;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class TransmitterBlockEntities {
    public static BlockEntityType<TransmitterBlockEntity> TRANSMITTER_BE;

    private TransmitterBlockEntities() {}

    public static void register() {
        TRANSMITTER_BE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "transmitter"),
            FabricBlockEntityTypeBuilder.create(TransmitterBlockEntity::new,
                TransmitterBlocks.TRANSMITTER
            ).build());
    }
}
