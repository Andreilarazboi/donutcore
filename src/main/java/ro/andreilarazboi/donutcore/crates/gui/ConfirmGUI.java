
package ro.andreilarazboi.donutcore.crates.gui;

import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ConfirmGUI {
    private final DonutCrates plugin;

    public ConfirmGUI(DonutCrates pl) {
        this.plugin = pl;
    }

    public Inventory build(Player p, ItemStack clicked, String crateName) {
        ConfigurationSection c = this.plugin.cfg.config.getConfigurationSection("confirm-menu");
        int rows = c.getInt("rows", 3);
        boolean fill = c.getBoolean("fillerEnabled", false);
        Material filler = Material.valueOf((String)c.getString("fillerMaterial", "GRAY_STAINED_GLASS_PANE"));
        Inventory inv = Bukkit.createInventory(null, (int)(rows * 9), (String)Utils.formatColors(c.getString("title")));
        if (fill) {
            ItemStack f = new ItemStack(filler);
            ItemMeta fm = f.getItemMeta();
            fm.setDisplayName(" ");
            f.setItemMeta(fm);
            for (int i = 0; i < rows * 9; ++i) {
                inv.setItem(i, f);
            }
        }
        ItemStack copy = clicked.clone();
        ItemMeta cm = copy.getItemMeta();
        cm.setDisplayName(Utils.formatColors(c.getString("ClickedItem.displayname", "%ClickedItemName%").replace("%ClickedItemName%", clicked.getItemMeta() != null && clicked.getItemMeta().hasDisplayName() ? clicked.getItemMeta().getDisplayName() : clicked.getType().name())));
        copy.setItemMeta(cm);
        inv.setItem(c.getInt("ClickedItem.slot", 13), copy);
        ItemStack con = new ItemStack(Material.valueOf((String)c.getString("Confirm.material", "LIME_STAINED_GLASS_PANE")));
        ItemMeta cmeta = con.getItemMeta();
        cmeta.setDisplayName(Utils.formatColors(c.getString("Confirm.displayname", "&aConfirm")));
        cmeta.setLore(Utils.formatColors(c.getStringList("Confirm.lore")));
        con.setItemMeta(cmeta);
        inv.setItem(c.getInt("Confirm.slot", 15), con);
        ItemStack dec = new ItemStack(Material.valueOf((String)c.getString("Decline.material", "RED_STAINED_GLASS_PANE")));
        ItemMeta dmeta = dec.getItemMeta();
        dmeta.setDisplayName(Utils.formatColors(c.getString("Decline.displayname", "&cDecline")));
        dmeta.setLore(Utils.formatColors(c.getStringList("Decline.lore")));
        dec.setItemMeta(dmeta);
        inv.setItem(c.getInt("Decline.slot", 11), dec);
        return inv;
    }
}

