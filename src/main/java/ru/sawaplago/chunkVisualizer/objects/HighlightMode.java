package ru.sawaplago.chunkVisualizer.objects;

import org.bukkit.entity.Player;

public enum HighlightMode {
    BLOCKS(null),
    WALLS("chunkvisualizer.use.display");

    private final String permission;

    HighlightMode(String permission) {
        this.permission = permission;
    }

    public boolean canUse(Player player) {
        return permission == null || player.hasPermission(permission);
    }

    public HighlightMode next() {
        HighlightMode[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public static HighlightMode fromString(String name, HighlightMode fallback) {
        if (name == null) return fallback;
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
