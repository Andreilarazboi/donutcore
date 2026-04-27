package ro.andreilarazboi.donutcore.crates.model;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.LinkedHashMap;
import java.util.Map;

public class Crate {

    private final String name;
    private String displayName;
    private CrateType type;
    private int rows;
    private boolean fillerEnabled;
    private Material fillerMaterial;
    private String guiTitle;
    private boolean keepOpenAfterClaim;
    private Location location;
    private final Map<String, CrateItem> items = new LinkedHashMap<>();
    private CrateKey physicalKey;
    private CrateHologramSettings hologramSettings;

    public Crate(String name) {
        this.name = name;
        this.displayName = "&6" + name;
        this.type = CrateType.CHOOSE;
        this.rows = 3;
        this.fillerEnabled = true;
        this.fillerMaterial = Material.GRAY_STAINED_GLASS_PANE;
        this.guiTitle = "&8&lChoose 1 item";
        this.keepOpenAfterClaim = false;
        this.physicalKey = new CrateKey();
        this.hologramSettings = new CrateHologramSettings();
    }

    public String getName() { return name; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public CrateType getType() { return type; }
    public void setType(CrateType type) { this.type = type; }

    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public boolean isFillerEnabled() { return fillerEnabled; }
    public void setFillerEnabled(boolean fillerEnabled) { this.fillerEnabled = fillerEnabled; }

    public Material getFillerMaterial() { return fillerMaterial; }
    public void setFillerMaterial(Material fillerMaterial) { this.fillerMaterial = fillerMaterial; }

    public String getGuiTitle() { return guiTitle; }
    public void setGuiTitle(String guiTitle) { this.guiTitle = guiTitle; }

    public boolean isKeepOpenAfterClaim() { return keepOpenAfterClaim; }
    public void setKeepOpenAfterClaim(boolean keepOpenAfterClaim) { this.keepOpenAfterClaim = keepOpenAfterClaim; }

    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }

    public Map<String, CrateItem> getItems() { return items; }

    public void addItem(CrateItem item) { items.put(item.getId(), item); }
    public void removeItem(String id) { items.remove(id); }

    public CrateKey getPhysicalKey() { return physicalKey; }
    public void setPhysicalKey(CrateKey physicalKey) { this.physicalKey = physicalKey; }

    public CrateHologramSettings getHologramSettings() { return hologramSettings; }
    public void setHologramSettings(CrateHologramSettings hologramSettings) { this.hologramSettings = hologramSettings; }

    public int getSize() { return rows * 9; }
}
