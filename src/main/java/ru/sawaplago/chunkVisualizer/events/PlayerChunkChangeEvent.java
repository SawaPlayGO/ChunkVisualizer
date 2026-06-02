package ru.sawaplago.chunkVisualizer.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.sawaplago.chunkVisualizer.objects.Chunk;

public class PlayerChunkChangeEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Chunk fromChunk;
    private final Chunk toChunk;

    public PlayerChunkChangeEvent(Player player, Chunk fromChunk, Chunk toChunk) {
        this.player = player;
        this.fromChunk = fromChunk;
        this.toChunk = toChunk;
    }

    public Player getPlayer() {
        return player;
    }

    public Chunk getToChunk() {
        return toChunk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
