package com.kestalkayden.wirelessredstone.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public final class ModConfig {
    private static final Logger LOG = LoggerFactory.getLogger("wirelessredstone-config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "wirelessredstone.json";

    public static final int RANGE_MIN = 4;
    public static final int RANGE_MAX = 1024;

    public boolean rangeLocked = true;
    public int maxRange = 128;

    private static volatile ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        ModConfig config = null;
        if (Files.exists(file)) {
            try {
                config = GSON.fromJson(Files.readString(file), ModConfig.class);
            } catch (IOException | JsonSyntaxException e) {
                LOG.error("Failed to read {}, using defaults", file, e);
            }
        }
        if (config == null) config = new ModConfig();
        config.normalize();
        INSTANCE = config;
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(config));
        } catch (IOException e) {
            LOG.warn("Failed to write {}", file, e);
        }
    }

    private void normalize() {
        if (maxRange < RANGE_MIN) maxRange = RANGE_MIN;
        if (maxRange > RANGE_MAX) maxRange = RANGE_MAX;
    }
}
