package com.kestalkayden.wirelessredstone.block;

import java.util.Optional;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

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
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TransmitterBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(WirelessRedstoneNeoForge.MOD_ID);
    public static final DeferredRegister.Items  ITEMS  = DeferredRegister.createItems(WirelessRedstoneNeoForge.MOD_ID);

    public static final DeferredBlock<TransmitterBlock> TRANSMITTER = BLOCKS.register("transmitter", id -> {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id);
        ResourceKey<LootTable> lootKey = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(WirelessRedstoneNeoForge.MOD_ID, "blocks/transmitter"));
        return new TransmitterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.REPEATER)
            .setId(blockKey)
            .overrideLootTable(Optional.of(lootKey))
            .mapColor(MapColor.METAL)
            .sound(SoundType.METAL)
            .strength(0.5F)
            .pushReaction(PushReaction.DESTROY));
    });

    public static final DeferredItem<BlockItem> TRANSMITTER_ITEM = ITEMS.register("transmitter", id -> {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);
        return new BlockItem(TRANSMITTER.get(),
            new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
    });

    private TransmitterBlocks() {}
}
