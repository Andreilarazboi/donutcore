package ro.andreilarazboi.donutcore.crates.model;

import org.bukkit.Material;

import java.util.List;

public class CrateKey {

    private Material material = Material.TRIPWIRE_HOOK;
    private String displayName = "&6Crate Key";
    private List<String> lore;
    private int customModelData = 0;

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }

    public int getCustomModelData() { return customModelData; }
    public void setCustomModelData(int customModelData) { this.customModelData = customModelData; }
}
