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
        ByteBufCodecs.idMapper(i -> {
            SourceMode[] v = values();
            return i >= 0 && i < v.length ? v[i] : ECHO;  // bounds-checked: a bad id decodes to default, not AIOOBE
        }, SourceMode::ordinal);

    private final String name;

    SourceMode(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
