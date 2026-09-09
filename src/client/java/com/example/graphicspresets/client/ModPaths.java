package com.example.graphicspresets.client;

import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;

public class ModPaths {
    public static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    public static final Path DISTANT_HORIZONS = CONFIG_DIR.resolve("DistantHorizons.toml");
    public static final Path IRIS_PROPERTIES = CONFIG_DIR.resolve("iris.properties");

    public static final Path PHYSICS_CLIENT = CONFIG_DIR.resolve("physicsmod/physics_client_config.json");
    public static final Path PHYSICS_BLOCKS = CONFIG_DIR.resolve("physicsmod/physics_blocks_client_config.json");
}