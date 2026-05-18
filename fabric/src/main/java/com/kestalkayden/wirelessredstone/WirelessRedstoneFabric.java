package com.kestalkayden.wirelessredstone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntities;
import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntity;
import com.kestalkayden.wirelessredstone.block.ReceiverBlocks;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntities;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntity;
import com.kestalkayden.wirelessredstone.block.TransmitterBlocks;
import com.kestalkayden.wirelessredstone.component.ReceiverComponents;
import com.kestalkayden.wirelessredstone.component.TransmitterComponents;
import com.kestalkayden.wirelessredstone.menu.ReceiverMenus;
import com.kestalkayden.wirelessredstone.menu.TransmitterMenus;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public class WirelessRedstoneFabric implements ModInitializer {
    public static final String MOD_ID = "wirelessredstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Wireless Redstone (Fabric)");

        TransmitterBlocks.register();
        ReceiverBlocks.register();
        TransmitterBlockEntities.register();
        ReceiverBlockEntities.register();
        TransmitterComponents.register();
        ReceiverComponents.register();
        TransmitterMenus.register();
        ReceiverMenus.register();

        ServerLevelEvents.UNLOAD.register((server, level) -> PairingRegistry.onLevelUnload(level));

        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, level) -> {
            if (be instanceof TransmitterBlockEntity tbe) tbe.registerInRegistry();
            else if (be instanceof ReceiverBlockEntity rbe) rbe.registerInRegistry();
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.insertAfter(Items.REPEATER,
                TransmitterBlocks.TRANSMITTER_ITEM.getDefaultInstance(),
                ReceiverBlocks.RECEIVER_ITEM.getDefaultInstance());
        });
    }
}
