package ro.andreilarazboi.donutcore.crates.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ro.andreilarazboi.donutcore.DonutCore;
import ro.andreilarazboi.donutcore.crates.CratesModule;

public class CratePlaceholderExpansion extends PlaceholderExpansion {

    private final CratesModule module;

    public CratePlaceholderExpansion(CratesModule module) {
        this.module = module;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "donutcrate";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Andreilarazboi";
    }

    @Override
    public @NotNull String getVersion() {
        return DonutCore.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "0";

        // %donutcrate_key_<CrateName>%
        if (params.startsWith("key_")) {
            String crateName = params.substring(4);
            return String.valueOf(module.getKeyManager().getKeys(player, crateName));
        }

        // %donutcrate_opens_<CrateName>%
        if (params.startsWith("opens_")) {
            String crateName = params.substring(6);
            var stats = module.getDatabase().getStats(player.getUniqueId());
            int[] data = stats.get(crateName);
            return data != null ? String.valueOf(data[0]) : "0";
        }

        // %donutcrate_total_opens%
        if (params.equals("total_opens")) {
            var stats = module.getDatabase().getStats(player.getUniqueId());
            return String.valueOf(stats.values().stream().mapToInt(d -> d[0]).sum());
        }

        return null;
    }
}
