
package ro.andreilarazboi.donutcore.crates.gui;

import java.util.ArrayList;
import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.EditorHolder;
import ro.andreilarazboi.donutcore.crates.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CreateModeGUI {
    private final DonutCrates plugin;

    public CreateModeGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build() {
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)27, (String)Utils.formatColors("&#444444Create Crate Mode"));
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
        inv.setItem(11, this.item(Material.ENCHANTED_BOOK, "&#27B0F5&lRandom Reward", "&#bfbfbfClick to choose this option!"));
        inv.setItem(15, this.item(Material.CHEST, "&#0fe30f&lChoose Reward", "&#bfbfbfClick to choose this option!"));
        inv.setItem(18, this.item(Material.ARROW, "&#f5f5f5\u00ab Back", "&#bfbfbfReturn to crate manager."));
        return inv;
    }

    private ItemStack item(Material material, String name, String ... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Utils.formatColors(name));
            ArrayList<String> lore = new ArrayList<String>();
            for (String line : loreLines) {
                lore.add(Utils.formatColors(line));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}

