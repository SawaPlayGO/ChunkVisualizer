package ru.sawaplago.chunkVisualizer.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.objects.Chunk;

import java.util.HashMap;
import java.util.Map;

public class ChunkChangeListener implements Listener {

    private final Map<Player, Chunk> lastChunk = new HashMap<>();

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Chunk currentChunk = Chunk.getCurrentChunk(p);
        Chunk previousChunk = lastChunk.get(p);
        if (previousChunk == null) {
            lastChunk.put(p, currentChunk);
            return;
        }
        if (!currentChunk.getStartChunkPositionVector().equals(previousChunk.getStartChunkPositionVector())) {
            PlayerChunkChangeEvent event = new PlayerChunkChangeEvent(p, previousChunk, currentChunk);
            Bukkit.getPluginManager().callEvent(event);
            lastChunk.put(p, currentChunk);
        }
    }
}
