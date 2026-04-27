package ro.andreilarazboi.donutcore.crates.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.Map;

public class CratePreviewGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;

    public CratePreviewGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Preview: &r" + crate.getDisplayName())
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, crate.getSize(), title);

        if (crate.isFillerEnabled()) {
            ItemStack filler = ItemBuilder.filler(crate.getFillerMaterial());
            for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, filler);
        }

        for (CrateItem crateItem : crate.getItems().values()) {
            int slot = crateItem.getSlot();
            if (slot < 0 || slot >= inventory.getSize()) continue;
            inventory.setItem(slot, buildItemStack(crateItem));
        }

        player.openInventory(inventory);
    }

    private ItemStack buildItemStack(CrateItem crateItem) {
        ItemBuilder builder = new ItemBuilder(crateItem.getMaterial())
                .name(crateItem.getDisplayName());

        if (crateItem.getLore() != null && !crateItem.getLore().isEmpty()) {
            builder.lore(crateItem.getLore());
        }

        ItemStack item = builder.hideFlags().build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Map.Entry<Enchantment, Integer> e : crateItem.getEnchantments().entrySet()) {
                meta.addEnchant(e.getKey(), e.getValue(), true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
