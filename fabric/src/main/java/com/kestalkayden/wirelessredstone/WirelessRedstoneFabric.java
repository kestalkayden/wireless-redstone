package com.kestalkayden.wirelessredstone;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntities;
import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntity;
import com.kestalkayden.wirelessredstone.block.ReceiverBlocks;
import com.kestalkayden.wirelessredstone.block.TransmitterBlock;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntities;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntity;
import com.kestalkayden.wirelessredstone.block.TransmitterBlocks;
import com.kestalkayden.wirelessredstone.component.ReceiverComponents;
import com.kestalkayden.wirelessredstone.component.TransmitterComponents;
import com.kestalkayden.wirelessredstone.config.ModConfig;
import com.kestalkayden.wirelessredstone.menu.ReceiverMenus;
import com.kestalkayden.wirelessredstone.menu.TransmitterMenus;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLevelEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;

public class WirelessRedstoneFabric implements ModInitializer {
    public static final String MOD_ID = "wirelessredstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Tasks queued from BE.onLoad to run at the end of the next server tick.
     *  This defers chunk-touchy work out of the chunk-load context without using TickTask
     *  (which can block shutdown waiting for tick advancement) or scheduleTick (which only
     *  fires for chunks in TICKING state). The queue is drained on END_SERVER_TICK and any
     *  pending tasks at shutdown are simply dropped (no hang). */
    public static final Queue<Runnable> PENDING_TICK_INITS = new ConcurrentLinkedQueue<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Wireless Redstone (Fabric)");

        ModConfig.load(FabricLoader.getInstance().getConfigDir());

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

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            Runnable task;
            while ((task = PENDING_TICK_INITS.poll()) != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    LOGGER.error("Pending tick init failed", e);
                }
            }
        });

        // Sneak-right-click on a transmitter cycles its ManualMode. We register at the
        // UseBlockCallback level because vanilla's game mode skips block interactions
        // entirely when the player sneaks with an item in hand.
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!player.isShiftKeyDown()) return InteractionResult.PASS;
            if (!(world.getBlockState(hit.getBlockPos()).getBlock() instanceof TransmitterBlock)) return InteractionResult.PASS;
            return TransmitterBlock.onSneakRightClick(world, hit.getBlockPos(), player);
        });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(output -> {
            output.insertAfter(Items.REPEATER,
                TransmitterBlocks.TRANSMITTER_ITEM.getDefaultInstance(),
                ReceiverBlocks.RECEIVER_ITEM.getDefaultInstance());
        });
    }
}
