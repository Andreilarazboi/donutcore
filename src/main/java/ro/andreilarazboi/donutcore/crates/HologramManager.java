
package ro.andreilarazboi.donutcore.crates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;

public class HologramManager {
    private final DonutCrates plugin;
    private final Map<String, Map<UUID, List<UUID>>> activePersonal = new HashMap<String, Map<UUID, List<UUID>>>();
    private static final double LINE_SPACING = 0.25;
    private static final String META_KEY = "donut-holo";
    private static final String META_CRATE = "crate";
    private static final String TAG_ANY = "donutcrates";
    private static final String TAG_CRATE_PREFIX = "donutcrate:";

    public HologramManager(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public void startupVacuum() {
        this.hardSweepRemove(null);
    }

    public void despawnAll() {
        for (Map<UUID, List<UUID>> per : this.activePersonal.values()) {
            for (List<UUID> uuids : per.values()) {
                for (UUID id : uuids) {
                    Entity e = Bukkit.getEntity((UUID)id);
                    if (e == null) continue;
                    e.remove();
                }
            }
        }
        this.activePersonal.clear();
        this.hardSweepRemove(null);
    }

    public void removeCrate(String crate) {
        Map<UUID, List<UUID>> per = this.activePersonal.remove(crate);
        if (per != null) {
            per.values().forEach(list -> list.forEach(id -> {
                Entity e = Bukkit.getEntity((UUID)id);
                if (e != null) {
                    e.remove();
                }
            }));
        }
        this.hardSweepRemove(crate);
    }

    public void refreshCrate(String crate) {
        Block block;
        ArrayList<String> rawLines;
        this.removeCrate(crate);
        ConfigurationSection sec = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Hologram");
        if (sec == null || !sec.getBoolean("enabled", false)) {
            return;
        }
        String templateId = sec.getString("template", null);
        ConfigurationSection templateSec = templateId == null ? null : this.plugin.cfg.config.getConfigurationSection("hologram-templates." + templateId);
        ArrayList<String> arrayList = rawLines = templateSec != null ? new ArrayList<String>(templateSec.getStringList("lines")) : new ArrayList(sec.getStringList("lines"));
        if (rawLines.isEmpty()) {
            return;
        }
        double offset = templateSec != null ? templateSec.getDouble("offset-y", sec.getDouble("offsetY", 1.5)) : sec.getDouble("offsetY", 1.5);
        boolean shadow = sec.getBoolean("shadow", true);
        String bgId = sec.getString("bgColor", null);
        if (bgId == null) {
            boolean oldTransparent = sec.getBoolean("background-transparent", true);
            String string = bgId = oldTransparent ? "TRANSPARENT" : "BLACK";
        }
        if ((block = this.plugin.crateMgr.crateBlocks.get(crate)) == null) {
            return;
        }
        Location crateLoc = block.getLocation();
        if (!this.isChunkLoaded(crateLoc)) {
            return;
        }
        HashMap<UUID, List<UUID>> perMap = new HashMap<UUID, List<UUID>>();
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!this.isViewerEligible(viewer, crateLoc)) continue;
            List<String> resolved = this.resolveForPlayer(rawLines, crate, viewer);
            List<UUID> spawned = this.spawnLineStack(crateLoc, offset, resolved, crate, shadow, bgId);
            perMap.put(viewer.getUniqueId(), spawned);
            for (UUID id : spawned) {
                Entity ent = Bukkit.getEntity((UUID)id);
                if (ent == null) continue;
                viewer.showEntity((Plugin)this.plugin.getPlugin(), ent);
            }
        }
        this.activePersonal.put(crate, perMap);
    }

    public void handleJoin(Player p) {
        for (String crate : this.plugin.crateMgr.crateBlocks.keySet()) {
            Location crateLoc;
            Block block;
            String templateId;
            ConfigurationSection templateSec;
            List lines;
            ConfigurationSection sec = this.plugin.cfg.crates.getConfigurationSection("Crates." + crate + ".Hologram");
            if (sec == null || !sec.getBoolean("enabled", false) || (lines = (templateSec = (templateId = sec.getString("template", null)) == null ? null : this.plugin.cfg.config.getConfigurationSection("hologram-templates." + templateId)) != null ? templateSec.getStringList("lines") : sec.getStringList("lines")).isEmpty() || (block = this.plugin.crateMgr.crateBlocks.get(crate)) == null || !this.isChunkLoaded(crateLoc = block.getLocation()) || !this.isViewerEligible(p, crateLoc)) continue;
            Map per = this.activePersonal.computeIfAbsent(crate, k -> new HashMap());
            List old = (List)per.remove(p.getUniqueId());
            if (old != null) {
                old.forEach(id -> {
                    Entity e = Bukkit.getEntity((UUID)id);
                    if (e != null) {
                        e.remove();
                    }
                });
            }
            double offset = templateSec != null ? templateSec.getDouble("offset-y", sec.getDouble("offsetY", 1.5)) : sec.getDouble("offsetY", 1.5);
            boolean shadow = sec.getBoolean("shadow", true);
            String bgId = sec.getString("bgColor", null);
            if (bgId == null) {
                boolean oldTransparent = sec.getBoolean("background-transparent", true);
                bgId = oldTransparent ? "TRANSPARENT" : "BLACK";
            }
            List<String> resolved = this.resolveForPlayer(lines, crate, p);
            List<UUID> spawned = this.spawnLineStack(crateLoc, offset, resolved, crate, shadow, bgId);
            per.put(p.getUniqueId(), spawned);
            for (UUID id2 : spawned) {
                Entity ent = Bukkit.getEntity((UUID)id2);
                if (ent == null) continue;
                p.showEntity((Plugin)this.plugin.getPlugin(), ent);
            }
        }
    }

    public void handleQuit(Player p) {
        for (Map<UUID, List<UUID>> per : this.activePersonal.values()) {
            List<UUID> list = per.remove(p.getUniqueId());
            if (list == null) continue;
            list.forEach(id -> {
                Entity e = Bukkit.getEntity((UUID)id);
                if (e != null) {
                    e.remove();
                }
            });
        }
    }

    private void hardSweepRemove(String crateOrNull) {
        String crateTag = crateOrNull == null ? null : TAG_CRATE_PREFIX + crateOrNull.toLowerCase(Locale.ROOT);
        for (World w : Bukkit.getWorlds()) {
            for (TextDisplay e : w.getEntitiesByClass(TextDisplay.class)) {
                boolean remove;
                Set tags = e.getScoreboardTags();
                boolean taggedOurs = tags.contains(TAG_ANY) || crateTag != null && tags.contains(crateTag);
                boolean metaOurs = false;
                boolean metaCrateMatch = false;
                if (e.hasMetadata(META_KEY)) {
                    metaOurs = true;
                    if (crateOrNull == null) {
                        metaCrateMatch = true;
                    } else if (e.hasMetadata(META_CRATE)) {
                        metaCrateMatch = e.getMetadata(META_CRATE).stream().anyMatch(vm -> String.valueOf(vm.value()).equalsIgnoreCase(crateOrNull));
                    }
                }
                if (!(remove = crateOrNull == null && (taggedOurs || metaOurs) || crateOrNull != null && (crateTag != null && tags.contains(crateTag) || metaCrateMatch))) continue;
                e.remove();
            }
        }
    }

    private List<UUID> spawnLineStack(Location crateLoc, double offsetY, List<String> lines, String crate, boolean shadow, String bgId) {
        ArrayList<UUID> created = new ArrayList<UUID>();
        if (lines.isEmpty() || crateLoc.getWorld() == null) {
            return created;
        }
        String crateTag = TAG_CRATE_PREFIX + crate.toLowerCase(Locale.ROOT);
        Color bgColor = this.resolveBackgroundColor(bgId);
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            Location base = crateLoc.clone().add(0.5, offsetY + (double)(lines.size() - 1 - i) * 0.25, 0.5);
            TextDisplay display = (TextDisplay)crateLoc.getWorld().spawn(base, TextDisplay.class, ent -> {
                ent.setText(Utils.formatColors(line));
                ent.setBillboard(Display.Billboard.CENTER);
                ent.setShadowed(shadow);
                ent.setSeeThrough(false);
                ent.setBackgroundColor(bgColor);
                ent.setLineWidth(200);
                ent.setViewRange(32.0f);
                ent.setVisibleByDefault(false);
                ent.setMetadata(META_KEY, (MetadataValue)new FixedMetadataValue((Plugin)this.plugin.getPlugin(), (Object)true));
                ent.setMetadata(META_CRATE, (MetadataValue)new FixedMetadataValue((Plugin)this.plugin.getPlugin(), (Object)crate));
                ent.addScoreboardTag(TAG_ANY);
                ent.addScoreboardTag(crateTag);
            });
            created.add(display.getUniqueId());
        }
        return created;
    }

    private Color resolveBackgroundColor(String raw) {
        if (raw == null) {
            return Color.fromARGB((int)0, (int)0, (int)0, (int)0);
        }
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty() || s.equals("TRANSPARENT") || s.equals("NONE")) {
            return Color.fromARGB((int)0, (int)0, (int)0, (int)0);
        }
        int r = 0;
        int g = 0;
        int b = 0;
        switch (s) {
            case "BLACK": {
                r = 0;
                g = 0;
                b = 0;
                break;
            }
            case "DARK_GRAY": {
                r = 40;
                g = 40;
                b = 40;
                break;
            }
            case "LIGHT_GRAY": {
                r = 170;
                g = 170;
                b = 170;
                break;
            }
            case "WHITE": {
                r = 255;
                g = 255;
                b = 255;
                break;
            }
            case "RED": {
                r = 255;
                g = 0;
                b = 0;
                break;
            }
            case "ORANGE": {
                r = 255;
                g = 128;
                b = 0;
                break;
            }
            case "YELLOW": {
                r = 255;
                g = 255;
                b = 0;
                break;
            }
            case "LIME": {
                r = 128;
                g = 255;
                b = 0;
                break;
            }
            case "GREEN": {
                r = 0;
                g = 160;
                b = 0;
                break;
            }
            case "CYAN": {
                r = 0;
                g = 255;
                b = 255;
                break;
            }
            case "LIGHT_BLUE": {
                r = 80;
                g = 160;
                b = 255;
                break;
            }
            case "BLUE": {
                r = 0;
                g = 0;
                b = 255;
                break;
            }
            case "PURPLE": {
                r = 160;
                g = 0;
                b = 255;
                break;
            }
            case "MAGENTA": {
                r = 255;
                g = 0;
                b = 255;
                break;
            }
            case "PINK": {
                r = 255;
                g = 128;
                b = 192;
                break;
            }
            case "BROWN": {
                r = 96;
                g = 64;
                b = 32;
                break;
            }
            default: {
                String hex = s;
                if (hex.startsWith("#")) {
                    hex = hex.substring(1);
                }
                if (hex.length() == 6 && hex.matches("[0-9A-F]{6}")) {
                    try {
                        int val = Integer.parseInt(hex, 16);
                        r = val >> 16 & 0xFF;
                        g = val >> 8 & 0xFF;
                        b = val & 0xFF;
                        break;
                    }
                    catch (NumberFormatException ignored) {
                        return Color.fromARGB((int)160, (int)0, (int)0, (int)0);
                    }
                }
                return Color.fromARGB((int)160, (int)0, (int)0, (int)0);
            }
        }
        return Color.fromARGB((int)160, (int)r, (int)g, (int)b);
    }

    private List<String> resolveForPlayer(List<String> lines, String crate, Player viewer) {
        int playerKeys = this.plugin.dataMgr.getKeys(viewer, crate);
        String crateDisplayRaw = this.plugin.getCrateDisplayNameRaw(crate);
        ArrayList<String> out = new ArrayList<String>(lines.size());
        for (String s : lines) {
            String t = s.replace("%crate%", crateDisplayRaw).replace("%crate_id%", crate).replace("%player_keys%", String.valueOf(playerKeys)).replace("%total_keys%", String.valueOf(playerKeys));
            if (this.plugin.hasPapi && t.contains("%")) {
                try {
                    t = PlaceholderAPI.setPlaceholders((Player)viewer, (String)t);
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            out.add(t);
        }
        return out;
    }

    private boolean isChunkLoaded(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    private boolean isViewerEligible(Player viewer, Location crateLoc) {
        if (viewer == null || crateLoc == null || crateLoc.getWorld() == null) {
            return false;
        }
        if (!viewer.isOnline()) {
            return false;
        }
        if (!viewer.getWorld().equals((Object)crateLoc.getWorld())) {
            return false;
        }
        double maxDistance = this.plugin.cfg.config.getDouble("hologram-player-view-distance", 48.0);
        if (maxDistance <= 0.0) {
            maxDistance = 48.0;
        }
        return viewer.getLocation().distanceSquared(crateLoc) <= maxDistance * maxDistance;
    }
}

