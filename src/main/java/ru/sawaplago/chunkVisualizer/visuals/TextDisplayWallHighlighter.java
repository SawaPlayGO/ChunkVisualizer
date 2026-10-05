package ru.sawaplago.chunkVisualizer.visuals;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.util.Quaternion4f;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.objects.WallColor;

/** Рисует 4 стенки чанка через text_display (только на клиенте игрока). */
public class TextDisplayWallHighlighter implements ChunkHighlighter {

    // Индексы метаданных Display / TextDisplay (1.20.2+)
    private static final int META_TRANSLATION = 11;
    private static final int META_SCALE = 12;
    private static final int META_LEFT_ROTATION = 13;
    private static final int META_RIGHT_ROTATION = 14;
    private static final int META_BILLBOARD = 15;
    private static final int META_VIEW_RANGE = 17;
    private static final int META_WIDTH = 20;
    private static final int META_HEIGHT = 21;
    private static final int META_TEXT = 23;
    private static final int META_BACKGROUND = 25;

    private static final byte BILLBOARD_FIXED = 0;
    private static final float VIEW_RANGE = 4f;
    private static final float WIDTH = 30f;
    private static final float HEIGHT = 319f;
    private static final Vector3f SCALE = new Vector3f(130f, 1020f, 1f);
    private static final Quaternion4f RIGHT_ROTATION = new Quaternion4f(0f, 0f, 0f, 1f);

    private static final AtomicInteger ID_HOLDER = new AtomicInteger(300_000_000);

    /** offsetX/offsetZ - место вызова относительно угла чанка (центр чанка = 8, 8). */
    private record Wall(
            double offsetX, double offsetZ, Vector3f translation, Quaternion4f leftRotation) {}

    private static final List<Wall> WALLS =
            List.of(
                    new Wall(
                            8.5,
                            6.5,
                            new Vector3f(7.5f, 0f, 0f),
                            new Quaternion4f(0f, -0.7071f, 0f, 0.7071f)),
                    new Wall(
                            9.5,
                            9.5,
                            new Vector3f(0f, 0f, 6.5f),
                            new Quaternion4f(0f, 1f, 0f, 0f)),
                    new Wall(
                            6.5,
                            9.5,
                            new Vector3f(-6.5f, 0f, 0f),
                            new Quaternion4f(0f, 0.7071f, 0f, 0.7071f)),
                    new Wall(
                            6.5,
                            6.5,
                            new Vector3f(0f, 0f, -6.5f),
                            new Quaternion4f(0f, 0f, 0f, 1f)));

    private final Chunk chunk;
    private final Player player;
    private final int backgroundColor;
    private final List<Integer> activeEntityIds = new ArrayList<>();

    public TextDisplayWallHighlighter(Chunk chunk, Player player, WallColor color, int alphaPercent) {
        this.chunk = chunk;
        this.player = player;
        WallColor safeColor = color != null ? color : WallColor.RED;
        this.backgroundColor = safeColor.toArgb(alphaPercent);
    }

    @Override
    public void show() {
        if (!activeEntityIds.isEmpty()) return;

        Vector start = chunk.getStartChunkPositionVector();
        double y = player.getWorld().getMinHeight();

        for (Wall wall : WALLS) {
            Location loc =
                    new Location(
                            start.getX() + wall.offsetX(), y, start.getZ() + wall.offsetZ(), 0, 0);
            activeEntityIds.add(spawnWall(loc, wall));
        }
    }

    private int spawnWall(Location loc, Wall wall) {
        int entityId = ID_HOLDER.getAndIncrement();
        UUID uuid = UUID.randomUUID();

        WrapperPlayServerSpawnEntity spawnPacket =
                new WrapperPlayServerSpawnEntity(
                        entityId, uuid, EntityTypes.TEXT_DISPLAY, loc, 0f, 0, null);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);

        List<EntityData<?>> meta = new ArrayList<>();
        meta.add(new EntityData<>(META_TRANSLATION, EntityDataTypes.VECTOR3F, wall.translation()));
        meta.add(new EntityData<>(META_SCALE, EntityDataTypes.VECTOR3F, SCALE));
        meta.add(
                new EntityData<>(
                        META_LEFT_ROTATION, EntityDataTypes.QUATERNION, wall.leftRotation()));
        meta.add(
                new EntityData<>(META_RIGHT_ROTATION, EntityDataTypes.QUATERNION, RIGHT_ROTATION));
        meta.add(new EntityData<>(META_BILLBOARD, EntityDataTypes.BYTE, BILLBOARD_FIXED));
        meta.add(new EntityData<>(META_VIEW_RANGE, EntityDataTypes.FLOAT, VIEW_RANGE));
        meta.add(new EntityData<>(META_WIDTH, EntityDataTypes.FLOAT, WIDTH));
        meta.add(new EntityData<>(META_HEIGHT, EntityDataTypes.FLOAT, HEIGHT));
        meta.add(new EntityData<>(META_TEXT, EntityDataTypes.ADV_COMPONENT, Component.text(" ")));
        meta.add(new EntityData<>(META_BACKGROUND, EntityDataTypes.INT, backgroundColor));

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(player, new WrapperPlayServerEntityMetadata(entityId, meta));

        return entityId;
    }

    @Override
    public void despawn() {
        if (activeEntityIds.isEmpty()) return;

        int[] ids = activeEntityIds.stream().mapToInt(Integer::intValue).toArray();
        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(player, new WrapperPlayServerDestroyEntities(ids));

        activeEntityIds.clear();
    }
}