package ro.andreilarazboi.donutcore.crates.model;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CrateItem {

    private String id;
    private Material material;
    private String displayName;
    private List<String> lore;
    private Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
    private int slot;
    private String command;
    private double chance;
    private boolean broadcastOnWin;
    private String broadcastMessage;

    public CrateItem(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Material getMaterial() { return material; }
    public void setMaterial(Material material) { this.material = material; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public List<String> getLore() { return lore; }
    public void setLore(List<String> lore) { this.lore = lore; }

    public Map<Enchantment, Integer> getEnchantments() { return enchantments; }
    public void setEnchantments(Map<Enchantment, Integer> enchantments) { this.enchantments = enchantments; }

    public int getSlot() { return slot; }
    public void setSlot(int slot) { this.slot = slot; }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public double getChance() { return chance; }
    public void setChance(double chance) { this.chance = chance; }

    public boolean isBroadcastOnWin() { return broadcastOnWin; }
    public void setBroadcastOnWin(boolean broadcastOnWin) { this.broadcastOnWin = broadcastOnWin; }

    public String getBroadcastMessage() { return broadcastMessage; }
    public void setBroadcastMessage(String broadcastMessage) { this.broadcastMessage = broadcastMessage; }
}
