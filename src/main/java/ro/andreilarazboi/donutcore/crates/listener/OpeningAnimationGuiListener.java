
package ro.andreilarazboi.donutcore.crates.listener;

import ro.andreilarazboi.donutcore.crates.opening.OpeningAnimationHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class OpeningAnimationGuiListener
implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        boolean inTop;
        if (!(e.getWhoClicked() instanceof Player)) {
            return;
        }
        int topSize = e.getView().getTopInventory().getSize();
        inTop = e.getRawSlot() < topSize;
        if (e.getView().getTopInventory().getHolder() instanceof OpeningAnimationHolder && inTop) {
            e.setCancelled(true);
        }
    }
}

