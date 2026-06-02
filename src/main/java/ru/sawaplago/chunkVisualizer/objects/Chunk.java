package ru.sawaplago.chunkVisualizer.objects;

import java.util.ArrayList;
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
        int chunkXIndex = Math.floorDiv((int) Math.floor(p.getX()), CHUNK_SIZE);
        int chunkZIndex = Math.floorDiv((int) Math.floor(p.getZ()), CHUNK_SIZE);

        int startX = chunkXIndex * CHUNK_SIZE;
        int startZ = chunkZIndex * CHUNK_SIZE;

        return new Chunk(startX, startZ);
    }

    public static ArrayList<Vector> getAngleChunk(Chunk chunk) {
        ArrayList<Vector> chunks = new ArrayList<>();
        Vector nwVector =
                new Vector(chunk.startAngleVector.getX(), 0, chunk.startAngleVector.getZ());
        Vector neVector =
                new Vector(
                        chunk.startAngleVector.getX() + CHUNK_SIZE,
                        0,
                        chunk.startAngleVector.getZ());
        Vector swVector =
                new Vector(
                        chunk.startAngleVector.getX(),
                        0,
                        chunk.startAngleVector.getZ() + CHUNK_SIZE);
        Vector seVector =
                new Vector(
                        chunk.startAngleVector.getX() + CHUNK_SIZE,
                        0,
                        chunk.startAngleVector.getZ() + CHUNK_SIZE);
        chunks.add(nwVector);
        chunks.add(neVector);
        chunks.add(swVector);
        chunks.add(seVector);
        return chunks;
    }
}
