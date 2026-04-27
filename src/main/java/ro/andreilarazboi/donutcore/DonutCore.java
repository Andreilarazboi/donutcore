package ro.andreilarazboi.donutcore;

import org.bukkit.plugin.java.JavaPlugin;
import ro.andreilarazboi.donutcore.crates.CratesModule;

public final class DonutCore extends JavaPlugin {

    private static DonutCore instance;
    private CratesModule cratesModule;

    @Override
    public void onEnable() {
        instance = this;
        cratesModule = new CratesModule(this);
        cratesModule.enable();
        getLogger().info("DonutCore enabled!");
    }

    @Override
    public void onDisable() {
        if (cratesModule != null) cratesModule.disable();
        getLogger().info("DonutCore disabled.");
    }

    public static DonutCore getInstance() {
        return instance;
    }

    public CratesModule getCratesModule() {
        return cratesModule;
    }
}
