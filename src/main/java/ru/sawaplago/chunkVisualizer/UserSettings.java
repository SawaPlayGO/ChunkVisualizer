package ru.sawaplago.chunkVisualizer;

import org.bukkit.Material;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserSettings {
    private static final Map<UUID, Integer> heights = new HashMap<>();
    private static final Map<UUID, Boolean> enabled = new HashMap<>();
    private static final Map<UUID, Material> playerMaterials = new HashMap<>();

    private static int defaultHeight = 10;
    private static boolean defaultEnabled = true;
    private static Material defaultMaterial = Material.GLOWSTONE;

    public static void init(int height, boolean isEnabled, String materialName) {
        defaultHeight = height;
        defaultEnabled = isEnabled;

        // Исправленная логика валидации материала
        Material matched = Material.matchMaterial(materialName.toUpperCase());
        if (matched != null && matched.isBlock()) {
            defaultMaterial = matched;
        } else {
            defaultMaterial = Material.GLOWSTONE;
        }
    }

    public static void loadPlayerSettings(UUID uuid, Integer height, Boolean isEnabled, String materialName) {
        if (height != null) heights.put(uuid, height);
        if (isEnabled != null) enabled.put(uuid, isEnabled);

        if (materialName != null) {
            Material m = Material.matchMaterial(materialName.toUpperCase());
            if (m != null && m.isBlock()) playerMaterials.put(uuid, m);
        }
    }

    public static int getHeight(UUID uuid) {
        return heights.getOrDefault(uuid, defaultHeight);
    }

    public static void setHeight(UUID uuid, int height) {
        heights.put(uuid, height);
    }

    public static boolean isEnabled(UUID uuid) {
        return enabled.getOrDefault(uuid, defaultEnabled);
    }

    public static void toggle(UUID uuid) {
        enabled.put(uuid, !isEnabled(uuid));
    }

    public static Material getMaterial(UUID uuid) {
        return playerMaterials.getOrDefault(uuid, defaultMaterial);
    }

    public static void setMaterial(UUID uuid, Material material) {
        if (material != null && material.isBlock()) {
            playerMaterials.put(uuid, material);
        }
    }

    public static void removeData(UUID uuid) {
        heights.remove(uuid);
        enabled.remove(uuid);
        playerMaterials.remove(uuid);
    }
}