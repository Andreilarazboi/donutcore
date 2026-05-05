package ro.andreilarazboi.donutcore;

import org.bukkit.plugin.java.JavaPlugin;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.enderchest.EnderChestModule;
import ro.andreilarazboi.donutcore.sell.SellModule;

public final class DonutCore extends JavaPlugin {

    private static DonutCore instance;
    private CratesModule cratesModule;
    private SellModule sellModule;
    private EnderChestModule enderChestModule;

    @Override
    public void onEnable() {
        instance = this;
        cratesModule = new CratesModule(this);
        cratesModule.enable();
        sellModule = new SellModule(this);
        sellModule.enable();
        enderChestModule = new EnderChestModule(this);
        enderChestModule.enable();
        getLogger().info("DonutCore enabled!");
    }

    @Override
    public void onDisable() {
        if (enderChestModule != null) enderChestModule.disable();
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

    public EnderChestModule getEnderChestModule() {
        return enderChestModule;
    }
}
