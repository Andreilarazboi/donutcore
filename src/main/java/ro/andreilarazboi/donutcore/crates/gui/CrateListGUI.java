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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrateListGUI extends AbstractGUI {

    private final CratesModule module;
    private final List<Crate> crateList;

    public CrateListGUI(CratesModule module) {
        this.module = module;
        this.crateList = new ArrayList<>(module.getCrateManager().getAllCrates());
    }

    @Override
    public void open(Player player) {
        int size = Math.min(54, (int) (Math.ceil(crateList.size() / 9.0) + 1) * 9);
        if (size < 9) size = 9;

        Component title = ColorUtil.parse("&8Crate Manager").decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, size, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < size; i++) inventory.setItem(i, border);

        for (int i = 0; i < crateList.size() && i < size; i++) {
            Crate crate = crateList.get(i);
            ItemStack icon = new ItemBuilder(Material.CHEST)
                    .name(crate.getDisplayName())
                    .lore(
                        "&7Type: &e" + crate.getType().name(),
                        "&7Items: &e" + crate.getItems().size(),
                        "&7Rows: &e" + crate.getRows(),
                        "",
                        "&eClick &7to edit this crate"
                    )
                    .build();
            inventory.setItem(i, icon);
        }

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        if (clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        int slot = event.getSlot();
        if (slot < crateList.size()) {
            Crate crate = crateList.get(slot);
            new CrateEditorGUI(module, crate).open(player);
        }
    }
}
