package ru.sawaplago.chunkVisualizer.visuals;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.world.Location;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import ru.sawaplago.chunkVisualizer.objects.Chunk;

public class ItemDisplayChunkHighlighter {
    private static final byte GLOWING_FLAG = 0x40;
    private static final byte INVISIBLE_FLAG = 0x20;
    private static final int METADATA_ITEM_INDEX = 23;
    private static final int METADATA_DISPLAY_TYPE_INDEX = 24;
    private static final byte DISPLAY_TYPE_HEAD = 5;

    private static final AtomicInteger ID_HOLDER = new AtomicInteger(200_000_000);

    private final Chunk chunk;
    private final Player player;
    private final List<Integer> activeEntityIds = new ArrayList<>();
    private final int height;
    private final Material material;

    public ItemDisplayChunkHighlighter(Chunk chunk, Player player, int height, Material material) {
        this.material = material;
        this.chunk = chunk;
        this.player = player;
        this.height = height;
    }

    public void show() {
        if (!activeEntityIds.isEmpty()) return;

        List<Vector> angles = chunk.getAngleChunk();
        for (Vector vector : angles) {
            Location loc =
                    new Location(
                            vector.getX(),
                            player.getLocation().getY() + height,
                            vector.getZ(),
                            0,
                            0);
            int id = spawnSinglePoint(loc);
            activeEntityIds.add(id);
        }
    }

    private int spawnSinglePoint(Location loc) {
        int entityId = ID_HOLDER.getAndIncrement();
        UUID uuid = UUID.randomUUID();

        WrapperPlayServerSpawnEntity spawnPacket =
                new WrapperPlayServerSpawnEntity(
                        entityId, uuid, EntityTypes.ITEM_DISPLAY, loc, 0f, 0, null);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);

        List<EntityData<?>> metaList = new ArrayList<>();
        metaList.add(
                new EntityData<>(0, EntityDataTypes.BYTE, (byte) (GLOWING_FLAG | INVISIBLE_FLAG)));

        String materialName = this.material.getKey().toString();
        ItemType type = ItemTypes.getByName(materialName);

        if (type == null) {
            type = ItemTypes.GLOWSTONE;
        }

        ItemStack displayItem = ItemStack.builder().type(type).amount(1).build();
        metaList.add(new EntityData<>(METADATA_ITEM_INDEX, EntityDataTypes.ITEMSTACK, displayItem));
        metaList.add(
                new EntityData<>(
                        METADATA_DISPLAY_TYPE_INDEX, EntityDataTypes.BYTE, DISPLAY_TYPE_HEAD));

        WrapperPlayServerEntityMetadata metadataPacket =
                new WrapperPlayServerEntityMetadata(entityId, metaList);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, metadataPacket);

        return entityId;
    }

    public void despawn() {
        if (activeEntityIds.isEmpty()) return;

        int[] idsArray = activeEntityIds.stream().mapToInt(Integer::intValue).toArray();
        WrapperPlayServerDestroyEntities destroyPacket =
                new WrapperPlayServerDestroyEntities(idsArray);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, destroyPacket);

        activeEntityIds.clear();
    }
}
