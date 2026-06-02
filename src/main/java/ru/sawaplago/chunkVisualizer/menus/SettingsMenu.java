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
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;
import ru.sawaplago.chunkVisualizer.managers.DatabaseManager;
import ru.sawaplago.chunkVisualizer.managers.UserSettingsManager;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;
import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;
import ru.sawaplago.chunkVisualizer.objects.Chunk;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import org.bukkit.Bukkit;

import java.util.List;

public class SettingsMenu {

    private final MessageManager messageManager;
    private final UserSettingsManager userSettingsManager;
    private final DatabaseManager databaseManager;
    private final ConfigManager configManager;

    public SettingsMenu() {
        this.configManager = ChunkVisualizer.getInstance().getConfigManager();
        this.userSettingsManager = ChunkVisualizer.getInstance().getUserSettingsManager();
        this.databaseManager = ChunkVisualizer.getInstance().getDatabaseManager();
        this.messageManager = ChunkVisualizer.getInstance().getMessageManager();
    }

    public void open(Player player) {
        ChestGui gui = new ChestGui(3, messageManager.getMessage("menu-title"));

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
                UserSettings userSettings = userSettingsManager.getSettings(player.getUniqueId());
                if (userSettings == null) return;

                userSettings.setMaterial(clicked.getType());
                databaseManager.saveOrCreateUserSettings(userSettings);
                userSettingsManager.setSettings(player.getUniqueId(), userSettings);
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
        UserSettings userSettings = userSettingsManager.getSettings(player.getUniqueId());
        if (userSettings == null) return;

        int currentHeight = userSettings.getHeights();
        boolean isEnabled = userSettings.isEnabled();
        Material currentMaterial = userSettings.getMaterial();

        String statusText = isEnabled ? messageManager.getMessage("gui.status-on") : messageManager.getMessage("gui.status-off");
        String heightName = messageManager.getMessage("gui.height-name").replace("%height%", String.valueOf(currentHeight));
        ItemStack heightItem = new ItemStack(isEnabled ? Material.LIME_GLAZED_TERRACOTTA : Material.RED_GLAZED_TERRACOTTA);
        ItemMeta hMeta = heightItem.getItemMeta();
        if (hMeta != null) {
            hMeta.setDisplayName(heightName);
            hMeta.setLore(List.of(
                    messageManager.getMessage("gui.lore-status") + statusText,
                    "",
                    messageManager.getMessage("gui.lore-lmb"),
                    messageManager.getMessage("gui.lore-rmb"),
                    messageManager.getMessage("gui.lore-shift")
            ));
            heightItem.setItemMeta(hMeta);
        }

        pane.addItem(new GuiItem(heightItem, event -> {
            UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
            if (current == null) return;

            if (event.isShiftClick()) {
                current.setEnabled(!current.isEnabled());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            } else if (event.isLeftClick()) {
                current.setHeights(current.getHeights() - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            } else if (event.isRightClick()) {
                current.setHeights(current.getHeights() + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }

            databaseManager.saveOrCreateUserSettings(current);
            userSettingsManager.setSettings(player.getUniqueId(), current);

            refreshVisuals(player);
            updateMenu(player, pane, gui);
            gui.update();
        }), 3, 1);

        ItemStack blockItem = new ItemStack(currentMaterial);
        ItemMeta bMeta = blockItem.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(messageManager.getMessage("gui.block-name"));
            List<String> lore = messageManager.getConfig().getStringList("gui.block-lore").stream()
                    .map(s -> ChatColor.translateAlternateColorCodes('&', s.replace("%material%", currentMaterial.name())))
                    .toList();
            bMeta.setLore(lore);
            blockItem.setItemMeta(bMeta);
        }

        pane.addItem(new GuiItem(blockItem, event -> {
            if (event.isRightClick()) {
                UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
                if (current == null) return;

                current.setMaterial(configManager.getDefaultMaterial());
                databaseManager.saveOrCreateUserSettings(current);
                userSettingsManager.setSettings(player.getUniqueId(), current);
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