package ro.andreilarazboi.donutcore.crates.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

import java.util.List;
import java.util.Map;

public class CrateChooseGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;

    public CrateChooseGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse(crate.getGuiTitle()).decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, crate.getSize(), title);

        if (crate.isFillerEnabled()) {
            ItemStack filler = ItemBuilder.filler(crate.getFillerMaterial());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, filler);
            }
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
            for (Map.Entry<Enchantment, Integer> entry : crateItem.getEnchantments().entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (crate.isFillerEnabled() && clicked.getType() == crate.getFillerMaterial()) return;

        int slot = event.getSlot();
        CrateItem selectedItem = null;
        for (CrateItem item : crate.getItems().values()) {
            if (item.getSlot() == slot) {
                selectedItem = item;
                break;
            }
        }

        if (selectedItem == null) return;

        new ConfirmGUI(module, crate, selectedItem).open(player);
    }
}
