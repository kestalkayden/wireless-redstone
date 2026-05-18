package com.kestalkayden.wirelessredstone;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kestalkayden.wirelessredstone.block.ReceiverBlockEntities;
import com.kestalkayden.wirelessredstone.block.ReceiverBlocks;
import com.kestalkayden.wirelessredstone.block.TransmitterBlock;
import com.kestalkayden.wirelessredstone.block.TransmitterBlockEntities;
import com.kestalkayden.wirelessredstone.block.TransmitterBlocks;
import com.kestalkayden.wirelessredstone.client.ReceiverScreen;
import com.kestalkayden.wirelessredstone.client.TransmitterScreen;
import com.kestalkayden.wirelessredstone.component.ReceiverComponents;
import com.kestalkayden.wirelessredstone.component.TransmitterComponents;
import com.kestalkayden.wirelessredstone.config.ModConfig;
import com.kestalkayden.wirelessredstone.menu.ReceiverMenus;
import com.kestalkayden.wirelessredstone.menu.TransmitterMenus;
import com.kestalkayden.wirelessredstone.pairing.PairingRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(WirelessRedstoneNeoForge.MOD_ID)
public class WirelessRedstoneNeoForge {
    public static final String MOD_ID = "wirelessredstone";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Tasks queued from BE.onLoad to run at the end of the next server tick.
     *  Defers chunk-touchy work out of the chunk-load context without using TickTask
     *  (blocks shutdown) or scheduleTick (only for ticking chunks). Drained on
     *  ServerTickEvent.Post; pending tasks at shutdown are simply dropped. */
    public static final Queue<Runnable> PENDING_TICK_INITS = new ConcurrentLinkedQueue<>();

    public WirelessRedstoneNeoForge(IEventBus modBus) {
        LOGGER.info("Initializing Wireless Redstone (NeoForge)");

        ModConfig.load(FMLPaths.CONFIGDIR.get());

        TransmitterBlocks.BLOCKS.register(modBus);
        TransmitterBlocks.ITEMS.register(modBus);
        ReceiverBlocks.BLOCKS.register(modBus);
        ReceiverBlocks.ITEMS.register(modBus);
        TransmitterBlockEntities.BES.register(modBus);
        ReceiverBlockEntities.BES.register(modBus);
        TransmitterComponents.COMPONENTS.register(modBus);
        ReceiverComponents.COMPONENTS.register(modBus);
        TransmitterMenus.MENUS.register(modBus);
        ReceiverMenus.MENUS.register(modBus);

        NeoForge.EVENT_BUS.addListener(WirelessRedstoneNeoForge::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(WirelessRedstoneNeoForge::onServerTickPost);
        NeoForge.EVENT_BUS.addListener(WirelessRedstoneNeoForge::onRightClickBlock);
        modBus.addListener(WirelessRedstoneNeoForge::onBuildCreativeTabs);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modBus.addListener(WirelessRedstoneNeoForge::onRegisterMenuScreens);
        }
    }

    private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(TransmitterMenus.TRANSMITTER_MENU, TransmitterScreen::new);
        event.register(ReceiverMenus.RECEIVER_MENU, ReceiverScreen::new);
    }

    private static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            PairingRegistry.onLevelUnload(serverLevel);
        }
    }

    private static void onServerTickPost(ServerTickEvent.Post event) {
        Runnable task;
        while ((task = PENDING_TICK_INITS.poll()) != null) {
            try {
                task.run();
            } catch (Exception e) {
                LOGGER.error("Pending tick init failed", e);
            }
        }
    }

    /** Sneak-right-click on a transmitter cycles its ManualMode. We hook this event
     *  because vanilla's game mode skips block interactions entirely when the player
     *  sneaks with an item in hand. */
    private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!event.getEntity().isShiftKeyDown()) return;
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof TransmitterBlock)) return;
        event.setCanceled(true);
        event.setCancellationResult(TransmitterBlock.onSneakRightClick(event.getLevel(), event.getPos(), event.getEntity()));
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            ItemStack tx = new ItemStack(TransmitterBlocks.TRANSMITTER_ITEM.get());
            ItemStack rx = new ItemStack(ReceiverBlocks.RECEIVER_ITEM.get());
            event.insertAfter(new ItemStack(Items.REPEATER), tx,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
            event.insertAfter(tx, rx,
                CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }
}
