package com.kestalkayden.wirelessredstone.menu;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ReceiverMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(Registries.MENU, WirelessRedstoneNeoForge.MOD_ID);

    public static MenuType<ReceiverMenu> RECEIVER_MENU;

    public static final DeferredHolder<MenuType<?>, MenuType<ReceiverMenu>> RECEIVER_MENU_HOLDER =
        MENUS.register("receiver", () -> {
            RECEIVER_MENU = new MenuType<>(ReceiverMenu::new, FeatureFlags.VANILLA_SET);
            return RECEIVER_MENU;
        });

    private ReceiverMenus() {}
}
