package ru.sawaplago.chunkVisualizer.managers;

import java.io.File;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sawaplago.chunkVisualizer.objects.HighlightMode;
import ru.sawaplago.chunkVisualizer.objects.WallColor;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File file;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (file == null) file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) plugin.saveResource("config.yml", false);
        config = YamlConfiguration.loadConfiguration(file);
    }

    public int getDefaultHeight() {
        return config.getInt("settings.default-height", 10);
    }

    public boolean isDefaultIsEnabled() {
        return config.getBoolean("settings.default-enabled", true);
    }

    public Material getDefaultMaterial() {
        String materialName = config.getString("settings.default-material", "GLOWSTONE");
        Material material = Material.getMaterial(materialName);
        return material != null ? material : Material.GLOWSTONE;
    }

    public WallColor getDefaultBlockGlowColor() {
        return WallColor.fromString(
                config.getString("settings.default-block-glow-color", "WHITE"), WallColor.WHITE);
    }

    public HighlightMode getDefaultMode() {
        return HighlightMode.fromString(
                config.getString("settings.default-mode", "BLOCKS"), HighlightMode.BLOCKS);
    }

    public WallColor getDefaultWallColor() {
        return WallColor.fromString(
                config.getString("settings.default-wall-color", "RED"), WallColor.RED);
    }

    public int getDefaultWallAlpha() {
        int alpha = config.getInt("settings.default-wall-alpha", 50);
        return Math.max(WallColor.MIN_ALPHA, Math.min(WallColor.MAX_ALPHA, alpha));
    }

    public boolean isDefaultWallGlow() {
        return config.getBoolean("settings.default-wall-glow", false);
    }
}
