
package ro.andreilarazboi.donutcore.crates;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class InventoryCloseListener
implements Listener {
    private final DonutCrates plugin;

    public InventoryCloseListener(DonutCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        UUID uid;
        String crate;
        CrateHolder ch;
        HumanEntity humanEntity = e.getPlayer();
        if (!(humanEntity instanceof Player)) {
            return;
        }
        Player p = (Player)humanEntity;
        InventoryHolder inventoryHolder = e.getInventory().getHolder();
        if (inventoryHolder instanceof CrateHolder && (ch = (CrateHolder)inventoryHolder).isPreview() && (crate = this.plugin.previewReturnCrate.remove(uid = p.getUniqueId())) != null) {
            Bukkit.getScheduler().runTask((Plugin)this.plugin.getPlugin(), () -> p.openInventory(this.plugin.guiCrateSettings.build(crate)));
        }
    }
}

