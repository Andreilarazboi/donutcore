package ro.andreilarazboi.donutcore.crates.animation;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.*;

public class CrateAnimation implements InventoryHolder {

    private final CratesModule module;
    private final Player player;
    private final Crate crate;
    private final CrateItem winner;
    private Inventory inventory;

    private static final int CENTER_SLOT = 4;
    private static final int[] SPIN_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int TOTAL_SPINS = 30;
    private static final long INITIAL_SPEED = 2L;

    public CrateAnimation(CratesModule module, Player player, Crate crate, CrateItem winner) {
        this.module = module;
        this.player = player;
        this.crate = crate;
        this.winner = winner;
    }

    public void start() {
        Component title = ColorUtil.parse("&8&lOpening " + crate.getDisplayName() + "&8...")
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 9, title);
        player.openInventory(inventory);

        List<CrateItem> items = new ArrayList<>(crate.getItems().values());
        if (items.isEmpty()) {
            module.giveReward(player, crate, winner);
            return;
        }

        new BukkitRunnable() {
            int tick = 0;
            int spinCount = 0;
            int currentSlot = 0;
            long delay = INITIAL_SPEED;

            @Override
            public void run() {
                if (!player.isOnline() || !inventory.equals(player.getOpenInventory().getTopInventory())) {
                    cancel();
                    return;
                }

                if (spinCount >= TOTAL_SPINS) {
                    finish();
                    cancel();
                    return;
                }

                ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
                for (int i = 0; i < 9; i++) inventory.setItem(i, filler);

                CrateItem displayed = spinCount >= TOTAL_SPINS - 1
                        ? winner
                        : items.get(spinCount % items.size());

                ItemStack displayItem = new ItemBuilder(displayed.getMaterial())
                        .name(displayed.getDisplayName())
                        .hideFlags()
                        .build();

                inventory.setItem(CENTER_SLOT, displayItem);

                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f + (spinCount / (float) TOTAL_SPINS));

                spinCount++;
                tick++;

                if (tick % 10 == 0) delay = Math.min(delay + 1, 10L);

                try {
                    this.runTaskLater(module.getPlugin(), delay);
                    cancel();
                } catch (IllegalStateException ignored) {}
            }

            private void finish() {
                ItemStack filler = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
                for (int i = 0; i < 9; i++) inventory.setItem(i, filler);

                ItemStack winnerItem = new ItemBuilder(winner.getMaterial())
                        .name(winner.getDisplayName())
                        .hideFlags()
                        .build();
                inventory.setItem(CENTER_SLOT, winnerItem);

                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> {
                    player.closeInventory();
                    module.giveReward(player, crate, winner);
                }, 40L);
            }
        }.runTask(module.getPlugin());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
