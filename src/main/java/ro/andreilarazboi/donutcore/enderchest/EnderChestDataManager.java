package ro.andreilarazboi.donutcore.enderchest;

import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EnderChestDataManager {
    private static final int MAX_SLOTS = 54;
    private final DonutEnderChest plugin;
    private Connection connection;
    private boolean enabled;

    public EnderChestDataManager(DonutEnderChest plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("storage.sqlite.enabled", true);
        if (!this.enabled) return;
        try {
            setupSqlite();
            createTable();
            plugin.getLogger().info("[DonutEnderChest] SQLite storage initialized.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[DonutEnderChest] SQLite initialization failed.", e);
            closeQuietly();
            this.enabled = false;
        }
    }

    private void setupSqlite() throws SQLException {
        String path = plugin.getConfig().getString("storage.sqlite.path", "enderchest.db");
        if (path == null || path.isBlank()) path = "enderchest.db";
        String url = (path.startsWith("/") || path.contains(":"))
                ? "jdbc:sqlite:" + path
                : "jdbc:sqlite:" + plugin.getDataFolder().toPath().resolve(path);
        this.connection = DriverManager.getConnection(url);
        this.connection.setAutoCommit(true);
    }

    private void createTable() throws SQLException {
        try (Statement st = this.connection.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS enderchest_data (" +
                    "uuid TEXT PRIMARY KEY, items TEXT NOT NULL, updated_at INTEGER NOT NULL)");
        }
    }

    public Map<Integer, ItemStack> loadItems(UUID playerUUID) {
        Map<Integer, ItemStack> result = new HashMap<>();
        if (!enabled || connection == null) return result;
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT items FROM enderchest_data WHERE uuid=?")) {
            ps.setString(1, playerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) decodeItems(rs.getString("items"), result);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[DonutEnderChest] Failed to load items for " + playerUUID, e);
        }
        return result;
    }

    public void saveItems(UUID playerUUID, Map<Integer, ItemStack> items) {
        if (!enabled || connection == null) return;
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO enderchest_data (uuid, items, updated_at) VALUES (?,?,?) " +
                "ON CONFLICT(uuid) DO UPDATE SET items=excluded.items, updated_at=excluded.updated_at")) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, encodeItems(items));
            ps.setLong(3, System.currentTimeMillis());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[DonutEnderChest] Failed to save items for " + playerUUID, e);
        }
    }

    public void clearItems(UUID playerUUID) {
        saveItems(playerUUID, new HashMap<>());
    }

    private String encodeItems(Map<Integer, ItemStack> items) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack item = entry.getValue();
            if (item == null || item.getType().isAir()) continue;
            try {
                String b64 = Base64.getEncoder().encodeToString(item.serializeAsBytes());
                if (sb.length() > 0) sb.append('\n');
                sb.append(entry.getKey()).append(':').append(b64);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[DonutEnderChest] Failed to serialize item at slot " + entry.getKey(), e);
            }
        }
        return sb.toString();
    }

    private void decodeItems(String encoded, Map<Integer, ItemStack> out) {
        if (encoded == null || encoded.isBlank()) return;
        for (String line : encoded.split("\n")) {
            if (line.isBlank()) continue;
            int colon = line.indexOf(':');
            if (colon < 1) continue;
            try {
                int slot = Integer.parseInt(line.substring(0, colon));
                if (slot < 0 || slot >= MAX_SLOTS) continue;
                byte[] bytes = Base64.getDecoder().decode(line.substring(colon + 1));
                out.put(slot, ItemStack.deserializeBytes(bytes));
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "[DonutEnderChest] Failed to deserialize item: " + e.getMessage());
            }
        }
    }

    public void shutdown() {
        closeQuietly();
    }

    private void closeQuietly() {
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
            connection = null;
        }
    }
}
