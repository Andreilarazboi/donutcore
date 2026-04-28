
package ro.andreilarazboi.donutcore.crates.gui;

import java.util.ArrayList;
import java.util.List;
import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.EditorHolder;
import ro.andreilarazboi.donutcore.crates.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CrateRewardsGUI {
    private final DonutCrates plugin;

    public CrateRewardsGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String crateName) {
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (String)(crateName + " Rewards"));
        holder.setInventory(inv);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) {
            fm.setDisplayName(" ");
            filler.setItemMeta(fm);
        }
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, filler);
        }
        ConfigurationSection items = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateName + ".Items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                int slot;
                ConfigurationSection it = items.getConfigurationSection(key);
                if (it == null || (slot = it.getInt("slot", 0)) < 0 || slot >= inv.getSize()) continue;
                ItemStack reward = this.plugin.guiItemUtil.buildItemFromSection(it);
                ItemMeta rewardMeta = reward.getItemMeta();
                if (rewardMeta != null) {
                    ArrayList<String> lore = rewardMeta.hasLore() ? new ArrayList<String>(rewardMeta.getLore()) : new ArrayList<>();
                    lore.add("");
                    lore.add(Utils.formatColors("&#bfbfbfShift + Right-click to move right"));
                    lore.add(Utils.formatColors("&#bfbfbfShift + Left-click to move left"));
                    rewardMeta.setLore(lore);
                    reward.setItemMeta(rewardMeta);
                }
                inv.setItem(slot, reward);
            }
        }
        int backSlot = 45;
        int addSlot = 49;
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) {
            bm.setDisplayName(Utils.formatColors("&#0f99e3Back to Crate Settings"));
            bm.setLore(List.of(Utils.formatColors("&#777777Click to go back to this crate's settings.")));
            back.setItemMeta(bm);
        }
        inv.setItem(backSlot, back);
        ItemStack add = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta am = add.getItemMeta();
        if (am != null) {
            am.setDisplayName(Utils.formatColors("&#0fe30fAdd Reward"));
            am.setLore(List.of(Utils.formatColors("&#777777Drag an item here from your inventory"), Utils.formatColors("&#777777to add it as a new crate reward.")));
            add.setItemMeta(am);
        }
        inv.setItem(addSlot, add);
        return holder.getInventory();
    }
}

