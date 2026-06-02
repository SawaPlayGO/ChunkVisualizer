package ru.sawaplago.chunkVisualizer.managers;

import java.io.File;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

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
        return Material.getMaterial(materialName);
    }
}
