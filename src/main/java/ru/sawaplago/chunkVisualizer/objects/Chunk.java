package ru.sawaplago.chunkVisualizer.objects;


import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sawaplago.chunkVisualizer.DevConfig;

import java.util.ArrayList;

public class Chunk {
    private final Vector startAngleVector;

    public Chunk(int startAngleX, int startAngleZ) {
        this.startAngleVector = new Vector(startAngleX, 0, startAngleZ);

    }

    public Vector getStartChunkPositionVector() {
        return this.startAngleVector;
    }

    public static Chunk getCurrentChunk(Player p) {
        int chunkXIndex = Math.floorDiv((int) Math.floor(p.getX()), DevConfig.CHUNK_SIZE);
        int chunkZIndex = Math.floorDiv((int) Math.floor(p.getZ()), DevConfig.CHUNK_SIZE);

        int startX = chunkXIndex * DevConfig.CHUNK_SIZE;
        int startZ = chunkZIndex * DevConfig.CHUNK_SIZE;

        return new Chunk(startX, startZ);
    }

    public ArrayList<Vector> getAngleChunk() {
        ArrayList<Vector> chunks = new ArrayList<>();
        Vector nwVector = new Vector(this.startAngleVector.getX(), 0, this.startAngleVector.getZ());
        Vector neVector = new Vector(this.startAngleVector.getX() + DevConfig.CHUNK_SIZE, 0, this.startAngleVector.getZ());
        Vector swVector = new Vector(this.startAngleVector.getX(), 0, this.startAngleVector.getZ() + DevConfig.CHUNK_SIZE);
        Vector seVector = new Vector(this.startAngleVector.getX() + DevConfig.CHUNK_SIZE, 0, this.startAngleVector.getZ() + DevConfig.CHUNK_SIZE);
        chunks.add(nwVector); chunks.add(neVector); chunks.add(swVector); chunks.add(seVector);
        return chunks;
    }

    public static ArrayList<Vector> getAngleChunk(Chunk chunk) {
        ArrayList<Vector> chunks = new ArrayList<>();
        Vector nwVector = new Vector(chunk.startAngleVector.getX(), 0, chunk.startAngleVector.getZ());
        Vector neVector = new Vector(chunk.startAngleVector.getX() + DevConfig.CHUNK_SIZE, 0, chunk.startAngleVector.getZ());
        Vector swVector = new Vector(chunk.startAngleVector.getX(), 0, chunk.startAngleVector.getZ() + DevConfig.CHUNK_SIZE);
        Vector seVector = new Vector(chunk.startAngleVector.getX() + DevConfig.CHUNK_SIZE, 0, chunk.startAngleVector.getZ() + DevConfig.CHUNK_SIZE);
        chunks.add(nwVector); chunks.add(neVector); chunks.add(swVector); chunks.add(seVector);
        return chunks;
    }

}
