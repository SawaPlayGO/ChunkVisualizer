package ru.sawaplago.chunkVisualizer.menus;

import java.util.List;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.events.PlayerChunkChangeEvent;
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;
import ru.sawaplago.chunkVisualizer.managers.DatabaseManager;
import ru.sawaplago.chunkVisualizer.managers.MessageManager;
import ru.sawaplago.chunkVisualizer.managers.UserSettingsManager;
import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;
import ru.sawaplago.chunkVisualizer.objects.Chunk;

public class SettingsMenu implements Listener {

    private static final int SIZE = 27;
    private static final int HEIGHT_SLOT = 12; // x=3, y=1
    private static final int BLOCK_SLOT = 14; // x=5, y=1

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

    /** Холдер нужен, чтобы listener отличал это меню от остальных инвентарей. */
    private static class MenuHolder implements InventoryHolder {
        private Inventory inventory;

        void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }

    public void open(Player player) {
        MenuHolder holder = new MenuHolder();
        Inventory inventory =
                Bukkit.createInventory(
                        holder,
                        SIZE,
                        LegacyComponentSerializer.legacySection()
                                .deserialize(messageManager.getMessage("menu-title")));
        holder.setInventory(inventory);

        render(player, inventory);
        player.openInventory(inventory);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) {
            return;
        }

        // Пока открыто это меню, отменяем любые клики
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        try {
            if (clickedInventory == holder.getInventory()) {
                handleTopClick(event, player, holder.getInventory());
            } else {
                handleBottomClick(event, player, holder.getInventory());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void handleTopClick(InventoryClickEvent event, Player player, Inventory inventory) {
        int slot = event.getSlot();
        ClickType click = event.getClick();

        if (slot == HEIGHT_SLOT) {
            UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
            if (current == null) return;

            if (click.isShiftClick()) {
                current.setEnabled(!current.isEnabled());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            } else if (click.isLeftClick()) {
                current.setHeights(current.getHeights() - 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            } else if (click.isRightClick()) {
                current.setHeights(current.getHeights() + 1);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            } else {
                return;
            }

            saveAndRefresh(player, current, inventory);
        } else if (slot == BLOCK_SLOT && click.isRightClick() && !click.isShiftClick()) {
            UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
            if (current == null) return;

            current.setMaterial(configManager.getDefaultMaterial());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);

            saveAndRefresh(player, current, inventory);
        }
    }

    private void handleBottomClick(InventoryClickEvent event, Player player, Inventory inventory) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.getType().isBlock() || clicked.getType().isAir()) {
            return;
        }

        UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
        if (current == null) return;

        current.setMaterial(clicked.getType());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

        saveAndRefresh(player, current, inventory);
    }

    private void saveAndRefresh(Player player, UserSettings settings, Inventory inventory) {
        databaseManager.saveOrCreateUserSettings(settings);
        userSettingsManager.setSettings(player.getUniqueId(), settings);

        refreshVisuals(player);
        render(player, inventory);
    }

    private void render(Player player, Inventory inventory) {
        UserSettings userSettings = userSettingsManager.getSettings(player.getUniqueId());
        if (userSettings == null) return;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, glass);
        }

        int currentHeight = userSettings.getHeights();
        boolean isEnabled = userSettings.isEnabled();
        Material currentMaterial = userSettings.getMaterial();

        String statusText =
                isEnabled
                        ? messageManager.getMessage("gui.status-on")
                        : messageManager.getMessage("gui.status-off");
        String heightName =
                messageManager
                        .getMessage("gui.height-name")
                        .replace("%height%", String.valueOf(currentHeight));

        ItemStack heightItem =
                new ItemStack(
                        isEnabled
                                ? Material.LIME_GLAZED_TERRACOTTA
                                : Material.RED_GLAZED_TERRACOTTA);
        ItemMeta hMeta = heightItem.getItemMeta();
        if (hMeta != null) {
            hMeta.setDisplayName(heightName);
            hMeta.setLore(
                    List.of(
                            messageManager.getMessage("gui.lore-status") + statusText,
                            "",
                            messageManager.getMessage("gui.lore-lmb"),
                            messageManager.getMessage("gui.lore-rmb"),
                            messageManager.getMessage("gui.lore-shift")));
            heightItem.setItemMeta(hMeta);
        }
        inventory.setItem(HEIGHT_SLOT, heightItem);

        ItemStack blockItem = new ItemStack(currentMaterial);
        ItemMeta bMeta = blockItem.getItemMeta();
        if (bMeta != null) {
            bMeta.setDisplayName(messageManager.getMessage("gui.block-name"));
            List<String> lore =
                    messageManager.getConfig().getStringList("gui.block-lore").stream()
                            .map(
                                    s ->
                                            ChatColor.translateAlternateColorCodes(
                                                    '&',
                                                    s.replace(
                                                            "%material%", currentMaterial.name())))
                            .toList();
            bMeta.setLore(lore);
            blockItem.setItemMeta(bMeta);
        }
        inventory.setItem(BLOCK_SLOT, blockItem);
    }

    private void refreshVisuals(Player player) {
        Chunk current = Chunk.getCurrentChunk(player);
        Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player, current, current));
    }
}
