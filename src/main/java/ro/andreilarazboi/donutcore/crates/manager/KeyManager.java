package ro.andreilarazboi.donutcore.crates.manager;

import org.bukkit.entity.Player;
import ro.andreilarazboi.donutcore.crates.database.CrateDatabase;
import ro.andreilarazboi.donutcore.crates.model.Crate;

import java.util.Map;

public class KeyManager {

    private final CrateDatabase database;

    public KeyManager(CrateDatabase database) {
        this.database = database;
    }

    public int getKeys(Player player, Crate crate) {
        return database.getKeys(player.getUniqueId(), crate.getName());
    }

    public int getKeys(Player player, String crateName) {
        return database.getKeys(player.getUniqueId(), crateName);
    }

    public void addKeys(Player player, Crate crate, int amount) {
        database.addKeys(player.getUniqueId(), crate.getName(), amount);
    }

    public boolean removeKeys(Player player, Crate crate, int amount) {
        return database.removeKeys(player.getUniqueId(), crate.getName(), amount);
    }

    public void resetKeys(Player player, Crate crate) {
        database.resetKeys(player.getUniqueId(), crate.getName());
    }

    public void resetAllKeys(Crate crate) {
        database.resetAllKeys(crate.getName());
    }

    public boolean hasKey(Player player, Crate crate) {
        return getKeys(player, crate) > 0;
    }

    public boolean payKeys(Player from, Player to, Crate crate, int amount) {
        if (getKeys(from, crate) < amount) return false;
        database.removeKeys(from.getUniqueId(), crate.getName(), amount);
        database.addKeys(to.getUniqueId(), crate.getName(), amount);
        return true;
    }

    public Map<String, Integer> getAllKeys(Player player) {
        return database.getAllKeys(player.getUniqueId());
    }
}
