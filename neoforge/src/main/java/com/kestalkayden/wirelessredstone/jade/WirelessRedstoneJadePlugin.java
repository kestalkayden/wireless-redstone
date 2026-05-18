package com.kestalkayden.wirelessredstone.jade;

import com.kestalkayden.wirelessredstone.WirelessRedstoneNeoForge;
import com.kestalkayden.wirelessredstone.block.ReceiverBlock;
import com.kestalkayden.wirelessredstone.block.TransmitterBlock;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/** Jade plugin — adds in-world tooltips for transmitter/receiver showing
 *  channel, frequency, lock, private, manual mode. Jade is an optional
 *  dep; this class is only loaded if Jade is installed (Jade's annotation
 *  scanner picks up @WailaPlugin). */
@WailaPlugin(WirelessRedstoneNeoForge.MOD_ID)
public class WirelessRedstoneJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new TransmitterTooltipProvider(), TransmitterBlock.class);
        registration.registerBlockComponent(new ReceiverTooltipProvider(), ReceiverBlock.class);
    }
}
