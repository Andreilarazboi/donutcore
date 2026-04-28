
package ro.andreilarazboi.donutcore.crates.opening.anim;

import java.util.List;
import java.util.Random;
import ro.andreilarazboi.donutcore.crates.DonutCrates;
import ro.andreilarazboi.donutcore.crates.opening.OpeningAnimationType;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AnimationContext {
    private final DonutCrates plugin;
    private final Player player;
    private final String crate;
    private final OpeningAnimationType type;
    private final Inventory inventory;
    private final List<ItemStack> pool;
    private final ItemStack reward;
    private final Random rng;
    private final Sound tickSound;
    private final Sound plingSound;
    private final Sound winSound;

    public AnimationContext(DonutCrates plugin, Player player, String crate, OpeningAnimationType type, Inventory inventory, List<ItemStack> pool, ItemStack reward, Random rng, Sound tickSound, Sound plingSound, Sound winSound) {
        this.plugin = plugin;
        this.player = player;
        this.crate = crate;
        this.type = type;
        this.inventory = inventory;
        this.pool = pool;
        this.reward = reward;
        this.rng = rng;
        this.tickSound = tickSound;
        this.plingSound = plingSound;
        this.winSound = winSound;
    }

    public DonutCrates plugin() {
        return this.plugin;
    }

    public Player player() {
        return this.player;
    }

    public String crate() {
        return this.crate;
    }

    public OpeningAnimationType type() {
        return this.type;
    }

    public Inventory inv() {
        return this.inventory;
    }

    public List<ItemStack> pool() {
        return this.pool;
    }

    public ItemStack reward() {
        return this.reward;
    }

    public Random rng() {
        return this.rng;
    }

    public Sound tick() {
        return this.tickSound;
    }

    public Sound pling() {
        return this.plingSound;
    }

    public Sound win() {
        return this.winSound;
    }
}

