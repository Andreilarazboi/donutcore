
package ro.andreilarazboi.donutcore.crates.gui;

import java.util.ArrayList;
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

public class CrateEditorGUI {
    private final DonutCrates plugin;

    public CrateEditorGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String crateName) {
        int rows = this.plugin.cfg.crates.getInt("Crates." + crateName + ".rows", 3);
        rows = Math.max(1, Math.min(6, rows));
        int editorRows = Math.min(rows + 1, 6);
        int size = editorRows * 9;
        int rewardAreaSize = rows * 9;
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)size, (String)Utils.formatColors("&#444444" + crateName + " Rewards"));
        holder.setInventory(inv);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < size; ++i) {
            inv.setItem(i, filler);
        }
        ConfigurationSection items = this.plugin.cfg.crates.getConfigurationSection("Crates." + crateName + ".Items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                ConfigurationSection it = items.getConfigurationSection(key);
                int slot = it.getInt("slot", 0);
                if (slot < 0 || slot >= rewardAreaSize) continue;
                ItemStack is = this.plugin.guiItemUtil.buildItemFromSection(it);
                ItemMeta im = is.getItemMeta();
                ArrayList<String> lore = im != null && im.hasLore() ? new ArrayList<String>(im.getLore()) : new ArrayList<String>();
                double chance = it.getDouble("chance", 0.0);
                lore.add("");
                if (chance > 0.0) {
                    lore.add(Utils.formatColors("&#27B0F5Chance: &f" + chance + "%"));
                } else {
                    lore.add(Utils.formatColors("&#27B0F5Chance: &fAuto (equal weight)"));
                }
                lore.add(Utils.formatColors("&#bfbfbfClick to edit reward."));
                lore.add(Utils.formatColors("&#bfbfbfShift-Left/Right to move."));
                if (im == null) {
                    im = is.getItemMeta();
                }
                im.setLore(lore);
                is.setItemMeta(im);
                inv.setItem(slot, is);
            }
        }
        int bottomRowStart = (editorRows - 1) * 9;
        inv.setItem(bottomRowStart, this.named(Material.ARROW, "&#f5f5f5\u00ab Back", "&#bfbfbfReturn to crate settings."));
        int center = bottomRowStart + 4;
        inv.setItem(center, this.named(Material.PURPLE_STAINED_GLASS_PANE, "&#991be1&lAdd Reward", "&#bfbfbfDrag an item from your inventory", "&#bfbfbfonto this slot to add it as a reward."));
        return holder.getInventory();
    }

    private ItemStack named(Material m, String name, String ... lore) {
        ItemStack i = new ItemStack(m);
        ItemMeta im = i.getItemMeta();
        im.setDisplayName(Utils.formatColors(name));
        if (lore.length > 0) {
            ArrayList<String> l = new ArrayList<String>();
            for (String s : lore) {
                l.add(Utils.formatColors(s));
            }
            im.setLore(l);
        }
        i.setItemMeta(im);
        return i;
    }
}

