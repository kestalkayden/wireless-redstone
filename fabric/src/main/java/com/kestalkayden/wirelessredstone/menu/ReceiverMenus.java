package com.kestalkayden.wirelessredstone.menu;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ReceiverMenus {
    public static MenuType<ReceiverMenu> RECEIVER_MENU;

    private ReceiverMenus() {}

    public static void register() {
        RECEIVER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "receiver"),
            new MenuType<>(ReceiverMenu::new, FeatureFlags.VANILLA_SET));
    }
}
