package com.kestalkayden.wirelessredstone.component;

import com.mojang.serialization.Codec;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** Overrides what a transmitter broadcasts, regardless of redstone input.
 *  Sneak-right-click on a transmitter cycles through these. */
public enum ManualMode implements StringRepresentable {
    /** Default — broadcast follows redstone input (the existing SourceMode/ECHO behavior). */
    TOGGLE("toggle"),
    /** Always broadcast strength 15, input is ignored. */
    ALWAYS_ON("always_on"),
    /** Always broadcast 0, input is ignored. Acts as a kill switch. */
    ALWAYS_OFF("always_off");

    public static final Codec<ManualMode> CODEC = StringRepresentable.fromEnum(ManualMode::values);
    public static final StreamCodec<ByteBuf, ManualMode> STREAM_CODEC =
        ByteBufCodecs.idMapper(i -> values()[i], ManualMode::ordinal);

    private final String name;

    ManualMode(String name) { this.name = name; }

    @Override
    public String getSerializedName() { return name; }

    public ManualMode next() {
        return switch (this) {
            case TOGGLE -> ALWAYS_ON;
            case ALWAYS_ON -> ALWAYS_OFF;
            case ALWAYS_OFF -> TOGGLE;
        };
    }

    public static ManualMode fromName(String name) {
        for (ManualMode m : values()) if (m.name.equals(name)) return m;
        return TOGGLE;
    }
}
