package ru.sawaplago.chunkVisualizer.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.sawaplago.chunkVisualizer.UserSettings;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.visuals.ItemDisplayChunkHighlighter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class PlayerChunkChangeListener implements Listener {

    private final Map<UUID, ItemDisplayChunkHighlighter> activeHighlighters = new HashMap<>();

    @EventHandler
    public void onPlayerChunkChange(PlayerChunkChangeEvent event) {
        Player p = event.getPlayer();
        Chunk chunkTo = event.getToChunk();

        ItemDisplayChunkHighlighter oldHighlighter = activeHighlighters.remove(p.getUniqueId());
        if (oldHighlighter != null) {
            oldHighlighter.despawn();
        }

        if (!UserSettings.isEnabled(p.getUniqueId())) {
            return;
        }

        ItemDisplayChunkHighlighter newHighlighter = new ItemDisplayChunkHighlighter(chunkTo, p, UserSettings.getHeight(p.getUniqueId()), UserSettings.getMaterial(p.getUniqueId()));
        newHighlighter.show();

        activeHighlighters.put(p.getUniqueId(), newHighlighter);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Chunk currentChunk = Chunk.getCurrentChunk(player);

        if (!UserSettings.isEnabled(player.getUniqueId())) {
            return;
        }

        ItemDisplayChunkHighlighter newHighlighter = new ItemDisplayChunkHighlighter(currentChunk, player, UserSettings.getHeight(player.getUniqueId()),  UserSettings.getMaterial(player.getUniqueId()));
        newHighlighter.show();
        activeHighlighters.put(player.getUniqueId(), newHighlighter);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ItemDisplayChunkHighlighter highlighter = activeHighlighters.remove(event.getPlayer().getUniqueId());
        if (highlighter != null) {
            highlighter.despawn();
            UserSettings.removeData(event.getPlayer().getUniqueId());
        }
    }
}
