
package ro.andreilarazboi.donutcore.crates.gui;

import java.util.ArrayList;
import java.util.List;
import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.EditorHolder;
import ro.andreilarazboi.donutcore.crates.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class HologramTemplateGUI {
    private final DonutCrates plugin;
    private static final int[] TEMPLATE_SLOTS = new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public HologramTemplateGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String crate) {
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)54, (String)Utils.formatColors("&#444444" + crate + " Hologram Templates"));
        holder.setInventory(inv);
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta fm = filler.getItemMeta();
        fm.setDisplayName(" ");
        filler.setItemMeta(fm);
        for (int i = 0; i < inv.getSize(); ++i) {
            inv.setItem(i, filler);
        }
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(Utils.formatColors("&#f5f5f5\u00ab Back"));
        ArrayList<String> bl = new ArrayList<String>();
        bl.add(Utils.formatColors("&#bfbfbfReturn to the hologram editor."));
        bm.setLore(bl);
        back.setItemMeta(bm);
        inv.setItem(45, back);
        ItemStack header = new ItemStack(Material.BOOK);
        ItemMeta hm = header.getItemMeta();
        hm.setDisplayName(Utils.formatColors("&#0f99e3Select a Hologram Template"));
        ArrayList<String> hl = new ArrayList<String>();
        hl.add(Utils.formatColors("&#bfbfbfClick a template below to"));
        hl.add(Utils.formatColors("&#bfbfbfchange how this crate's"));
        hl.add(Utils.formatColors("&#bfbfbfhologram text looks."));
        hl.add("");
        hl.add(Utils.formatColors("&#bfbfbfYou can edit and add templates"));
        hl.add(Utils.formatColors("&#bfbfbfin &fconfig.yml &#bfbfbfunder"));
        hl.add(Utils.formatColors("&f'hologram-templates'&#bfbfbf."));
        hm.setLore(hl);
        header.setItemMeta(hm);
        inv.setItem(4, header);
        ConfigurationSection root = this.plugin.cfg.config.getConfigurationSection("hologram-templates");
        String currentTemplate = this.plugin.cfg.crates.getString("Crates." + crate + ".Hologram.template", null);
        if (currentTemplate == null && root != null && root.isConfigurationSection("default")) {
            currentTemplate = "default";
        }
        if (root == null || root.getKeys(false).isEmpty()) {
            ItemStack none = new ItemStack(Material.BARRIER);
            ItemMeta nm = none.getItemMeta();
            nm.setDisplayName(Utils.formatColors("&#d61111No Templates Defined"));
            ArrayList<String> nl = new ArrayList<String>();
            nl.add(Utils.formatColors("&#bfbfbfAdd templates under &f'hologram-templates'"));
            nl.add(Utils.formatColors("&#bfbfbfin &fconfig.yml &7to use this menu."));
            nm.setLore(nl);
            none.setItemMeta(nm);
            inv.setItem(22, none);
            return inv;
        }
        int idx = 0;
        for (String id : root.getKeys(false)) {
            boolean isSelected;
            if (idx >= TEMPLATE_SLOTS.length) break;
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) continue;
            Material mat = Material.ENDER_EYE;
            ArrayList<String> lore = new ArrayList<String>();
            boolean bl2 = isSelected = currentTemplate != null && currentTemplate.equalsIgnoreCase(id);
            if (isSelected) {
                lore.add(Utils.formatColors("&#0fe30f\u2714 Currently selected"));
            } else {
                lore.add(Utils.formatColors("&#bfbfbfClick to use this template."));
            }
            List templateLines = sec.getStringList("lines");
            if (!templateLines.isEmpty()) {
                lore.add(Utils.formatColors(""));
                lore.add(Utils.formatColors("&#bfbfbfPreview:"));
                int shown = Math.min(3, templateLines.size());
                for (int i2 = 0; i2 < shown; ++i2) {
                    lore.add(Utils.formatColors("&7\u2022 &f" + (String)templateLines.get(i2)));
                }
                if (templateLines.size() > 3) {
                    lore.add(Utils.formatColors("&7..."));
                }
            }
            ItemStack icon = new ItemStack(mat);
            ItemMeta im = icon.getItemMeta();
            im.setDisplayName(Utils.formatColors("&#0fe30fTemplate: &f" + id));
            im.setLore(lore);
            if (isSelected) {
                im.addEnchant(Enchantment.UNBREAKING, 1, true);
                im.addItemFlags(new ItemFlag[]{ItemFlag.HIDE_ENCHANTS});
            }
            icon.setItemMeta(im);
            inv.setItem(TEMPLATE_SLOTS[idx++], icon);
        }
        return inv;
    }
}

