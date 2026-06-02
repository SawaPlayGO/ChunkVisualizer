package ru.sawaplago.chunkVisualizer.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.Optional;
import org.bukkit.Material;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.ArgumentFactory;
import ru.sawaplago.chunkVisualizer.managers.data.UserSettings;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final Jdbi jdbi;

    public DatabaseManager() {
        HikariConfig config = new HikariConfig();
        String directoryFullPath = DatabaseManager.getAndCreateDirectory("data.db");
        config.setJdbcUrl(directoryFullPath);
        config.setMaximumPoolSize(1);
        this.dataSource = new HikariDataSource(config);
        this.jdbi = Jdbi.create(dataSource);
        this.createTables();
        this.registerMappers();
    }

    public void saveOrCreateUserSettings(UserSettings settings) {
        jdbi.useHandle(
                handle ->
                        handle.createUpdate(
                                        """
                                    INSERT OR REPLACE INTO user_settings
                                    (playerName, heights, isEnabled, material)
                                    VALUES (:playerName, :heights, :enabled, :material)
                                """)
                                .bindBean(settings)
                                .execute());
    }

    public Optional<UserSettings> getUserSettings(String playerName) {
        return jdbi.withHandle(
                handle ->
                        handle.createQuery(
                                        """
                                   SELECT playerName, heights, isEnabled as enabled, material
                                   FROM user_settings
                                   WHERE playerName = :playerName
                                """)
                                .bind("playerName", playerName)
                                .mapToBean(UserSettings.class)
                                .findOne());
    }

    public void close() {
        dataSource.close();
    }

    private void registerMappers() {
        // Read Material -> string
        jdbi.registerColumnMapper(
                Material.class,
                (resultSet, column, __) -> Material.valueOf(resultSet.getString(column)));

        // Write String -> Material
        jdbi.registerArgument(
                (ArgumentFactory)
                        (type, value, __) ->
                                type == Material.class && value instanceof Material m
                                        ? Optional.of(
                                                (pos, stmt, ___) -> stmt.setString(pos, m.name()))
                                        : Optional.empty());
    }

    private void createTables() {
        jdbi.withHandle(
                handle ->
                        handle.execute(
                                """
                            CREATE TABLE IF NOT EXISTS user_settings (
                                playerName TEXT PRIMARY KEY,
                                heights     INTEGER,
                                isEnabled  BOOLEAN,
                                material    TEXT
                            )
                        """));
    }

    private static String getAndCreateDirectory(String fileName) {
        File dataFolder = new File("plugins/ChunkVisualizer");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File db = new File(dataFolder, fileName);
        return "jdbc:sqlite:" + db;
    }
}
