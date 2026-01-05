package ru.sawaplago.chunkVisualizer;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import ru.sawaplago.chunkVisualizer.commands.CVCommand;
import ru.sawaplago.chunkVisualizer.listeners.ChunkChangeListener;
import ru.sawaplago.chunkVisualizer.listeners.PlayerChunkChangeListener;
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;

public final class ChunkVisualizer extends JavaPlugin {

    private static ChunkVisualizer instance;
    private MessageManager messageManager;
    private ConfigManager configManager;

    public static ChunkVisualizer getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
    public ConfigManager getConfigManager() { return configManager; }

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.messageManager = new MessageManager(this);
        this.configManager = new ConfigManager(this);

        UserSettings.init(
            configManager.getDefaultHeight(),
            configManager.isDefaultEnabled(),
            configManager.getDefaultMaterial()
        );

        CVCommand cvCommand = new CVCommand();
        getCommand("cv").setExecutor(cvCommand);
        getCommand("cv").setTabCompleter(cvCommand);

        getServer().getPluginManager().registerEvents(new ChunkChangeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerChunkChangeListener(), this);

        PacketEvents.getAPI().init();
        getLogger().info("ChunkVisualizerPlugin enabled!");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        getLogger().info("ChunkVisualizerPlugin disabled!");
    }
}
