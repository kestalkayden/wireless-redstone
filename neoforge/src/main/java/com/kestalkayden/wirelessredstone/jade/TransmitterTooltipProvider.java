package com.kestalkayden.wirelessredstone.jade;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntity;
import com.kestalkayden.wirelessredstone.component.ManualMode;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

class TransmitterTooltipProvider implements IBlockComponentProvider {
    private static final Identifier UID =
        Identifier.fromNamespaceAndPath(WirelessRedstoneNeoForge.MOD_ID, "transmitter");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof TransmitterBlockEntity be)) return;
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
        if (be.manualMode() != ManualMode.TOGGLE) {
            tooltip.add(Component.translatable(
                    "tooltip.wirelessredstone.manual_mode." + be.manualMode().getSerializedName())
                .withStyle(be.manualMode() == ManualMode.ALWAYS_ON
                    ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public Identifier getUid() { return UID; }
}
