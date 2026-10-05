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
    private static final int HEIGHT_SLOT = 11; // высота + toggle
    private static final int BLOCK_SLOT = 13; // материал
    private static final int BLOCK_GLOW_COLOR_SLOT = 15; // цвет обводки

    // Режим WALLS (четыре кнопки через одну)
    private static final int WALLS_STATUS_SLOT = 10; // toggle
    private static final int WALLS_COLOR_SLOT = 12; // цвет
    private static final int WALLS_ALPHA_SLOT = 14; // прозрачность
    private static final int WALLS_GLOW_SLOT = 16; // свечение

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

        boolean changed;
        if (slot == MODE_SLOT) {
            changed = handleModeClick(player, click, current, mode);
        } else if (mode == HighlightMode.BLOCKS) {
            changed = handleBlocksClick(player, slot, click, current);
        } else {
            changed = handleWallsClick(player, slot, click, current);
        }

        if (changed) {
            saveAndRefresh(player, current, inventory);
        }
    }

    private boolean handleModeClick(
            Player player, ClickType click, UserSettings current, HighlightMode mode) {
        if (!click.isLeftClick() && !click.isRightClick()) return false;

        HighlightMode target = mode.next();
        if (!target.canUse(player)) {
            player.sendMessage(messageManager.getMessage("no-permission-display"));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
            return false;
        }

        current.setMode(target);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1.5f);
        return true;
    }

    private boolean handleBlocksClick(
            Player player, int slot, ClickType click, UserSettings current) {
        boolean leftOrRight = click.isLeftClick() || click.isRightClick();

        switch (slot) {
            case HEIGHT_SLOT -> {
                if (!leftOrRight) return false;
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
                return true;
            }
            case BLOCK_SLOT -> {
                if (!click.isRightClick() || click.isShiftClick()) return false;
                current.setMaterial(configManager.getDefaultMaterial());
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1, 1);
                return true;
            }
            case BLOCK_GLOW_COLOR_SLOT -> {
                WallColor color = current.resolveBlockGlowColor();
                if (click.isLeftClick()) {
                    current.setBlockGlowColor(color.next());
                } else if (click.isRightClick()) {
                    current.setBlockGlowColor(color.previous());
                } else {
                    return false;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private boolean handleWallsClick(
            Player player, int slot, ClickType click, UserSettings current) {
        boolean leftOrRight = click.isLeftClick() || click.isRightClick();

        switch (slot) {
            case WALLS_STATUS_SLOT -> {
                if (!leftOrRight) return false;
                current.setEnabled(!current.isEnabled());
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);
                return true;
            }
            case WALLS_COLOR_SLOT -> {
                WallColor color =
                        current.getWallColor() != null ? current.getWallColor() : WallColor.RED;
                if (click.isLeftClick()) {
                    current.setWallColor(color.next());
                } else if (click.isRightClick()) {
                    current.setWallColor(color.previous());
                } else {
                    return false;
                }
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return true;
            }
            case WALLS_ALPHA_SLOT -> {
                int alpha = current.getWallAlpha();
                if (click.isLeftClick()) {
                    alpha = Math.max(WallColor.MIN_ALPHA, alpha - WallColor.ALPHA_STEP);
                } else if (click.isRightClick()) {
                    alpha = Math.min(WallColor.MAX_ALPHA, alpha + WallColor.ALPHA_STEP);
                } else {
                    return false;
                }
                current.setWallAlpha(alpha);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
                return true;
            }
            case WALLS_GLOW_SLOT -> {
                if (!leftOrRight) return false;
                current.setWallGlow(!current.isWallGlow());
                player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
                return true;
            }
            default -> {
                return false;
            }
        }
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
                    createItem(currentMaterial, messageManager.getMessage("gui.block-name"), lore));

            // --- Цвет обводки блоков ---
            WallColor glowColor = settings.resolveBlockGlowColor();
            String glowColorText = messageManager.getMessage("gui.colors." + glowColor.name());
            inventory.setItem(
                    BLOCK_GLOW_COLOR_SLOT,
                    createItem(
                            glowColor.getIcon(),
                            messageManager
                                    .getMessage("gui.block-glow-name")
                                    .replace("%color%", glowColorText),
                            List.of(
                                    messageManager.getMessage("gui.lore-color-current")
                                            + glowColorText,
                                    "",
                                    messageManager.getMessage("gui.lore-color-next"),
                                    messageManager.getMessage("gui.lore-color-prev"))));
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
                                    messageManager.getMessage("gui.lore-color-current") + colorText,
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

            // --- Свечение ---
            boolean glowOn = settings.isWallGlow();
            String glowText =
                    glowOn
                            ? messageManager.getMessage("gui.status-on")
                            : messageManager.getMessage("gui.status-off");
            inventory.setItem(
                    WALLS_GLOW_SLOT,
                    createItem(
                            glowOn ? Material.GLOW_INK_SAC : Material.INK_SAC,
                            messageManager
                                    .getMessage("gui.glow-name")
                                    .replace("%status%", glowText),
                            List.of(
                                    messageManager.getMessage("gui.lore-status") + glowText,
                                    "",
                                    messageManager.getMessage("gui.lore-glow-toggle"))));
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
