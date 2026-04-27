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
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

public class DeleteConfirmGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;

    public DeleteConfirmGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&c&lDelete Crate?").decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 27, title);

        ItemStack border = ItemBuilder.filler(Material.RED_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) inventory.setItem(i, border);

        inventory.setItem(11, new ItemBuilder(Material.BARRIER)
                .name("&c&lYES, DELETE")
                .lore("&7This will permanently delete",
                      "&7crate &e" + ColorUtil.strip(crate.getDisplayName()),
                      "&cThis cannot be undone!")
                .build());

        inventory.setItem(13, new ItemBuilder(Material.CHEST)
                .name(crate.getDisplayName())
                .lore("&7Deleting this crate...")
                .build());

        inventory.setItem(15, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE)
                .name("&a&lCANCEL")
                .lore("&7Go back without deleting")
                .build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        if (slot == 11) {
            module.getHologramManager().removeHologram(crate.getName());
            module.getCrateManager().deleteCrate(crate.getName());
            player.closeInventory();
            player.sendMessage(ColorUtil.colorize("&aCrate &e" + crate.getName() + " &adeleted."));
            new CrateListGUI(module).open(player);
        } else if (slot == 15) {
            new CrateEditorGUI(module, crate).open(player);
        }
    }
}
