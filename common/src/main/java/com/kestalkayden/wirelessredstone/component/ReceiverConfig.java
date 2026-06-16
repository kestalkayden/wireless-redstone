package com.kestalkayden.wirelessredstone.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ReceiverConfig(
    int frequency,
    int channel,
    boolean privateMode,
    boolean editLock
) {
    public static final ReceiverConfig DEFAULT = new ReceiverConfig(0, 1, false, false);

    public static final Codec<ReceiverConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("frequency", 0).forGetter(ReceiverConfig::frequency),
        Codec.INT.optionalFieldOf("channel", 1).forGetter(ReceiverConfig::channel),
        Codec.BOOL.optionalFieldOf("private_mode", false).forGetter(ReceiverConfig::privateMode),
        Codec.BOOL.optionalFieldOf("edit_lock", false).forGetter(ReceiverConfig::editLock)
    ).apply(instance, ReceiverConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReceiverConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, ReceiverConfig::frequency,
        ByteBufCodecs.VAR_INT, ReceiverConfig::channel,
        ByteBufCodecs.BOOL, ReceiverConfig::privateMode,
        ByteBufCodecs.BOOL, ReceiverConfig::editLock,
        ReceiverConfig::new
    );
}
