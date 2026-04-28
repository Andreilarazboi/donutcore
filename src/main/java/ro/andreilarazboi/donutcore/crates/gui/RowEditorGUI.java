
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

public class RowEditorGUI {
    private final DonutCrates plugin;

    public RowEditorGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String crate) {
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)27, (String)Utils.formatColors("&#444444" + crate + " Rows"));
        holder.setInventory(inv);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, filler);
        }
        int current = this.plugin.cfg.crates.getInt("Crates." + crate + ".rows", 3);
        current = Math.max(1, Math.min(6, current));
        for (int i = 1; i <= 6; ++i) {
            int slot = 9 + (i - 1);
            ItemStack it = new ItemStack(Material.CHEST);
            ItemMeta im = it.getItemMeta();
            im.setDisplayName(Utils.formatColors("&#f5f5f5" + i + " Rows"));
            ArrayList<String> lore = new ArrayList<String>();
            lore.add(Utils.formatColors((String)(i == current ? "&#0fe30fCurrently selected." : "&#bfbfbfClick to use &f" + i + "&#bfbfbf rows.")));
            im.setLore(lore);
            it.setItemMeta(im);
            inv.setItem(slot, it);
        }
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(Utils.formatColors("&#f5f5f5\u00ab Back"));
        ArrayList<String> bl = new ArrayList<String>();
        bl.add(Utils.formatColors("&#bfbfbfReturn to crate settings."));
        bm.setLore(bl);
        back.setItemMeta(bm);
        inv.setItem(18, back);
        return inv;
    }
}

