package com.kestalkayden.wirelessredstone.menu;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TransmitterMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, WirelessRedstoneNeoForge.MOD_ID);

    public static MenuType<TransmitterMenu> TRANSMITTER_MENU;

    public static final DeferredHolder<MenuType<?>, MenuType<TransmitterMenu>> TRANSMITTER_MENU_HOLDER =
        MENUS.register("transmitter", () -> {
            TRANSMITTER_MENU = new MenuType<>(TransmitterMenu::new, FeatureFlags.VANILLA_SET);
            return TRANSMITTER_MENU;
        });

    private TransmitterMenus() {}
}
