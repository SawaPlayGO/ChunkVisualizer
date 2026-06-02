package ru.sawaplago.chunkVisualizer.managers;

import java.io.File;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {

    private final JavaPlugin plugin;
    private FileConfiguration config = null;
    private File configFile = null;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        reload();
    }

    public void reload() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "messages.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reload();
        }
        return config;
    }

    public String getMessage(String path) {
        String message = getConfig().getString(path);
        if (message == null) {
            plugin.getLogger()
                    .log(
                            Level.WARNING,
                            "Сообщение по пути '" + path + "' не найдено в messages.yml");
            return ChatColor.RED + "Missing message: " + path;
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить конфигурацию!", e);
        }
    }
}
