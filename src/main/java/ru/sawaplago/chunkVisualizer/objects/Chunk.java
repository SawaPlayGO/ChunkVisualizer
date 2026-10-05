package ru.sawaplago.chunkVisualizer.objects;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Chunk {
    private final Vector startAngleVector;
    private static final int CHUNK_SIZE = 16;

    public Chunk(int startAngleX, int startAngleZ) {
        this.startAngleVector = new Vector(startAngleX, 0, startAngleZ);
    }

    public Vector getStartChunkPositionVector() {
        return this.startAngleVector;
    }

    public ArrayList<Vector> getAngleChunk() {
        ArrayList<Vector> chunks = new ArrayList<>();
        Vector nwVector = new Vector(this.startAngleVector.getX(), 0, this.startAngleVector.getZ());
        Vector neVector =
                new Vector(
                        this.startAngleVector.getX() + CHUNK_SIZE, 0, this.startAngleVector.getZ());
        Vector swVector =
                new Vector(
                        this.startAngleVector.getX(), 0, this.startAngleVector.getZ() + CHUNK_SIZE);
        Vector seVector =
                new Vector(
                        this.startAngleVector.getX() + CHUNK_SIZE,
                        0,
                        this.startAngleVector.getZ() + CHUNK_SIZE);
        chunks.add(nwVector);
        chunks.add(neVector);
        chunks.add(swVector);
        chunks.add(seVector);
        return chunks;
    }

    public static Chunk getCurrentChunk(Player p) {
        return getCurrentChunk(p.getLocation());
    }

    public static Chunk getCurrentChunk(Location location) {
        int chunkXIndex = Math.floorDiv(location.getBlockX(), CHUNK_SIZE);
        int chunkZIndex = Math.floorDiv(location.getBlockZ(), CHUNK_SIZE);

        return new Chunk(chunkXIndex * CHUNK_SIZE, chunkZIndex * CHUNK_SIZE);
    }

    public static ArrayList<Vector> getAngleChunk(Chunk chunk) {
        return chunk.getAngleChunk();
    }
}
