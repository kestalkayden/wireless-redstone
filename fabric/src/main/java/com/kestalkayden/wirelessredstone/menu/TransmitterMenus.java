package com.kestalkayden.wirelessredstone.menu;

import com.kestalkayden.wirelessredstone.WirelessRedstoneFabric;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class TransmitterMenus {
    public static MenuType<TransmitterMenu> TRANSMITTER_MENU;

    private TransmitterMenus() {}

    public static void register() {
        TRANSMITTER_MENU = Registry.register(
            BuiltInRegistries.MENU,
            Identifier.fromNamespaceAndPath(WirelessRedstoneFabric.MOD_ID, "transmitter"),
            new MenuType<>(TransmitterMenu::new, FeatureFlags.VANILLA_SET));
    }
}
