package ro.andreilarazboi.donutcore.sell;

import org.bukkit.plugin.java.JavaPlugin;

public class SellModule {
    private final DonutSell sell;

    public SellModule(JavaPlugin plugin) {
        this.sell = new DonutSell(plugin);
    }

    public void enable() {
        this.sell.enable();
    }

    public void disable() {
        this.sell.disable();
    }

    public DonutSell getSell() {
        return this.sell;
    }
}
