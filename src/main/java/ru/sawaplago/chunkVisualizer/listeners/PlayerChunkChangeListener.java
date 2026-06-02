package ru.sawaplago.chunkVisualizer.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.managers.UserSettingsManager;
import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.managers.DatabaseManager;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.visuals.ItemDisplayChunkHighlighter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;


public class PlayerChunkChangeListener implements Listener {
    private final DatabaseManager databaseManager;
    private final UserSettingsManager userSettingsManager;
    private final Map<UUID, ItemDisplayChunkHighlighter> activeHighlighters = new HashMap<>();

    public PlayerChunkChangeListener() {
        this.databaseManager = ChunkVisualizer.getInstance().getDatabaseManager();
        this.userSettingsManager = ChunkVisualizer.getInstance().getUserSettingsManager();
    }

    @EventHandler
    public void onPlayerChunkChange(PlayerChunkChangeEvent event) {
        Player p = event.getPlayer();
        Chunk chunkTo = event.getToChunk();


        ItemDisplayChunkHighlighter oldHighlighter = activeHighlighters.remove(p.getUniqueId());
        if (oldHighlighter != null) {
            oldHighlighter.despawn();
        }

        UserSettings userSettings = userSettingsManager.getSettings(p.getUniqueId());

        if (userSettings == null || !userSettings.isEnabled()) {
            return;
        }

        if (!userSettings.isEnabled()) {
            return;
        }

        ItemDisplayChunkHighlighter newHighlighter = new ItemDisplayChunkHighlighter(chunkTo, p, userSettings.getHeights(), userSettings.getMaterial());
        newHighlighter.show();
        activeHighlighters.put(p.getUniqueId(), newHighlighter);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Chunk currentChunk = Chunk.getCurrentChunk(player);

        Optional<UserSettings> userSettingsOpt = databaseManager.getUserSettings(player.getName());

        UserSettings userSettings;
        if (userSettingsOpt.isEmpty()) {
            userSettings = UserSettings.defaultSettings(player.getName());
            databaseManager.saveOrCreateUserSettings(userSettings);
            userSettingsManager.setSettings(player.getUniqueId(), userSettings);
        } else {
            userSettings = userSettingsOpt.get();
            userSettingsManager.setSettings(player.getUniqueId(), userSettings);
        }

        if (!userSettings.isEnabled()) {
            return;
        }

        ItemDisplayChunkHighlighter newHighlighter = new ItemDisplayChunkHighlighter(currentChunk, player, userSettings.getHeights(), userSettings.getMaterial());
        newHighlighter.show();
        activeHighlighters.put(player.getUniqueId(), newHighlighter);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ItemDisplayChunkHighlighter highlighter = activeHighlighters.remove(event.getPlayer().getUniqueId());
        userSettingsManager.removeSettings(event.getPlayer().getUniqueId());
        if (highlighter != null) {
            highlighter.despawn();
        }
    }
}
