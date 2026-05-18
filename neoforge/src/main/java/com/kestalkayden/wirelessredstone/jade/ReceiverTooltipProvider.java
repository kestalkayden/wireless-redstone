package com.kestalkayden.wirelessredstone.jade;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;
import com.kestalkayden.wirelessredstone.block.ReceiverBlock;
import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

class ReceiverTooltipProvider implements IBlockComponentProvider {
    private static final Identifier UID =
        Identifier.fromNamespaceAndPath(WirelessRedstoneNeoForge.MOD_ID, "receiver");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof ReceiverBlockEntity be)) return;
        tooltip.add(Component.translatable("tooltip.wirelessredstone.channel", be.channel())
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.wirelessredstone.frequency", be.frequency())
            .withStyle(ChatFormatting.GRAY));
        if (be.privateMode()) {
            tooltip.add(Component.translatable("tooltip.wirelessredstone.private")
                .withStyle(ChatFormatting.GOLD));
        }
        if (be.editLock()) {
            tooltip.add(Component.translatable("tooltip.wirelessredstone.locked")
                .withStyle(ChatFormatting.YELLOW));
        }
        // POWERED is synced to client, currentStrength isn't — use the blockstate
        // property as a binary "receiving signal" indicator.
        if (accessor.getBlockState().getValue(ReceiverBlock.POWERED)) {
            tooltip.add(Component.translatable("tooltip.wirelessredstone.receiving")
                .withStyle(ChatFormatting.AQUA));
        }
    }

    @Override
    public Identifier getUid() { return UID; }
}
