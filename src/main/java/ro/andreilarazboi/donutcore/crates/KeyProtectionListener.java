
package ro.andreilarazboi.donutcore.crates;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class KeyProtectionListener
implements Listener {
    private final DonutCrates plugin;
    private final NamespacedKey keyTag;

    public KeyProtectionListener(DonutCrates plugin) {
        this.plugin = plugin;
        this.keyTag = new NamespacedKey((Plugin)plugin.getPlugin(), "crate_key");
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack hand = e.getItemInHand();
        if (hand == null || hand.getType().isAir()) {
            return;
        }
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) {
            return;
        }
        if (meta.getPersistentDataContainer().has(this.keyTag, PersistentDataType.STRING)) {
            e.setCancelled(true);
            String raw = this.plugin.cfg.config.getString("messages.cannot-place-key", "&#ff5555You cannot place crate keys as blocks!");
            this.plugin.msg(e.getPlayer(), raw);
        }
    }
}

