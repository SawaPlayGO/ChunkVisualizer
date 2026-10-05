package ru.sawaplago.chunkVisualizer.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.objects.Chunk;

public class ChunkChangeListener implements Listener {

    /** Задержка, чтобы игрок реально оказался на новой позиции/в новом мире. */
    private static final long REFRESH_DELAY_TICKS = 2L;

    private final Map<UUID, Chunk> lastChunk = new HashMap<>();

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() == null) return;

        // Внутри события игрок ещё стоит в точке "from", поэтому берём "to"
        Chunk currentChunk = Chunk.getCurrentChunk(e.getTo());
        Chunk previousChunk = lastChunk.get(p.getUniqueId());
        if (previousChunk == null) {
            lastChunk.put(p.getUniqueId(), currentChunk);
            return;
        }
        if (!currentChunk
                .getStartChunkPositionVector()
                .equals(previousChunk.getStartChunkPositionVector())) {
            lastChunk.put(p.getUniqueId(), currentChunk);
            Bukkit.getPluginManager()
                    .callEvent(new PlayerChunkChangeEvent(p, previousChunk, currentChunk));
        }
    }

    // PlayerTeleportEvent НЕ приходит в хендлер PlayerMoveEvent (у него свой HandlerList)
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        scheduleRefresh(e.getPlayer());
    }

    // После респавна клиент очищает сущности
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        scheduleRefresh(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastChunk.remove(e.getPlayer().getUniqueId());
    }

    private void scheduleRefresh(Player player) {
        Bukkit.getScheduler()
                .runTaskLater(
                        ChunkVisualizer.getInstance(),
                        () -> {
                            if (!player.isOnline()) return;
                            Chunk chunk = Chunk.getCurrentChunk(player);
                            lastChunk.put(player.getUniqueId(), chunk);
                            Bukkit.getPluginManager()
                                    .callEvent(new PlayerChunkChangeEvent(player, chunk, chunk));
                        },
                        REFRESH_DELAY_TICKS);
    }
}
