package com.kestalkayden.wirelessredstone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(WirelessRedstoneNeoForge.MOD_ID)
public class WirelessRedstoneNeoForge {
    public static final String MOD_ID = "wirelessredstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public WirelessRedstoneNeoForge(IEventBus modBus) {
        LOGGER.info("Initializing Wireless Redstone (NeoForge)");
    }
}
