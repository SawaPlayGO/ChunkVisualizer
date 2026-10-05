package ru.sawaplago.chunkVisualizer.objects;

import org.bukkit.Material;

public enum WallColor {
    RED(0xFF0000, Material.RED_STAINED_GLASS_PANE),
    ORANGE(0xFF8000, Material.ORANGE_STAINED_GLASS_PANE),
    YELLOW(0xFFFF00, Material.YELLOW_STAINED_GLASS_PANE),
    GREEN(0x00FF00, Material.LIME_STAINED_GLASS_PANE),
    CYAN(0x00FFFF, Material.CYAN_STAINED_GLASS_PANE),
    BLUE(0x0000FF, Material.BLUE_STAINED_GLASS_PANE),
    PURPLE(0xAA00FF, Material.PURPLE_STAINED_GLASS_PANE),
    WHITE(0xFFFFFF, Material.WHITE_STAINED_GLASS_PANE);

    public static final int MIN_ALPHA = 5;
    public static final int MAX_ALPHA = 100;
    public static final int ALPHA_STEP = 5;

    private final int rgb;
    private final Material icon;

    WallColor(int rgb, Material icon) {
        this.rgb = rgb;
        this.icon = icon;
    }

    public int getRgb() {
        return rgb;
    }

    public Material getIcon() {
        return icon;
    }

    public WallColor next() {
        WallColor[] values = values();
        return values[(ordinal() + 1) % values.length];
    }

    public WallColor previous() {
        WallColor[] values = values();
        return values[(ordinal() - 1 + values.length) % values.length];
    }

    /** Возвращает ARGB-значение для background text_display. */
    public int toArgb(int alphaPercent) {
        int clamped = Math.max(0, Math.min(100, alphaPercent));
        int alpha = Math.round(clamped * 255f / 100f);
        return (alpha << 24) | rgb;
    }

    public static WallColor fromString(String name, WallColor fallback) {
        if (name == null) return fallback;
        try {
            return valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}