package com.kestalkayden.wirelessredstone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;

public class WirelessRedstoneFabric implements ModInitializer {
    public static final String MOD_ID = "wirelessredstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Wireless Redstone (Fabric)");
    }
}
