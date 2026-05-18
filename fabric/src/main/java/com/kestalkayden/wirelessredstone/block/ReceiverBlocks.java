package com.kestalkayden.wirelessredstone.block;

import java.util.Optional;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;

public final class ReceiverBlocks {
    public static ReceiverBlock RECEIVER;
    public static BlockItem RECEIVER_ITEM;

    private ReceiverBlocks() {}

    public static void register() {
        Identifier id = Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "receiver");
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        ResourceKey<LootTable> lootKey = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "blocks/receiver"));

        RECEIVER = Registry.register(BuiltInRegistries.BLOCK, id,
            new ReceiverBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
                .setId(blockKey)
                .overrideLootTable(Optional.of(lootKey))
                .mapColor(MapColor.METAL)
                .sound(SoundType.METAL)
                .strength(0.5F)
                .pushReaction(PushReaction.DESTROY)));

        RECEIVER_ITEM = Registry.register(BuiltInRegistries.ITEM, id,
            new ReceiverItem(RECEIVER, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
    }
}
