package ru.sawaplago.chunkVisualizer.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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
        this.migrateTables();
        this.registerMappers();
    }

    public void saveOrCreateUserSettings(UserSettings settings) {
        jdbi.useHandle(
                handle ->
                        handle.createUpdate(
                                        """
                                    INSERT OR REPLACE INTO user_settings
                                    (playerName, heights, isEnabled, material, mode, wallColor, wallAlpha, wallGlow)
                                    VALUES (:playerName, :heights, :enabled, :material, :mode, :wallColor, :wallAlpha, :wallGlow)
                                """)
                                .bindBean(settings)
                                .execute());
    }

    public Optional<UserSettings> getUserSettings(String playerName) {
        return jdbi.withHandle(
                handle ->
                        handle.createQuery(
                                        """
                                   SELECT playerName, heights, isEnabled as enabled, material,
                                          mode, wallColor, wallAlpha, wallGlow
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
        // HighlightMode и WallColor (enum) Jdbi сохраняет/читает по имени сам
    }

    private void createTables() {
        jdbi.withHandle(
                handle ->
                        handle.execute(
                                """
                            CREATE TABLE IF NOT EXISTS user_settings (
                                playerName  TEXT PRIMARY KEY,
                                heights     INTEGER,
                                isEnabled   BOOLEAN,
                                material    TEXT,
                                mode        TEXT DEFAULT 'BLOCKS',
                                wallColor   TEXT DEFAULT 'RED',
                                wallAlpha   INTEGER DEFAULT 50,
                                wallGlow    BOOLEAN DEFAULT 0
                            )
                        """));
    }

    /** Добавляет новые колонки в таблицу, созданную старыми версиями плагина. */
    private void migrateTables() {
        jdbi.useHandle(
                handle -> {
                    Set<String> columns =
                            new HashSet<>(
                                    handle.createQuery(
                                                    "SELECT name FROM pragma_table_info('user_settings')")
                                            .mapTo(String.class)
                                            .list());
                    if (!columns.contains("mode")) {
                        handle.execute(
                                "ALTER TABLE user_settings ADD COLUMN mode TEXT DEFAULT 'BLOCKS'");
                    }
                    if (!columns.contains("wallColor")) {
                        handle.execute(
                                "ALTER TABLE user_settings ADD COLUMN wallColor TEXT DEFAULT 'RED'");
                    }
                    if (!columns.contains("wallAlpha")) {
                        handle.execute(
                                "ALTER TABLE user_settings ADD COLUMN wallAlpha INTEGER DEFAULT 50");
                    }
                    if (!columns.contains("wallGlow")) {
                        handle.execute(
                                "ALTER TABLE user_settings ADD COLUMN wallGlow BOOLEAN DEFAULT 0");
                    }
                });
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