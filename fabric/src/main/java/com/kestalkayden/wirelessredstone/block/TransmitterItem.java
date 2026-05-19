package com.kestalkayden.wirelessredstone.block;

import java.util.function.Consumer;

import com.kestalkayden.wirelessredstone.component.ManualMode;
import com.kestalkayden.wirelessredstone.component.TransmitterComponents;
import com.kestalkayden.wirelessredstone.component.TransmitterConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class TransmitterItem extends BlockItem {

    public TransmitterItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        // Subtitle adds "Redstone" to the searchable tooltip text so JEI/REI/EMI
        // (and vanilla creative with tooltip-search enabled) match a "redstone" query.
        tooltip.accept(Component.translatable("tooltip.wirelessredstone.subtitle.transmitter")
            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        TransmitterConfig config = stack.get(TransmitterComponents.TRANSMITTER_CONFIG());
        if (config == null) return;
        tooltip.accept(Component.translatable("tooltip.wirelessredstone.channel", config.channel())
            .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.wirelessredstone.frequency", config.frequency())
            .withStyle(ChatFormatting.GRAY));
        if (config.privateMode()) {
            tooltip.accept(Component.translatable("tooltip.wirelessredstone.private")
                .withStyle(ChatFormatting.GOLD));
        }
        if (config.editLock()) {
            tooltip.accept(Component.translatable("tooltip.wirelessredstone.locked")
                .withStyle(ChatFormatting.YELLOW));
        }
        if (config.manualMode() != ManualMode.TOGGLE) {
            tooltip.accept(Component.translatable(
                    "tooltip.wirelessredstone.manual_mode." + config.manualMode().getSerializedName())
                .withStyle(config.manualMode() == ManualMode.ALWAYS_ON ? ChatFormatting.AQUA : ChatFormatting.DARK_GRAY));
        }
    }
}
