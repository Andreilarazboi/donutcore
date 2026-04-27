package ro.andreilarazboi.donutcore.crates.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import ro.andreilarazboi.donutcore.crates.gui.AbstractGUI;

public class CrateInventoryListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof AbstractGUI gui)) return;
        event.setCancelled(true);
        if (event.getClickedInventory() != top) return;
        gui.handleClick(event);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof AbstractGUI) {
            event.setCancelled(true);
        }
    }
}
