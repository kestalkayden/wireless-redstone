package com.kestalkayden.wirelessredstone.block;

import java.util.function.Consumer;

import com.kestalkayden.wirelessredstone.component.ReceiverComponents;
import com.kestalkayden.wirelessredstone.component.ReceiverConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class ReceiverItem extends BlockItem {

    public ReceiverItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display,
                                 Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        ReceiverConfig config = stack.get(ReceiverComponents.RECEIVER_CONFIG());
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
    }
}
