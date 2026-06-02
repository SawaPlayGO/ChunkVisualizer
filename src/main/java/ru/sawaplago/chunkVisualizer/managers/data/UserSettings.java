package ru.sawaplago.chunkVisualizer.managers.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import ru.sawaplago.chunkVisualizer.ChunkVisualizer;
import ru.sawaplago.chunkVisualizer.managers.ConfigManager;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {
    private String playerName;
    private int heights;
    private boolean isEnabled;
    private Material material;

    public static UserSettings defaultSettings(String playerName) {
        ConfigManager configManager = ChunkVisualizer.getInstance().getConfigManager();
        return new UserSettings(
                playerName,
                configManager.getDefaultHeight(),
                configManager.isDefaultIsEnabled(),
                configManager.getDefaultMaterial());
    }
}
