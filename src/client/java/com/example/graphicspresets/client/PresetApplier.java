package com.example.graphicspresets.client;

import net.minecraft.client.Minecraft;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PresetApplier {

    public enum GraphicsPreset {
        VERY_LOW("§aVery Low", "Physics Off, DH 16 Radius, Render Distance 6"),
        LOW("§bLow", "Physics Off, DH 24 Radius, Render Distance 8"),
        MEDIUM("§eMedium", "Physics On, DH 32 Radius, Render Distance 10"),
        HIGH("§6High", "Physics On, DH 64 Radius, Render Distance 12"),
        ULTRA("§c§lUltra", "Physics On, DH 128 Radius, Render Distance 14"),
        TRAILER("§5§lTrailer", "Trailer Setup | Iteration Shader | Physics ON | DH 32 | RD 10"),
        CUSTOM("§dCustom", "No changes applied. Keeps your personal settings.");

        private final String name;
        private final String desc;

        GraphicsPreset(String name, String desc) {
            this.name = name;
            this.desc = desc;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }
    }

    public static void apply(GraphicsPreset preset) {
        try {
            switch (preset) {
                case VERY_LOW -> {
                    setPhysicsModEnabled(false, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"VERY_LOW\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "16");

                    setRenderDistance(6);
                }
                case LOW -> {
                    setPhysicsModEnabled(false, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"LOW\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "24");

                    setRenderDistance(8);
                }
                case MEDIUM -> {
                    setPhysicsModEnabled(true, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"MEDIUM\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "32");

                    setRenderDistance(10);
                }
                case HIGH -> {
                    setPhysicsModEnabled(true, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"HIGH\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "64");

                    setRenderDistance(12);
                }
                case ULTRA -> {
                    setPhysicsModEnabled(true, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"EXTREME\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "128");

                    setRenderDistance(14);
                }
                case TRAILER -> {
                    // Fragman Çekim Ayarları: Iteration Shader + Full Physics + DH 32 + RD 14
                    setShaderpack("iterationT 3.2.0.zip");
                    setPhysicsModEnabled(true, preset);

                    setTomlValue(ModPaths.DISTANT_HORIZONS, "presetQuality", "\"MEDIUM\"");
                    setTomlValue(ModPaths.DISTANT_HORIZONS, "lodChunkRenderDistanceRadius", "32");

                    setRenderDistance(10);
                }
                case CUSTOM -> {
                    return;
                }
            }

            Minecraft client = Minecraft.getInstance();
            if (client.levelRenderer != null) {
                client.levelRenderer.allChanged();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setRenderDistance(int chunks) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> {
            if (client.options != null) {
                client.options.renderDistance().set(chunks);
                client.options.save();
            }
        });
    }

    private static void setShaderpack(String shaderpackName) {
        try {
            Properties props = new Properties();
            if (Files.exists(ModPaths.IRIS_PROPERTIES)) {
                try (InputStream in = Files.newInputStream(ModPaths.IRIS_PROPERTIES)) {
                    props.load(in);
                }
            }
            props.setProperty("shaderPack", shaderpackName);
            props.setProperty("enableShaders", "true");

            try (OutputStream out = Files.newOutputStream(ModPaths.IRIS_PROPERTIES)) {
                props.store(out, null);
            }
        } catch (Exception ignored) {
        }
    }

    private static void setPhysicsModEnabled(boolean enabled, GraphicsPreset preset) {
        try {
            if (Files.exists(ModPaths.PHYSICS_CLIENT)) {
                String clientContent = Files.readString(ModPaths.PHYSICS_CLIENT);

                if (enabled) {
                    // Block Physics Type: 0 (Aktif)
                    clientContent = clientContent.replaceAll("\"blockSettings\":\\s*\\{\\s*\"Physics Type\":\\s*\\d+", "\"blockSettings\": {\n    \"Physics Type\": 0");

                    // Mob Physics Type: 1
                    clientContent = clientContent.replaceAll("\"mobSettings\":\\s*\\{\\s*\"Physics Type\":\\s*\\d+", "\"mobSettings\": {\n    \"Physics Type\": 1");

                    // Mob Animasyon Değeri
                    clientContent = clientContent.replaceAll("(\"mobSettings\":\\s*\\{[^}]*\"Animation\":\\s*)\\d+", "$11");

                    // Parçacık Ayarları (AÇIK)
                    clientContent = clientContent.replace("\"crackPhysicsParticles\": false", "\"crackPhysicsParticles\": true");
                    clientContent = clientContent.replace("\"eatingPhysicsParticles\": false", "\"eatingPhysicsParticles\": true");
                    clientContent = clientContent.replace("\"itemBreakPhysics\": false", "\"itemBreakPhysics\": true");
                    clientContent = clientContent.replace("\"sprintingPhysicsParticles\": false", "\"sprintingPhysicsParticles\": true");

                    // Sunucu Blok Parçacıkları (AÇIK)
                    clientContent = clientContent.replaceAll("\"serverBlockPhysicsParticles\":\\s*false", "\"serverBlockPhysicsParticles\": true");

                    // Diğer Fizik Efektleri (AÇIK)
                    clientContent = clientContent.replace("\"weatherParticles\": false", "\"weatherParticles\": true");
                    clientContent = clientContent.replace("\"vinePhysics\": false", "\"vinePhysics\": true");
                    clientContent = clientContent.replace("\"capePhysics\": false", "\"capePhysics\": true");
                    clientContent = clientContent.replace("\"bannerPhysics\": false", "\"bannerPhysics\": true");
                    clientContent = clientContent.replace("\"windPhysics\": false", "\"windPhysics\": true");
                } else {
                    // Physics Type: Kapalı (Blok: 3, Mob: 4)
                    clientContent = clientContent.replaceAll("\"blockSettings\":\\s*\\{\\s*\"Physics Type\":\\s*\\d+", "\"blockSettings\": {\n    \"Physics Type\": 3");
                    clientContent = clientContent.replaceAll("\"mobSettings\":\\s*\\{\\s*\"Physics Type\":\\s*\\d+", "\"mobSettings\": {\n    \"Physics Type\": 4");

                    // Parçacık Ayarları (KAPALI)
                    clientContent = clientContent.replace("\"crackPhysicsParticles\": true", "\"crackPhysicsParticles\": false");
                    clientContent = clientContent.replace("\"eatingPhysicsParticles\": true", "\"eatingPhysicsParticles\": false");
                    clientContent = clientContent.replace("\"itemBreakPhysics\": true", "\"itemBreakPhysics\": false");
                    clientContent = clientContent.replace("\"sprintingPhysicsParticles\": true", "\"sprintingPhysicsParticles\": false");

                    // Sunucu Blok Parçacıkları (VERY_LOW ve LOW için KAPALI)
                    clientContent = clientContent.replaceAll("\"serverBlockPhysicsParticles\":\\s*true", "\"serverBlockPhysicsParticles\": false");

                    // Diğer Fizik Efektleri (KAPALI)
                    clientContent = clientContent.replace("\"weatherParticles\": true", "\"weatherParticles\": false");
                    clientContent = clientContent.replace("\"vinePhysics\": true", "\"vinePhysics\": false");
                    clientContent = clientContent.replace("\"capePhysics\": true", "\"capePhysics\": false");
                    clientContent = clientContent.replace("\"bannerPhysics\": true", "\"bannerPhysics\": false");
                    clientContent = clientContent.replace("\"windPhysics\": true", "\"windPhysics\": false");
                }

                Files.writeString(ModPaths.PHYSICS_CLIENT, clientContent);
            }

            if (Files.exists(ModPaths.PHYSICS_BLOCKS)) {
                String blockContent = Files.readString(ModPaths.PHYSICS_BLOCKS);

                if (enabled) {
                    blockContent = blockContent.replace("\"blockPhysics\": false", "\"blockPhysics\": true");
                } else {
                    blockContent = blockContent.replace("\"blockPhysics\": true", "\"blockPhysics\": false");
                }

                // Stone ve taş türevi blokların Physics Type ayarı (VERY_LOW/LOW ise 4, diğerleri ise 2)[cite: 1]
                int stonePhysicsType = (preset == GraphicsPreset.VERY_LOW || preset == GraphicsPreset.LOW) ? 4 : 2;
                blockContent = blockContent.replaceAll(
                        "(\"identifier\":\\s*\"minecraft:stone[^\"]*\",\\s*\"Physics Type\":\\s*)\\d+",
                        "$1" + stonePhysicsType
                );

                Files.writeString(ModPaths.PHYSICS_BLOCKS, blockContent);
            }

        } catch (Exception ignored) {
        }
    }

    private static void setTomlValue(Path file, String key, String newValue) {
        try {
            if (!Files.exists(file)) return;
            List<String> lines = Files.readAllLines(file);
            Pattern pattern = Pattern.compile("^(\\s*" + Pattern.quote(key) + "\\s*=\\s*).*$");
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                Matcher m = pattern.matcher(lines.get(i));
                if (m.matches()) {
                    lines.set(i, m.group(1) + newValue);
                    found = true;
                    break;
                }
            }
            if (found) {
                Files.write(file, lines);
            }
        } catch (Exception ignored) {
        }
    }
}