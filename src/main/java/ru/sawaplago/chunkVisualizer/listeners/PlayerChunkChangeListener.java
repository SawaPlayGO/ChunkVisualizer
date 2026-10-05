package ru.sawaplago.chunkVisualizer.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.managers.DatabaseManager;
import ru.sawaplago.chunkVisualizer.managers.UserSettingsManager;
import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.visuals.ChunkHighlighter;
import ru.sawaplago.chunkVisualizer.visuals.ItemDisplayChunkHighlighter;
import ru.sawaplago.chunkVisualizer.visuals.TextDisplayWallHighlighter;

public class PlayerChunkChangeListener implements Listener {
    private final DatabaseManager databaseManager;
    private final UserSettingsManager userSettingsManager;
    private final Map<UUID, ChunkHighlighter> activeHighlighters = new HashMap<>();

    public PlayerChunkChangeListener() {
        this.databaseManager = ChunkVisualizer.getInstance().getDatabaseManager();
        this.userSettingsManager = ChunkVisualizer.getInstance().getUserSettingsManager();
    }

    @EventHandler
    public void onPlayerChunkChange(PlayerChunkChangeEvent event) {
        Player p = event.getPlayer();
        removeHighlighter(p);
        showHighlighter(p, event.getToChunk());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Optional<UserSettings> userSettingsOpt = databaseManager.getUserSettings(player.getName());

        UserSettings userSettings;
        if (userSettingsOpt.isEmpty()) {
            userSettings = UserSettings.defaultSettings(player.getName());
            databaseManager.saveOrCreateUserSettings(userSettings);
        } else {
            userSettings = userSettingsOpt.get();
        }
        userSettingsManager.setSettings(player.getUniqueId(), userSettings);

        showHighlighter(player, Chunk.getCurrentChunk(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeHighlighter(event.getPlayer());
        userSettingsManager.removeSettings(event.getPlayer().getUniqueId());
    }

    private void removeHighlighter(Player player) {
        ChunkHighlighter old = activeHighlighters.remove(player.getUniqueId());
        if (old != null) {
            old.despawn();
        }
    }

    private void showHighlighter(Player player, Chunk chunk) {
        UserSettings settings = userSettingsManager.getSettings(player.getUniqueId());
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        ChunkHighlighter highlighter =
                switch (settings.getEffectiveMode(player)) {
                    case WALLS ->
                            new TextDisplayWallHighlighter(
                                    chunk,
                                    player,
                                    settings.getWallColor(),
                                    settings.getWallAlpha());
                    case BLOCKS ->
                            new ItemDisplayChunkHighlighter(
                                    chunk, player, settings.getHeights(), settings.getMaterial());
                };

        highlighter.show();
        activeHighlighters.put(player.getUniqueId(), highlighter);
    }
}