package com.kestalkayden.wirelessredstone.component;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum SourceMode implements StringRepresentable {
    ECHO("echo"),
    FIXED("fixed");

    public static final Codec<SourceMode> CODEC = StringRepresentable.fromEnum(SourceMode::values);
    public static final StreamCodec<ByteBuf, SourceMode> STREAM_CODEC =
        ByteBufCodecs.idMapper(i -> values()[i], SourceMode::ordinal);

    private final String name;

    SourceMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
