package ro.andreilarazboi.donutcore.crates.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

public class ItemDeleteConfirmGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;
    private final CrateItem item;

    public ItemDeleteConfirmGUI(CratesModule module, Crate crate, CrateItem item) {
        this.module = module;
        this.crate = crate;
        this.item = item;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&c&lDelete Item?").decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 27, title);

        ItemStack border = ItemBuilder.filler(Material.RED_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inventory.setItem(i, border);

        inventory.setItem(11, new ItemBuilder(Material.BARRIER)
                .name("&c&lYES, DELETE")
                .lore("&7Delete item &e" + ColorUtil.strip(item.getDisplayName()),
                      "&cThis cannot be undone!")
                .build());

        inventory.setItem(13, new ItemBuilder(item.getMaterial())
                .name(item.getDisplayName())
                .build());

        inventory.setItem(15, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .name("&a&lCANCEL")
                .build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        if (slot == 11) {
            crate.removeItem(item.getId());
            module.getCrateManager().saveCrate(crate);
            player.sendMessage(ColorUtil.colorize("&aItem &e" + item.getId() + " &adeleted."));
            new CrateEditorGUI(module, crate).open(player);
        } else if (slot == 15) {
            new CrateEditorGUI(module, crate).open(player);
        }
    }
}
