package com.kestalkayden.wirelessredstone.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TransmitterConfig(
    int frequency,
    int channel,
    boolean ownerLock,
    SourceMode sourceMode,
    int fixedStrength
) {
    public static final TransmitterConfig DEFAULT = new TransmitterConfig(0, 1, false, SourceMode.ECHO, 15);

    public static final Codec<TransmitterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.optionalFieldOf("frequency", 0).forGetter(TransmitterConfig::frequency),
        Codec.INT.optionalFieldOf("channel", 1).forGetter(TransmitterConfig::channel),
        Codec.BOOL.optionalFieldOf("owner_lock", false).forGetter(TransmitterConfig::ownerLock),
        SourceMode.CODEC.optionalFieldOf("source_mode", SourceMode.ECHO).forGetter(TransmitterConfig::sourceMode),
        Codec.INT.optionalFieldOf("fixed_strength", 15).forGetter(TransmitterConfig::fixedStrength)
    ).apply(instance, TransmitterConfig::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TransmitterConfig> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, TransmitterConfig::frequency,
        ByteBufCodecs.VAR_INT, TransmitterConfig::channel,
        ByteBufCodecs.BOOL, TransmitterConfig::ownerLock,
        SourceMode.STREAM_CODEC, TransmitterConfig::sourceMode,
        ByteBufCodecs.VAR_INT, TransmitterConfig::fixedStrength,
        TransmitterConfig::new
    );
}
