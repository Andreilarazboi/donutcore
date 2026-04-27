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

import java.util.Map;

public class ConfirmGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;
    private final CrateItem selectedItem;

    private static final int CONFIRM_SLOT = 11;
    private static final int ITEM_SLOT = 13;
    private static final int DECLINE_SLOT = 15;

    public ConfirmGUI(CratesModule module, Crate crate, CrateItem selectedItem) {
        this.module = module;
        this.crate = crate;
        this.selectedItem = selectedItem;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Confirm").decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 27, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inventory.setItem(i, border);

        ItemStack confirm = new ItemBuilder(Material.LIME_STAINED_GLASS_PANE)
                .name("&a&lCONFIRM")
                .lore("&7Click to claim this reward!")
                .build();
        inventory.setItem(CONFIRM_SLOT, confirm);

        inventory.setItem(ITEM_SLOT, buildSelectedItemStack());

        ItemStack decline = new ItemBuilder(Material.RED_STAINED_GLASS_PANE)
                .name("&c&lDECLINE")
                .lore("&7Click to go back.")
                .build();
        inventory.setItem(DECLINE_SLOT, decline);

        player.openInventory(inventory);
    }

    private ItemStack buildSelectedItemStack() {
        ItemBuilder builder = new ItemBuilder(selectedItem.getMaterial())
                .name(selectedItem.getDisplayName());

        if (selectedItem.getLore() != null && !selectedItem.getLore().isEmpty()) {
            builder.lore(selectedItem.getLore());
        }

        ItemStack item = builder.hideFlags().build();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            for (Map.Entry<Enchantment, Integer> e : selectedItem.getEnchantments().entrySet()) {
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
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();

        if (slot == CONFIRM_SLOT) {
            player.closeInventory();
            module.giveReward(player, crate, selectedItem);
        } else if (slot == DECLINE_SLOT) {
            new CrateChooseGUI(module, crate).open(player);
        }
    }
}
