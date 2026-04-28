package ro.andreilarazboi.donutcore.crates;

import org.bukkit.plugin.java.JavaPlugin;

public class CratesModule {
    private final DonutCrates crates;

    public CratesModule(JavaPlugin plugin) {
        this.crates = new DonutCrates(plugin);
    }

    public void enable() {
        crates.enable();
    }

    public void disable() {
        crates.disable();
    }

    public DonutCrates getCrates() {
        return crates;
    }
}
