package ro.andreilarazboi.donutcore.crates.manager;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateHologramSettings;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;

import java.util.*;
import java.util.logging.Level;

public class HologramManager {

    private final JavaPlugin plugin;
    private final KeyManager keyManager;
    private final Map<String, List<TextDisplay>> hologramEntities = new HashMap<>();
    private final Map<String, BukkitRunnable> updateTasks = new HashMap<>();
    private final Map<String, CrateHologramSettings> templates = new HashMap<>();

    private static final double LINE_SPACING = 0.28;
    private static final double BASE_OFFSET = 2.5;

    public HologramManager(JavaPlugin plugin, KeyManager keyManager) {
        this.plugin = plugin;
        this.keyManager = keyManager;
    }

    public void spawnHologram(Crate crate) {
        Location loc = crate.getLocation();
        CrateHologramSettings settings = resolveSettings(crate);

        if (!settings.isEnabled() || loc == null || loc.getWorld() == null) return;

        removeHologram(crate.getName());

        List<String> lines = settings.getLines();
        List<TextDisplay> entities = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            double yOffset = BASE_OFFSET + (lines.size() - 1 - i) * LINE_SPACING;
            Location spawnLoc = loc.clone().add(0.5, yOffset, 0.5);

            TextDisplay display = (TextDisplay) loc.getWorld().spawnEntity(spawnLoc, EntityType.TEXT_DISPLAY);
            display.setPersistent(false);
            display.setBillboard(Display.Billboard.CENTER);
            display.setShadowed(settings.isTextShadow());

            int r = settings.getBackgroundColorR();
            int g = settings.getBackgroundColorG();
            int b = settings.getBackgroundColorB();
            int a = settings.getBackgroundAlpha();
            if (a > 0) {
                display.setBackgroundColor(Color.fromARGB(a, r, g, b));
            }

            String line = lines.get(i);
            display.text(ColorUtil.parse(line));
            display.setVisibleByDefault(true);
            entities.add(display);
        }

        hologramEntities.put(crate.getName(), entities);
        startUpdateTask(crate, settings);
    }

    private CrateHologramSettings resolveSettings(Crate crate) {
        CrateHologramSettings settings = crate.getHologramSettings();
        if (settings.getTemplateName() != null && !settings.getTemplateName().isEmpty()) {
            CrateHologramSettings template = templates.get(settings.getTemplateName());
            if (template != null) return template;
        }
        return settings;
    }

    private void startUpdateTask(Crate crate, CrateHologramSettings settings) {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                List<TextDisplay> entities = hologramEntities.get(crate.getName());
                if (entities == null) {
                    cancel();
                    return;
                }
                List<String> lines = settings.getLines();
                for (int i = 0; i < Math.min(lines.size(), entities.size()); i++) {
                    String line = lines.get(i);
                    Component component = ColorUtil.parse(line);
                    entities.get(i).text(component);
                }
            }
        };
        task.runTaskTimer(plugin, settings.getUpdateInterval(), settings.getUpdateInterval());
        updateTasks.put(crate.getName(), task);
    }

    public void removeHologram(String crateName) {
        BukkitRunnable task = updateTasks.remove(crateName);
        if (task != null) task.cancel();

        List<TextDisplay> entities = hologramEntities.remove(crateName);
        if (entities != null) {
            for (TextDisplay entity : entities) {
                if (entity != null && entity.isValid()) entity.remove();
            }
        }
    }

    public void respawnHologram(Crate crate) {
        removeHologram(crate.getName());
        spawnHologram(crate);
    }

    public void removeAll() {
        Set<String> names = new HashSet<>(hologramEntities.keySet());
        for (String name : names) removeHologram(name);
    }

    public void registerTemplate(String name, CrateHologramSettings settings) {
        templates.put(name, settings);
    }

    public void removeTemplate(String name) {
        templates.remove(name);
    }

    public boolean hasHologram(String crateName) {
        return hologramEntities.containsKey(crateName);
    }
}
