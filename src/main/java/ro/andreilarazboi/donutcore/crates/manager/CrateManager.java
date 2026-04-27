package ro.andreilarazboi.donutcore.crates.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ro.andreilarazboi.donutcore.crates.model.*;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class CrateManager {

    private final JavaPlugin plugin;
    private final Map<String, Crate> crates = new LinkedHashMap<>();
    private final File cratesFolder;
    public static final NamespacedKey KEY_CRATE_ID = new NamespacedKey("donutcore", "crate_key_id");

    public CrateManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.cratesFolder = new File(plugin.getDataFolder(), "crates");
    }

    public void loadAll() {
        crates.clear();
        if (!cratesFolder.exists()) {
            cratesFolder.mkdirs();
            plugin.saveResource("crates/ExampleCrate.yml", false);
        }

        File[] files = cratesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                Crate crate = loadCrate(key, config.getConfigurationSection(key));
                if (crate != null) {
                    crates.put(key, crate);
                }
            }
        }
        plugin.getLogger().info("[DonutCore] Loaded " + crates.size() + " crate(s).");
    }

    private Crate loadCrate(String name, ConfigurationSection section) {
        if (section == null) return null;
        Crate crate = new Crate(name);

        crate.setDisplayName(section.getString("displayName", "&6" + name));
        crate.setType(CrateType.valueOf(section.getString("type", "CHOOSE").toUpperCase()));
        crate.setRows(Math.max(1, Math.min(6, section.getInt("rows", 3))));
        crate.setFillerEnabled(section.getBoolean("fillerEnabled", true));
        String fillerMat = section.getString("fillerMaterial", "GRAY_STAINED_GLASS_PANE");
        Material mat = Material.getMaterial(fillerMat);
        crate.setFillerMaterial(mat != null ? mat : Material.GRAY_STAINED_GLASS_PANE);
        crate.setGuiTitle(section.getString("guiTitle", "&8Choose 1 item"));
        crate.setKeepOpenAfterClaim(section.getBoolean("keepOpenAfterClaim", false));

        ConfigurationSection locSection = section.getConfigurationSection("location");
        if (locSection != null) {
            String worldName = locSection.getString("world", "");
            if (!worldName.isEmpty() && Bukkit.getWorld(worldName) != null) {
                Location loc = new Location(
                        Bukkit.getWorld(worldName),
                        locSection.getDouble("x"),
                        locSection.getDouble("y"),
                        locSection.getDouble("z")
                );
                crate.setLocation(loc);
            }
        }

        ConfigurationSection holoSection = section.getConfigurationSection("hologram");
        if (holoSection != null) {
            CrateHologramSettings holo = new CrateHologramSettings();
            holo.setEnabled(holoSection.getBoolean("enabled", true));
            holo.setLines(holoSection.getStringList("lines"));
            holo.setBackgroundColorR(holoSection.getInt("backgroundColorR", 0));
            holo.setBackgroundColorG(holoSection.getInt("backgroundColorG", 0));
            holo.setBackgroundColorB(holoSection.getInt("backgroundColorB", 0));
            holo.setBackgroundAlpha(holoSection.getInt("backgroundAlpha", 0));
            holo.setTextShadow(holoSection.getBoolean("textShadow", false));
            holo.setUpdateInterval(Math.max(1, holoSection.getInt("updateInterval", 40)));
            holo.setTemplateName(holoSection.getString("templateName", ""));
            crate.setHologramSettings(holo);
        }

        ConfigurationSection keySection = section.getConfigurationSection("physicalKey");
        if (keySection != null) {
            CrateKey key = new CrateKey();
            Material keyMat = Material.getMaterial(keySection.getString("material", "TRIPWIRE_HOOK"));
            key.setMaterial(keyMat != null ? keyMat : Material.TRIPWIRE_HOOK);
            key.setDisplayName(keySection.getString("displayName", "&6" + name + " Key"));
            key.setLore(keySection.getStringList("lore"));
            key.setCustomModelData(keySection.getInt("customModelData", 0));
            crate.setPhysicalKey(key);
        }

        ConfigurationSection itemsSection = section.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemId : itemsSection.getKeys(false)) {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(itemId);
                if (itemSection == null) continue;
                CrateItem item = new CrateItem(itemId);
                Material itemMat = Material.getMaterial(itemSection.getString("material", "STONE"));
                item.setMaterial(itemMat != null ? itemMat : Material.STONE);
                item.setDisplayName(itemSection.getString("displayName", itemId));
                item.setLore(itemSection.getStringList("lore"));
                item.setSlot(itemSection.getInt("slot", 0));
                item.setCommand(itemSection.getString("command", ""));
                item.setChance(itemSection.getDouble("chance", 50.0));
                item.setBroadcastOnWin(itemSection.getBoolean("broadcastOnWin", false));
                item.setBroadcastMessage(itemSection.getString("broadcastMessage", ""));

                Map<Enchantment, Integer> enchants = new LinkedHashMap<>();
                for (String enchantStr : itemSection.getStringList("enchantments")) {
                    String[] parts = enchantStr.split(";");
                    if (parts.length == 2) {
                        NamespacedKey enchKey = NamespacedKey.minecraft(parts[0].toLowerCase());
                        Enchantment enchantment = Enchantment.getByKey(enchKey);
                        if (enchantment != null) {
                            try {
                                enchants.put(enchantment, Integer.parseInt(parts[1]));
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
                item.setEnchantments(enchants);
                crate.addItem(item);
            }
        }

        return crate;
    }

    public void saveCrate(Crate crate) {
        File file = new File(cratesFolder, crate.getName() + ".yml");
        FileConfiguration config = new YamlConfiguration();
        String base = crate.getName();

        config.set(base + ".displayName", crate.getDisplayName());
        config.set(base + ".type", crate.getType().name());
        config.set(base + ".rows", crate.getRows());
        config.set(base + ".fillerEnabled", crate.isFillerEnabled());
        config.set(base + ".fillerMaterial", crate.getFillerMaterial().name());
        config.set(base + ".guiTitle", crate.getGuiTitle());
        config.set(base + ".keepOpenAfterClaim", crate.isKeepOpenAfterClaim());

        Location loc = crate.getLocation();
        if (loc != null && loc.getWorld() != null) {
            config.set(base + ".location.world", loc.getWorld().getName());
            config.set(base + ".location.x", loc.getX());
            config.set(base + ".location.y", loc.getY());
            config.set(base + ".location.z", loc.getZ());
        } else {
            config.set(base + ".location.world", "");
            config.set(base + ".location.x", 0.0);
            config.set(base + ".location.y", 0.0);
            config.set(base + ".location.z", 0.0);
        }

        CrateHologramSettings holo = crate.getHologramSettings();
        config.set(base + ".hologram.enabled", holo.isEnabled());
        config.set(base + ".hologram.lines", holo.getLines());
        config.set(base + ".hologram.backgroundColorR", holo.getBackgroundColorR());
        config.set(base + ".hologram.backgroundColorG", holo.getBackgroundColorG());
        config.set(base + ".hologram.backgroundColorB", holo.getBackgroundColorB());
        config.set(base + ".hologram.backgroundAlpha", holo.getBackgroundAlpha());
        config.set(base + ".hologram.textShadow", holo.isTextShadow());
        config.set(base + ".hologram.updateInterval", holo.getUpdateInterval());
        config.set(base + ".hologram.templateName", holo.getTemplateName());

        CrateKey key = crate.getPhysicalKey();
        config.set(base + ".physicalKey.material", key.getMaterial().name());
        config.set(base + ".physicalKey.displayName", key.getDisplayName());
        config.set(base + ".physicalKey.lore", key.getLore() != null ? key.getLore() : new ArrayList<>());
        config.set(base + ".physicalKey.customModelData", key.getCustomModelData());

        for (CrateItem item : crate.getItems().values()) {
            String itemBase = base + ".items." + item.getId();
            config.set(itemBase + ".material", item.getMaterial().name());
            config.set(itemBase + ".displayName", item.getDisplayName());
            config.set(itemBase + ".slot", item.getSlot());
            config.set(itemBase + ".lore", item.getLore() != null ? item.getLore() : new ArrayList<>());
            config.set(itemBase + ".command", item.getCommand() != null ? item.getCommand() : "");
            config.set(itemBase + ".chance", item.getChance());
            config.set(itemBase + ".broadcastOnWin", item.isBroadcastOnWin());
            config.set(itemBase + ".broadcastMessage", item.getBroadcastMessage() != null ? item.getBroadcastMessage() : "");
            List<String> enchantList = new ArrayList<>();
            item.getEnchantments().forEach((e, lvl) -> enchantList.add(e.getKey().getKey().toUpperCase() + ";" + lvl));
            config.set(itemBase + ".enchantments", enchantList);
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crate " + crate.getName(), e);
        }
    }

    public void deleteCrate(String name) {
        crates.remove(name);
        File file = new File(cratesFolder, name + ".yml");
        if (file.exists()) file.delete();
    }

    public Crate copyCrate(String sourceName, String newName) {
        Crate source = crates.get(sourceName);
        if (source == null || crates.containsKey(newName)) return null;
        Crate copy = loadCrateFromSource(source, newName);
        crates.put(newName, copy);
        saveCrate(copy);
        return copy;
    }

    private Crate loadCrateFromSource(Crate source, String newName) {
        Crate copy = new Crate(newName);
        copy.setDisplayName(source.getDisplayName());
        copy.setType(source.getType());
        copy.setRows(source.getRows());
        copy.setFillerEnabled(source.isFillerEnabled());
        copy.setFillerMaterial(source.getFillerMaterial());
        copy.setGuiTitle(source.getGuiTitle());
        copy.setKeepOpenAfterClaim(source.isKeepOpenAfterClaim());
        copy.setHologramSettings(source.getHologramSettings());
        copy.setPhysicalKey(source.getPhysicalKey());
        for (CrateItem item : source.getItems().values()) copy.addItem(item);
        return copy;
    }

    public Crate createCrate(String name) {
        Crate crate = new Crate(name);
        crates.put(name, crate);
        saveCrate(crate);
        return crate;
    }

    public Crate getCrate(String name) {
        return crates.get(name);
    }

    public Crate getCrateAt(Location location) {
        if (location == null) return null;
        for (Crate crate : crates.values()) {
            Location loc = crate.getLocation();
            if (loc == null || loc.getWorld() == null) continue;
            if (loc.getWorld().equals(location.getWorld()) &&
                loc.getBlockX() == location.getBlockX() &&
                loc.getBlockY() == location.getBlockY() &&
                loc.getBlockZ() == location.getBlockZ()) {
                return crate;
            }
        }
        return null;
    }

    public boolean exists(String name) {
        return crates.containsKey(name);
    }

    public Collection<Crate> getAllCrates() {
        return crates.values();
    }

    public Map<String, Crate> getCratesMap() {
        return crates;
    }

    public ItemStack createKeyItem(Crate crate) {
        CrateKey keyModel = crate.getPhysicalKey();
        ItemBuilder builder = new ItemBuilder(keyModel.getMaterial())
                .name(keyModel.getDisplayName())
                .customModelData(keyModel.getCustomModelData());
        if (keyModel.getLore() != null && !keyModel.getLore().isEmpty()) {
            builder.lore(keyModel.getLore());
        }
        ItemStack item = builder.hideFlags().build();
        item.editMeta(meta -> meta.getPersistentDataContainer()
                .set(KEY_CRATE_ID, PersistentDataType.STRING, crate.getName()));
        return item;
    }

    public boolean isKeyFor(ItemStack item, String crateName) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        String stored = meta.getPersistentDataContainer().get(KEY_CRATE_ID, PersistentDataType.STRING);
        return crateName.equals(stored);
    }

    public String getCrateNameFromKey(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        var meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(KEY_CRATE_ID, PersistentDataType.STRING);
    }

    private org.bukkit.inventory.meta.ItemMeta getItemMeta(ItemStack item) {
        return item.getItemMeta();
    }
}
