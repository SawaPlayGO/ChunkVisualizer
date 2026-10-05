package ru.sawaplago.chunkVisualizer.managers.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;
import ru.sawaplago.chunkVisualizer.objects.HighlightMode;
import ru.sawaplago.chunkVisualizer.objects.WallColor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    private String playerName;
    private int heights;
    private boolean isEnabled;
    private Material material;
    private WallColor blockGlowColor;
    private HighlightMode mode;
    private WallColor wallColor;
    private int wallAlpha;
    private boolean wallGlow;

    public static UserSettings defaultSettings(String playerName) {
        ConfigManager configManager = ChunkVisualizer.getInstance().getConfigManager();
        return new UserSettings(
                playerName,
                configManager.getDefaultHeight(),
                configManager.isDefaultIsEnabled(),
                configManager.getDefaultMaterial(),
                configManager.getDefaultBlockGlowColor(),
                configManager.getDefaultMode(),
                configManager.getDefaultWallColor(),
                configManager.getDefaultWallAlpha(),
                configManager.isDefaultWallGlow());
    }

    /** Режим с учётом прав: без chunkvisualizer.use.display всегда BLOCKS. */
    public HighlightMode getEffectiveMode(Player player) {
        HighlightMode current = mode == null ? HighlightMode.BLOCKS : mode;
        return current.canUse(player) ? current : HighlightMode.BLOCKS;
    }

    /** Цвет обводки блоков; если в БД null - белый (стандартный). */
    public WallColor resolveBlockGlowColor() {
        return blockGlowColor != null ? blockGlowColor : WallColor.WHITE;
    }
}
