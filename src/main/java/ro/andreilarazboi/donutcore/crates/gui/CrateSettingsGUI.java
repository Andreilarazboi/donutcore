
package ro.andreilarazboi.donutcore.crates.gui;

import java.util.ArrayList;
import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.EditorHolder;
import ro.andreilarazboi.donutcore.crates.Utils;
import ro.andreilarazboi.donutcore.crates.opening.OpeningAnimationType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CrateSettingsGUI {
    private final DonutCrates plugin;

    public CrateSettingsGUI(DonutCrates plugin) {
        this.plugin = plugin;
    }

    public Inventory build(String crateName) {
        EditorHolder holder = new EditorHolder();
        Inventory inv = Bukkit.createInventory((InventoryHolder)holder, (int)36, (String)Utils.formatColors("&#444444" + crateName + " Settings"));
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
        String keyId = this.plugin.getKeyIdForCrate(crateName);
        boolean keyMissing = !this.plugin.keyExists(keyId);
        boolean animEnabled = this.plugin.cfg.crates.getBoolean("Crates." + crateName + ".opening-animation.enabled", false);
        String typeId = this.plugin.cfg.crates.getString("Crates." + crateName + ".opening-animation.type", OpeningAnimationType.ROW_SPIN.id());
        OpeningAnimationType type = OpeningAnimationType.byId(typeId);
        inv.setItem(4, this.item(Material.BOOK, "&#0f99e3&lCrate Settings", "&#bfbfbfManage core crate features.", "&#7f7f7f(advanced actions are in Edit Crate)"));
        inv.setItem(11, this.item(Material.CHEST, "&#0fe30f&lRewards", "&#bfbfbfEdit crate rewards in a separate menu."));
        inv.setItem(13, this.item(Material.ENDER_CHEST, "&#0f99e3&lPreview", "&#bfbfbfOpen a preview of this crate."));
        inv.setItem(15, this.item(Material.ENDER_EYE, "&#f5f5f5&lHologram", "&#bfbfbfConfigure hologram text and style."));
        inv.setItem(20, this.item(Material.NETHER_STAR, "&#f5f5f5&lEdit Crate", "&#bfbfbfRename ID, move, rows,", "&#bfbfbfdisplay name, random mode, copy/delete."));
        ArrayList<String> animLore = new ArrayList<String>();
        animLore.add(Utils.formatColors("&#bfbfbfChoose how the crate opens."));
        animLore.add(Utils.formatColors(""));
        animLore.add(Utils.formatColors("&#27B0F5Enabled: " + (animEnabled ? "&#0fe30fYES" : "&#d61111NO")));
        animLore.add(Utils.formatColors("&#27B0F5Selected: &f" + (type != null ? type.displayName() : "ROW_SPIN")));
        animLore.add(Utils.formatColors(""));
        animLore.add(Utils.formatColors("&#bfbfbfClick to configure."));
        ItemStack animItem = new ItemStack(animEnabled ? Material.AMETHYST_SHARD : Material.QUARTZ);
        ItemMeta aim = animItem.getItemMeta();
        if (aim != null) {
            aim.setDisplayName(Utils.formatColors("&#0f99e3&lOpening Animations"));
            aim.setLore(animLore);
            animItem.setItemMeta(aim);
        }
        inv.setItem(22, animItem);
        ArrayList<String> keyLore = new ArrayList<String>();
        keyLore.add(Utils.formatColors("&#27B0F5Selected: &f" + keyId));
        keyLore.add(Utils.formatColors(""));
        keyLore.add(Utils.formatColors(keyMissing ? "&#d61111Selected key is missing. Click to fix." : "&#bfbfbfClick to change the selected key."));
        ItemStack keyItem = new ItemStack(Material.TRIPWIRE_HOOK);
        ItemMeta kim = keyItem.getItemMeta();
        if (kim != null) {
            kim.setDisplayName(Utils.formatColors("&#0fe30f&lKey"));
            kim.setLore(keyLore);
            keyItem.setItemMeta(kim);
        }
        inv.setItem(24, keyItem);
        inv.setItem(27, this.item(Material.ARROW, "&#f5f5f5\u00ab Back", "&#bfbfbfReturn to the &#0fe30fCrate Manager&#bfbfbf."));
        return inv;
    }

    private ItemStack item(Material mat, String name, String ... loreLines) {
        ItemStack i = new ItemStack(mat);
        ItemMeta im = i.getItemMeta();
        if (im != null) {
            im.setDisplayName(Utils.formatColors(name));
            if (loreLines.length > 0) {
                ArrayList<String> lore = new ArrayList<String>();
                for (String s : loreLines) {
                    lore.add(Utils.formatColors(s));
                }
                im.setLore(lore);
            }
            i.setItemMeta(im);
        }
        return i;
    }
}

