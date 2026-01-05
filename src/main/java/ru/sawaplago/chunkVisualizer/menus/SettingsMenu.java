package ru.sawaplago.chunkVisualizer.menus;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.UserSettings;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

public class SettingsMenu {

    private final MessageManager mm;

    public SettingsMenu() {
        this.mm = ChunkVisualizer.getInstance().getMessageManager();
    }

    public void open(Player player) {
        ChestGui gui = new ChestGui(3, mm.getMessage("menu-title"));

        gui.setOnTopClick(event -> event.setCancelled(true));

        StaticPane pane = new StaticPane(0, 0, 9, 3);

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        pane.fillWith(glass);

        gui.setOnBottomClick(event -> {
            event.setCancelled(true);
            ItemStack clicked = event.getCurrentItem();

            if (clicked != null && clicked.getType().isBlock() && !clicked.getType().isAir()) {
                UserSettings.setMaterial(player.getUniqueId(), clicked.getType());
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

                refreshVisuals(player);
                updateMenu(player, pane, gui);
                gui.update();
            }
        });

        updateMenu(player, pane, gui);
        gui.addPane(pane);
        gui.show(player);
    }

    private void updateMenu(Player player, StaticPane pane, ChestGui gui) {
        UUID uuid = player.getUniqueId();
        int currentHeight = UserSettings.getHeight(uuid);
        boolean isEnabled = UserSettings.isEnabled(uuid);
        Material currentMaterial = UserSettings.getMaterial(uuid);

        String statusText = isEnabled ? mm.getMessage("gui.status-on") : mm.getMessage("gui.status-off");
        String heightName = mm.getMessage("gui.height-name").replace("%height%", String.valueOf(currentHeight));
        ItemStack heightItem = new ItemStack(isEnabled ? Material.LIME_GLAZED_TERRACOTTA : Material.RED_GLAZED_TERRACOTTA);
        ItemMeta hMeta = heightItem.getItemMeta();
        if (hMeta != null) {
            hMeta.setDisplayName(heightName);
            hMeta.setLore(List.of(
                    mm.getMessage("gui.lore-status") + statusText,
                    "",
                    mm.getMessage("gui.lore-lmb"),
                    mm.getMessage("gui.lore-rmb"),
                    mm.getMessage("gui.lore-shift")
            ));
            heightItem.setItemMeta(hMeta);
        }

        pane.addItem(new GuiItem(heightItem, event -> {
            if (event.isShiftClick()) {
                UserSettings.toggle(uuid);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            } else if (event.isLeftClick()) {
                UserSettings.setHeight(uuid, currentHeight - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            } else if (event.isRightClick()) {
                UserSettings.setHeight(uuid, currentHeight + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }

            refreshVisuals(player);
            updateMenu(player, pane, gui);
            gui.update();
        }), 3, 1);

        ItemStack blockItem = new ItemStack(currentMaterial);
        ItemMeta bMeta = blockItem.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(mm.getMessage("gui.block-name"));
            List<String> lore = mm.getConfig().getStringList("gui.block-lore").stream()
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("%material%", currentMaterial.name())))
                    .toList();
            bMeta.setLore(lore);
            blockItem.setItemMeta(bMeta);
        }

        pane.addItem(new GuiItem(blockItem, event -> {
            if (event.isRightClick()) {
                UserSettings.setMaterial(uuid, Material.GLOWSTONE);
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                refreshVisuals(player);
                updateMenu(player, pane, gui);
                gui.update();
            }
        }), 5, 1);
    }

    private void refreshVisuals(Player player) {
        Chunk current = Chunk.getCurrentChunk(player);
        Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player, current, current));
    }
}