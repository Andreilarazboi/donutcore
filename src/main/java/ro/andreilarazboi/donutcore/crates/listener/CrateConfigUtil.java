
package ro.andreilarazboi.donutcore.crates.listener;

import ro.andreilarazboi.donutcore.crates.DonutCrates;
import org.bukkit.configuration.ConfigurationSection;

public final class CrateConfigUtil {
    private CrateConfigUtil() {
    }

    public static String findItemKeyBySlot(DonutCrates plugin, String crate, int slot) {
        ConfigurationSection items = plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
        if (items == null) {
            return null;
        }
        for (String k : items.getKeys(false)) {
            ConfigurationSection it = items.getConfigurationSection(k);
            if (it == null || it.getInt("slot", -1) != slot) continue;
            return k;
        }
        return null;
    }

    public static void swapSlots(DonutCrates plugin, String crate, int a, int b) {
        if (b < 0) {
            return;
        }
        int rows = plugin.cfg.crates.getInt("Crates." + crate + ".rows", 3);
        int size = Math.max(9, Math.min(54, rows * 9));
        if (a >= size || b >= size) {
            return;
        }
        ConfigurationSection items = plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Items");
        if (items == null) {
            return;
        }
        String ka = null;
        String kb = null;
        for (String k : items.getKeys(false)) {
            ConfigurationSection it = items.getConfigurationSection(k);
            if (it == null) continue;
            int s = it.getInt("slot");
            if (s == a) {
                ka = k;
            }
            if (s != b) continue;
            kb = k;
        }
        if (ka == null && kb == null) {
            return;
        }
        if (ka != null) {
            items.getConfigurationSection(ka).set("slot", b);
        }
        if (kb != null) {
            items.getConfigurationSection(kb).set("slot", a);
        }
        plugin.cfg.saveAll();
    }
}

