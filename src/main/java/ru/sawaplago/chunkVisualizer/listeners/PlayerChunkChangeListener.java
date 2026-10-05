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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
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
    /** Через сколько тиков показываем подсветку, даже если чанк так и не отправлен. */
    private static final int MAX_WAIT_TICKS = 100;

    private static final long CHECK_PERIOD_TICKS = 2L;

    private final DatabaseManager databaseManager;
    private final UserSettingsManager userSettingsManager;
    private final Map<UUID, ChunkHighlighter> activeHighlighters = new HashMap<>();
    private final Map<UUID, BukkitTask> pendingTasks = new HashMap<>();

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
        cancelPending(event.getPlayer());
        removeHighlighter(event.getPlayer());
        userSettingsManager.removeSettings(event.getPlayer().getUniqueId());
    }

    private void removeHighlighter(Player player) {
        cancelPending(player);
        ChunkHighlighter old = activeHighlighters.remove(player.getUniqueId());
        if (old != null) {
            old.despawn();
        }
    }

    private void cancelPending(Player player) {
        BukkitTask task = pendingTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void showHighlighter(Player player, Chunk chunk) {
        cancelPending(player);

        UserSettings settings = userSettingsManager.getSettings(player.getUniqueId());
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        // Клиент выбрасывает спавн сущностей в незагруженном чанке,
        // поэтому ждём, пока сервер отправит игроку этот чанк.
        if (isChunkReady(player, chunk)) {
            spawnHighlighter(player, chunk);
            return;
        }

        UUID uuid = player.getUniqueId();
        BukkitRunnable runnable =
                new BukkitRunnable() {
                    private int waited = 0;

                    @Override
                    public void run() {
                        waited += (int) CHECK_PERIOD_TICKS;

                        if (!player.isOnline()) {
                            pendingTasks.remove(uuid);
                            cancel();
                            return;
                        }

                        if (isChunkReady(player, chunk) || waited >= MAX_WAIT_TICKS) {
                            pendingTasks.remove(uuid);
                            cancel();
                            spawnHighlighter(player, chunk);
                        }
                    }
                };
        pendingTasks.put(
                uuid,
                runnable.runTaskTimer(
                        ChunkVisualizer.getInstance(), CHECK_PERIOD_TICKS, CHECK_PERIOD_TICKS));
    }

    private boolean isChunkReady(Player player, Chunk chunk) {
        Vector start = chunk.getStartChunkPositionVector();
        int chunkX = start.getBlockX() >> 4;
        int chunkZ = start.getBlockZ() >> 4;
        return player.isChunkSent(org.bukkit.Chunk.getChunkKey(chunkX, chunkZ));
    }

    private void spawnHighlighter(Player player, Chunk chunk) {
        // Настройки могли поменяться, пока ждали
        UserSettings settings = userSettingsManager.getSettings(player.getUniqueId());
        if (settings == null || !settings.isEnabled()) {
            return;
        }

        ChunkHighlighter old = activeHighlighters.remove(player.getUniqueId());
        if (old != null) {
            old.despawn();
        }

        ChunkHighlighter highlighter =
                switch (settings.getEffectiveMode(player)) {
                    case WALLS ->
                            new TextDisplayWallHighlighter(
                                    chunk,
                                    player,
                                    settings.getWallColor(),
                                    settings.getWallAlpha(),
                                    settings.isWallGlow());
                    case BLOCKS ->
                            new ItemDisplayChunkHighlighter(
                                    chunk,
                                    player,
                                    settings.getHeights(),
                                    settings.getMaterial(),
                                    settings.resolveBlockGlowColor());
                };

        highlighter.show();
        activeHighlighters.put(player.getUniqueId(), highlighter);
    }
}