package ro.andreilarazboi.donutcore.crates.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.MessageUtil;

import java.util.Map;

public class CrateBlockListener implements Listener {

    private final CratesModule module;

    public CrateBlockListener(CratesModule module) {
        this.module = module;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();

        if (module.isMoveBlockMode(player)) {
            String crateName = module.getMoveBlockTarget(player);
            Crate crate = module.getCrateManager().getCrate(crateName);
            if (crate != null) {
                event.setCancelled(true);
                crate.setLocation(event.getClickedBlock().getLocation());
                module.getCrateManager().saveCrate(crate);
                module.getHologramManager().respawnHologram(crate);
                module.removeMoveBlockMode(player);
                player.sendMessage(MessageUtil.get("crate-moved",
                        Map.of("crate", crate.getName())));
            }
            return;
        }

        Crate crate = module.getCrateManager().getCrateAt(event.getClickedBlock().getLocation());
        if (crate == null) return;

        event.setCancelled(true);

        if (!player.hasPermission("donutcore.crate.use")) {
            MessageUtil.send(player, "no-permission");
            return;
        }

        if (!module.getKeyManager().hasKey(player, crate)) {
            MessageUtil.send(player, "no-keys", Map.of("crate", crate.getDisplayName()));
            MessageUtil.sendActionBar(player,
                    "&cYou need a key for &e" + crate.getDisplayName() + "&c!");
            return;
        }

        module.openCrate(player, crate);
    }
}
