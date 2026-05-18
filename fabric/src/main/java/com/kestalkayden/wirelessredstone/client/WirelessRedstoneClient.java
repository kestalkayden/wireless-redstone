package com.kestalkayden.wirelessredstone.client;

import com.kestalkayden.wirelessredstone.menu.ReceiverMenus;
import com.kestalkayden.wirelessredstone.menu.TransmitterMenus;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

public class WirelessRedstoneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MenuScreens.register(TransmitterMenus.TRANSMITTER_MENU, TransmitterScreen::new);
        MenuScreens.register(ReceiverMenus.RECEIVER_MENU, ReceiverScreen::new);
    }
}
