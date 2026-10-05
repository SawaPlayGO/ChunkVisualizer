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
import ru.sawaplago.chunkVisualizer.objects.HighlightMode;
import ru.sawaplago.chunkVisualizer.objects.WallColor;

public class SettingsMenu implements Listener {

    private static final int SIZE = 27;

    // Общий слот: левый нижний угол
    private static final int MODE_SLOT = 18;

    // Режим BLOCKS
    private static final int HEIGHT_SLOT = 12; // высота + toggle
    private static final int BLOCK_SLOT = 14; // материал

    // Режим WALLS (ровная тройка по центру)
    private static final int WALLS_STATUS_SLOT = 11; // toggle
    private static final int WALLS_COLOR_SLOT = 13; // цвет
    private static final int WALLS_ALPHA_SLOT = 15; // прозрачность

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

        UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
        if (current == null) return;

        HighlightMode mode = current.getEffectiveMode(player);
        boolean leftOrRight = click.isLeftClick() || click.isRightClick();

        switch (slot) {
            case MODE_SLOT -> {
                if (!leftOrRight) return;
                HighlightMode target = mode.next();
                if (!target.canUse(player)) {
                    player.sendMessage(messageManager.getMessage("no-permission-display"));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                    return;
                }
                current.setMode(target);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
            }
            case HEIGHT_SLOT -> {
                if (mode != HighlightMode.BLOCKS || !leftOrRight) return;
                if (click.isShiftClick()) {
                    current.setEnabled(!current.isEnabled());
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                } else if (click.isLeftClick()) {
                    current.setHeights(current.getHeights() - 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                } else {
                    current.setHeights(current.getHeights() + 1);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                }
            }
            case BLOCK_SLOT -> {
                if (mode != HighlightMode.BLOCKS) return;
                if (!click.isRightClick() || click.isShiftClick()) return;
                current.setMaterial(configManager.getDefaultMaterial());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
            }
            case WALLS_STATUS_SLOT -> {
                if (mode != HighlightMode.WALLS || !leftOrRight) return;
                current.setEnabled(!current.isEnabled());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
            }
            case WALLS_COLOR_SLOT -> {
                if (mode != HighlightMode.WALLS) return;
                WallColor color =
                        current.getWallColor() != null ? current.getWallColor() : WallColor.RED;
                if (click.isLeftClick()) {
                    current.setWallColor(color.next());
                } else if (click.isRightClick()) {
                    current.setWallColor(color.previous());
                } else {
                    return;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            case WALLS_ALPHA_SLOT -> {
                if (mode != HighlightMode.WALLS) return;
                int alpha = current.getWallAlpha();
                if (click.isLeftClick()) {
                    alpha = Math.max(WallColor.MIN_ALPHA, alpha - WallColor.ALPHA_STEP);
                } else if (click.isRightClick()) {
                    alpha = Math.min(WallColor.MAX_ALPHA, alpha + WallColor.ALPHA_STEP);
                } else {
                    return;
                }
                current.setWallAlpha(alpha);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
            }
            default -> {
                return;
            }
        }

        saveAndRefresh(player, current, inventory);
    }

    private void handleBottomClick(InventoryClickEvent event, Player player, Inventory inventory) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.getType().isBlock() || clicked.getType().isAir()) {
            return;
        }

        UserSettings current = userSettingsManager.getSettings(player.getUniqueId());
        if (current == null) return;

        // Материал имеет смысл менять только в режиме блоков
        if (current.getEffectiveMode(player) != HighlightMode.BLOCKS) return;

        current.setMaterial(clicked.getType());
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1, 1);

        saveAndRefresh(player, current, inventory);
    }

    private void saveAndRefresh(Player player, UserSettings settings, Inventory inventory) {
        databaseManager.saveOrCreateUserSettings(settings);
        userSettingsManager.setSettings(player.getUniqueId(), settings);

        // Сразу перерисовываем подсветку, не дожидаясь смены чанка
        refreshVisuals(player);
        render(player, inventory);
    }

    private void render(Player player, Inventory inventory) {
        UserSettings settings = userSettingsManager.getSettings(player.getUniqueId());
        if (settings == null) return;

        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glass.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glass.setItemMeta(glassMeta);
        }
        for (int i = 0; i < SIZE; i++) {
            inventory.setItem(i, glass);
        }

        HighlightMode mode = settings.getEffectiveMode(player);
        boolean isEnabled = settings.isEnabled();

        String statusText =
                isEnabled
                        ? messageManager.getMessage("gui.status-on")
                        : messageManager.getMessage("gui.status-off");

        // --- Режим (левый нижний угол) ---
        String modeText =
                messageManager.getMessage(
                        mode == HighlightMode.WALLS ? "gui.mode-walls" : "gui.mode-blocks");
        inventory.setItem(
                MODE_SLOT,
                createItem(
                        mode == HighlightMode.WALLS ? Material.GLASS : Material.GLOWSTONE,
                        messageManager.getMessage("gui.mode-name").replace("%mode%", modeText),
                        List.of(
                                messageManager.getMessage("gui.lore-mode-current") + modeText,
                                "",
                                messageManager.getMessage("gui.lore-mode-switch"))));

        Material statusIcon =
                isEnabled ? Material.LIME_GLAZED_TERRACOTTA : Material.RED_GLAZED_TERRACOTTA;

        if (mode == HighlightMode.BLOCKS) {
            // --- Высота + toggle ---
            inventory.setItem(
                    HEIGHT_SLOT,
                    createItem(
                            statusIcon,
                            messageManager
                                    .getMessage("gui.height-name")
                                    .replace("%height%", String.valueOf(settings.getHeights())),
                            List.of(
                                    messageManager.getMessage("gui.lore-status") + statusText,
                                    "",
                                    messageManager.getMessage("gui.lore-lmb"),
                                    messageManager.getMessage("gui.lore-rmb"),
                                    messageManager.getMessage("gui.lore-shift"))));

            // --- Материал ---
            Material currentMaterial = settings.getMaterial();
            List<String> lore =
                    messageManager.getConfig().getStringList("gui.block-lore").stream()
                            .map(
                                    s ->
                                            ChatColor.translateAlternateColorCodes(
                                                    '&',
                                                    s.replace(
                                                            "%material%", currentMaterial.name())))
                            .toList();
            inventory.setItem(
                    BLOCK_SLOT,
                    createItem(
                            currentMaterial, messageManager.getMessage("gui.block-name"), lore));
        } else {
            // --- Toggle ---
            inventory.setItem(
                    WALLS_STATUS_SLOT,
                    createItem(
                            statusIcon,
                            messageManager
                                    .getMessage("gui.status-name")
                                    .replace("%status%", statusText),
                            List.of(
                                    messageManager.getMessage("gui.lore-status") + statusText,
                                    "",
                                    messageManager.getMessage("gui.lore-toggle"))));

            // --- Цвет ---
            WallColor color =
                    settings.getWallColor() != null ? settings.getWallColor() : WallColor.RED;
            String colorText = messageManager.getMessage("gui.colors." + color.name());
            inventory.setItem(
                    WALLS_COLOR_SLOT,
                    createItem(
                            color.getIcon(),
                            messageManager
                                    .getMessage("gui.color-name")
                                    .replace("%color%", colorText),
                            List.of(
                                    messageManager.getMessage("gui.lore-color-current")
                                            + colorText,
                                    "",
                                    messageManager.getMessage("gui.lore-color-next"),
                                    messageManager.getMessage("gui.lore-color-prev"))));

            // --- Прозрачность ---
            inventory.setItem(
                    WALLS_ALPHA_SLOT,
                    createItem(
                            Material.GLASS_PANE,
                            messageManager
                                    .getMessage("gui.alpha-name")
                                    .replace("%alpha%", String.valueOf(settings.getWallAlpha())),
                            List.of(
                                    messageManager.getMessage("gui.lore-alpha-lmb"),
                                    messageManager.getMessage("gui.lore-alpha-rmb"))));
        }
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void refreshVisuals(Player player) {
        Chunk current = Chunk.getCurrentChunk(player);
        Bukkit.getPluginManager().callEvent(new PlayerChunkChangeEvent(player, current, current));
    }
}