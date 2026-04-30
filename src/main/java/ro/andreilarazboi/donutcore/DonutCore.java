package ro.andreilarazboi.donutcore;

import org.bukkit.plugin.java.JavaPlugin;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.sell.SellModule;

public final class DonutCore extends JavaPlugin {

    private static DonutCore instance;
    private CratesModule cratesModule;
    private SellModule sellModule;

    @Override
    public void onEnable() {
        instance = this;
        cratesModule = new CratesModule(this);
        cratesModule.enable();
        sellModule = new SellModule(this);
        sellModule.enable();
        getLogger().info("DonutCore enabled!");
    }

    @Override
    public void onDisable() {
        if (sellModule != null) sellModule.disable();
        if (cratesModule != null) cratesModule.disable();
        getLogger().info("DonutCore disabled.");
    }

    public static DonutCore getInstance() {
        return instance;
    }

    public CratesModule getCratesModule() {
        return cratesModule;
    }

    public SellModule getSellModule() {
        return sellModule;
    }
}
