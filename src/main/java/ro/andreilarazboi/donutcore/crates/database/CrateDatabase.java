package ro.andreilarazboi.donutcore.crates.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class CrateDatabase {

    private final JavaPlugin plugin;
    private Connection connection;

    public CrateDatabase(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dbFile = new File(plugin.getDataFolder(), "data.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            createTables();
            plugin.getLogger().info("[DonutCore] SQLite database connected.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[DonutCore] Failed to connect to database!", e);
        }
    }

    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[DonutCore] Error closing database.", e);
        }
    }

    private void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_keys (
                    uuid TEXT NOT NULL,
                    crate_name TEXT NOT NULL,
                    amount INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, crate_name)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS player_stats (
                    uuid TEXT NOT NULL,
                    crate_name TEXT NOT NULL,
                    opens INTEGER NOT NULL DEFAULT 0,
                    last_reward TEXT NOT NULL DEFAULT '',
                    total_rewards INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY (uuid, crate_name)
                )
            """);
        }
    }

    public int getKeys(UUID uuid, String crateName) {
        String sql = "SELECT amount FROM player_keys WHERE uuid = ? AND crate_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("amount");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error getting keys for " + uuid, e);
        }
        return 0;
    }

    public void setKeys(UUID uuid, String crateName, int amount) {
        String sql = "INSERT INTO player_keys (uuid, crate_name, amount) VALUES (?, ?, ?) " +
                     "ON CONFLICT(uuid, crate_name) DO UPDATE SET amount = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateName);
            ps.setInt(3, amount);
            ps.setInt(4, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error setting keys for " + uuid, e);
        }
    }

    public void addKeys(UUID uuid, String crateName, int amount) {
        int current = getKeys(uuid, crateName);
        setKeys(uuid, crateName, current + amount);
    }

    public boolean removeKeys(UUID uuid, String crateName, int amount) {
        int current = getKeys(uuid, crateName);
        if (current < amount) return false;
        setKeys(uuid, crateName, current - amount);
        return true;
    }

    public void resetKeys(UUID uuid, String crateName) {
        setKeys(uuid, crateName, 0);
    }

    public void resetAllKeys(String crateName) {
        String sql = "UPDATE player_keys SET amount = 0 WHERE crate_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, crateName);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error resetting all keys for " + crateName, e);
        }
    }

    public Map<String, Integer> getAllKeys(UUID uuid) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT crate_name, amount FROM player_keys WHERE uuid = ? AND amount > 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("crate_name"), rs.getInt("amount"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error getting all keys for " + uuid, e);
        }
        return result;
    }

    public void recordOpen(UUID uuid, String crateName, String rewardName) {
        String sql = "INSERT INTO player_stats (uuid, crate_name, opens, last_reward, total_rewards) VALUES (?, ?, 1, ?, 1) " +
                     "ON CONFLICT(uuid, crate_name) DO UPDATE SET opens = opens + 1, last_reward = ?, total_rewards = total_rewards + 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateName);
            ps.setString(3, rewardName);
            ps.setString(4, rewardName);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error recording open for " + uuid, e);
        }
    }

    public Map<String, int[]> getStats(UUID uuid) {
        Map<String, int[]> result = new LinkedHashMap<>();
        String sql = "SELECT crate_name, opens, total_rewards FROM player_stats WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("crate_name"),
                        new int[]{rs.getInt("opens"), rs.getInt("total_rewards")});
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error getting stats for " + uuid, e);
        }
        return result;
    }

    public String getLastReward(UUID uuid, String crateName) {
        String sql = "SELECT last_reward FROM player_stats WHERE uuid = ? AND crate_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, crateName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("last_reward");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Error getting last reward for " + uuid, e);
        }
        return "None";
    }
}
