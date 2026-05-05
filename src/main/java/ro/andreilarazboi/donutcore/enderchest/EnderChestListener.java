package ro.andreilarazboi.donutcore.enderchest;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnderChestListener implements Listener {
    private final DonutEnderChest plugin;

    public EnderChestListener(DonutEnderChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENDER_CHEST) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        plugin.getEnderChestManager().openOwnChest(player);
    }
}
