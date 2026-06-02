package ru.sawaplago.chunkVisualizer.managers;

import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserSettingsManager {
    private final Map<UUID, UserSettings> cache = new ConcurrentHashMap<>();

    public UserSettings getSettings(UUID uuid) {
        return cache.get(uuid);
    }

    public void setSettings(UUID uuid, UserSettings settings) {
        cache.put(uuid, settings);
    }

    public void removeSettings(UUID uuid) {
        cache.remove(uuid);
    }
}