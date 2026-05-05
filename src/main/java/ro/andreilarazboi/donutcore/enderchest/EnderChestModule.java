package ro.andreilarazboi.donutcore.enderchest;

import org.bukkit.plugin.java.JavaPlugin;

public class EnderChestModule {
    private final DonutEnderChest enderChest;

    public EnderChestModule(JavaPlugin plugin) {
        this.enderChest = new DonutEnderChest(plugin);
    }

    public void enable() {
        this.enderChest.enable();
    }

    public void disable() {
        this.enderChest.disable();
    }

    public DonutEnderChest getEnderChest() {
        return this.enderChest;
    }
}
