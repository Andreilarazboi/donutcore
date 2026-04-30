package ro.andreilarazboi.donutcore.sell;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;

public class SellMySQL {
    private final DonutSell plugin;
    private final StorageMode storageMode;
    private Connection connection;
    private final String url;
    private final String user;
    private final String password;
    private final String tablePlayers;
    private final String tableCategories;
    private final String tableItems;
    private final String tableToggleWorth;

    public SellMySQL(DonutSell plugin, StorageMode storageMode) {
        this.plugin = plugin;
        this.storageMode = storageMode;
        String prefix;
        if (storageMode == StorageMode.MYSQL) {
            ConfigurationSection cfg = plugin.getMysqlConfig().getConfigurationSection("mysql");
            if (cfg == null) throw new IllegalStateException("mysql section missing in mysql.yml");
            String host = cfg.getString("host", "localhost");
            int port = cfg.getInt("port", 3306);
            String database = cfg.getString("database", "minecraft");
            this.user = cfg.getString("user", "root");
            this.password = cfg.getString("password", "");
            boolean useSSL = cfg.getBoolean("use-ssl", false);
            String params = "useSSL=" + useSSL + "&autoReconnect=true&characterEncoding=utf8&useUnicode=true";
            this.url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?" + params;
            prefix = cfg.getString("table-prefix", "sell_");
        } else {
            this.user = "";
            this.password = "";
            String file = plugin.getConfig().getString("sqlite.file", "sell-data.db");
            this.url = "jdbc:sqlite:" + new File(plugin.getPlugin().getDataFolder(), file).getAbsolutePath();
            prefix = plugin.getConfig().getString("sqlite.table-prefix", "sell_");
        }
        this.tablePlayers = prefix + "players_v2";
        this.tableCategories = prefix + "categories_v2";
        this.tableItems = prefix + "items_v2";
        this.tableToggleWorth = prefix + "toggleworth_v2";
    }

    public boolean init() {
        try {
            this.connect();
            this.createTables();
            return true;
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE, "[DonutCore] Could not connect to database: " + ex.getMessage(), ex);
            this.closeQuietly();
            return false;
        }
    }

    private void connect() throws SQLException {
        if (this.connection != null && !this.connection.isClosed()) return;
        this.connection = this.storageMode == StorageMode.SQLITE
                ? DriverManager.getConnection(this.url)
                : DriverManager.getConnection(this.url, this.user, this.password);
    }

    private void createTables() throws SQLException {
        try (Statement st = this.connection.createStatement()) {
            if (this.storageMode == StorageMode.SQLITE) {
                st.execute("PRAGMA journal_mode=WAL;");
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tablePlayers
                    + " (uuid CHAR(36) NOT NULL, total DOUBLE NOT NULL DEFAULT 0, PRIMARY KEY (uuid))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tableCategories
                    + " (uuid CHAR(36) NOT NULL, category VARCHAR(64) NOT NULL, amount DOUBLE NOT NULL DEFAULT 0, PRIMARY KEY (uuid, category))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tableItems
                    + " (uuid CHAR(36) NOT NULL, item VARCHAR(128) NOT NULL, count DOUBLE NOT NULL DEFAULT 0, revenue DOUBLE NOT NULL DEFAULT 0, PRIMARY KEY (uuid, item))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS " + this.tableToggleWorth
                    + " (uuid CHAR(36) NOT NULL, PRIMARY KEY (uuid))");
        }
    }

    public void closeQuietly() {
        if (this.connection != null) {
            try { this.connection.close(); } catch (SQLException ignored) {}
        }
    }

    public synchronized PlayerSnapshot loadPlayerData(UUID uuid) {
        PlayerSnapshot snap = new PlayerSnapshot();
        try {
            this.connect();
            String u = uuid.toString();
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT total FROM " + this.tablePlayers + " WHERE uuid = ?")) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    snap.total = rs.next() ? rs.getDouble("total") : 0.0;
                }
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT category, amount FROM " + this.tableCategories + " WHERE uuid = ?")) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        snap.categories.put(rs.getString("category"), rs.getDouble("amount"));
                    }
                }
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT item, count, revenue FROM " + this.tableItems + " WHERE uuid = ?")) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        snap.items.put(rs.getString("item"),
                                new DonutSell.Stats(rs.getDouble("count"), rs.getDouble("revenue")));
                    }
                }
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT uuid FROM " + this.tableToggleWorth + " WHERE uuid = ?")) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    snap.toggleDisabled = rs.next();
                }
            }
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE,
                    "[DonutCore] Failed to load player data for " + uuid, ex);
            return null;
        }
        return snap;
    }

    public synchronized void loadAll(
            Map<UUID, Double> totalSold,
            Map<UUID, Map<String, Double>> soldByCategory,
            Map<UUID, Map<String, DonutSell.Stats>> itemHistory,
            Set<UUID> toggleWorthDisabled) {
        try {
            this.connect();
            totalSold.clear();
            soldByCategory.clear();
            itemHistory.clear();
            toggleWorthDisabled.clear();

            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT uuid, total FROM " + this.tablePlayers)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("uuid");
                        double total = rs.getDouble("total");
                        try { totalSold.put(UUID.fromString(uuidStr), total); }
                        catch (IllegalArgumentException ignored) {}
                    }
                }
            }

            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT uuid, category, amount FROM " + this.tableCategories)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("uuid");
                        String category = rs.getString("category");
                        double amount = rs.getDouble("amount");
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            soldByCategory.computeIfAbsent(uuid, k -> new HashMap<>()).put(category, amount);
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }

            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT uuid, item, count, revenue FROM " + this.tableItems)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("uuid");
                        String itemKey = rs.getString("item");
                        double count = rs.getDouble("count");
                        double revenue = rs.getDouble("revenue");
                        try {
                            UUID uuid = UUID.fromString(uuidStr);
                            itemHistory.computeIfAbsent(uuid, k -> new HashMap<>())
                                    .put(itemKey, new DonutSell.Stats(count, revenue));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }

            try (PreparedStatement ps = this.connection.prepareStatement(
                    "SELECT uuid FROM " + this.tableToggleWorth)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String uuidStr = rs.getString("uuid");
                        try { toggleWorthDisabled.add(UUID.fromString(uuidStr)); }
                        catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE,
                    "[DonutCore] Failed to load stats from database", ex);
        }
    }

    public synchronized void applySaleDelta(UUID uuid, double totalDelta,
            Map<String, Double> catDelta, Map<String, DonutSell.Stats> itemsDelta) {
        if (totalDelta <= 0.0
                && (catDelta == null || catDelta.isEmpty())
                && (itemsDelta == null || itemsDelta.isEmpty())) return;
        try {
            this.connect();
            String u = uuid.toString();
            if (totalDelta > 0.0) {
                String upsertTotal = this.storageMode == StorageMode.SQLITE
                        ? "INSERT INTO " + this.tablePlayers + " (uuid, total) VALUES (?, ?) ON CONFLICT(uuid) DO UPDATE SET total = total + excluded.total"
                        : "INSERT INTO " + this.tablePlayers + " (uuid, total) VALUES (?, ?) ON DUPLICATE KEY UPDATE total = total + VALUES(total)";
                try (PreparedStatement ps = this.connection.prepareStatement(upsertTotal)) {
                    ps.setString(1, u);
                    ps.setDouble(2, totalDelta);
                    ps.executeUpdate();
                }
            }
            if (catDelta != null && !catDelta.isEmpty()) {
                String upsertCategory = this.storageMode == StorageMode.SQLITE
                        ? "INSERT INTO " + this.tableCategories + " (uuid, category, amount) VALUES (?, ?, ?) ON CONFLICT(uuid, category) DO UPDATE SET amount = amount + excluded.amount"
                        : "INSERT INTO " + this.tableCategories + " (uuid, category, amount) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount)";
                try (PreparedStatement ps = this.connection.prepareStatement(upsertCategory)) {
                    for (Map.Entry<String, Double> entry : catDelta.entrySet()) {
                        ps.setString(1, u);
                        ps.setString(2, entry.getKey());
                        ps.setDouble(3, entry.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
            if (itemsDelta == null || itemsDelta.isEmpty()) return;
            String upsertItem = this.storageMode == StorageMode.SQLITE
                    ? "INSERT INTO " + this.tableItems + " (uuid, item, count, revenue) VALUES (?, ?, ?, ?) ON CONFLICT(uuid, item) DO UPDATE SET count = count + excluded.count, revenue = revenue + excluded.revenue"
                    : "INSERT INTO " + this.tableItems + " (uuid, item, count, revenue) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE count = count + VALUES(count), revenue = revenue + VALUES(revenue)";
            try (PreparedStatement ps = this.connection.prepareStatement(upsertItem)) {
                for (Map.Entry<String, DonutSell.Stats> entry : itemsDelta.entrySet()) {
                    DonutSell.Stats st = entry.getValue();
                    ps.setString(1, u);
                    ps.setString(2, entry.getKey());
                    ps.setDouble(3, st.count);
                    ps.setDouble(4, st.revenue);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE,
                    "[DonutCore] Failed to apply sale delta to database", ex);
        }
    }

    public synchronized void setToggleWorthDisabled(UUID uuid, boolean disabled) {
        try {
            this.connect();
            String u = uuid.toString();
            if (disabled) {
                String insert = this.storageMode == StorageMode.SQLITE
                        ? "INSERT OR IGNORE INTO " + this.tableToggleWorth + " (uuid) VALUES (?)"
                        : "INSERT IGNORE INTO " + this.tableToggleWorth + " (uuid) VALUES (?)";
                try (PreparedStatement ps = this.connection.prepareStatement(insert)) {
                    ps.setString(1, u);
                    ps.executeUpdate();
                }
                return;
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "DELETE FROM " + this.tableToggleWorth + " WHERE uuid = ?")) {
                ps.setString(1, u);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE,
                    "[DonutCore] Failed to update toggleworth in database", ex);
        }
    }

    public synchronized void resetPlayerData(UUID uuid) {
        try {
            this.connect();
            String u = uuid.toString();
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "DELETE FROM " + this.tablePlayers + " WHERE uuid = ?")) {
                ps.setString(1, u);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "DELETE FROM " + this.tableCategories + " WHERE uuid = ?")) {
                ps.setString(1, u);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "DELETE FROM " + this.tableItems + " WHERE uuid = ?")) {
                ps.setString(1, u);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = this.connection.prepareStatement(
                    "DELETE FROM " + this.tableToggleWorth + " WHERE uuid = ?")) {
                ps.setString(1, u);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            this.plugin.getPlugin().getLogger().log(Level.SEVERE,
                    "[DonutCore] Failed to reset player data in database", ex);
        }
    }

    public enum StorageMode {
        MYSQL, SQLITE
    }

    public static class PlayerSnapshot {
        public double total;
        public Map<String, Double> categories = new HashMap<>();
        public Map<String, DonutSell.Stats> items = new HashMap<>();
        public boolean toggleDisabled;
    }
}
