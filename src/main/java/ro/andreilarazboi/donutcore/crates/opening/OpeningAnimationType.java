
package ro.andreilarazboi.donutcore.crates.opening;

import java.util.Locale;
import org.bukkit.Material;

public enum OpeningAnimationType {
    ROW_SPIN("ROW_SPIN", "Row Spin", Material.RAIL, "Classic horizontal spin with dramatic reveal"),
    CIRCLE_SPIN("CIRCLE_SPIN", "Circle Spin", Material.ENDER_PEARL, "Spins around border with glowing trail"),
    CAROUSEL("CAROUSEL", "Carousel", Material.REDSTONE, "Roulette strip with pulsing winner"),
    CENTER_PULSE("CENTER_PULSE", "Center Pulse", Material.NETHER_STAR, "Explosive pulse from center"),
    FLICKER_LOCK("FLICKER_LOCK", "Flicker Lock", Material.AMETHYST_SHARD, "Rapid flicker with instant lock"),
    SPIRAL_REVEAL("SPIRAL_REVEAL", "Spiral Reveal", Material.END_ROD, "Items spiral inward to reveal winner"),
    WAVE_SWEEP("WAVE_SWEEP", "Wave Sweep", Material.CYAN_DYE, "Wave animation with rainbow trail"),
    EXPLOSION_REVEAL("EXPLOSION_REVEAL", "Explosion Reveal", Material.TNT, "Items explode then converge on winner"),
    CASCADE_DROP("CASCADE_DROP", "Cascade Drop", Material.WATER_BUCKET, "Cascading waterfall effect"),
    MATRIX_RAIN("MATRIX_RAIN", "Matrix Rain", Material.LIME_DYE, "Matrix-style digital rain"),
    DOUBLE_SPIN("DOUBLE_SPIN", "Double Spin", Material.ECHO_SHARD, "Dual spinning rows that collide");

    private final String id;
    private final String displayName;
    private final Material icon;
    private final String description;

    private OpeningAnimationType(String id, String displayName, Material icon, String description) {
        this.id = id;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
    }

    public String id() {
        return this.id;
    }

    public String displayName() {
        return this.displayName;
    }

    public Material icon() {
        return this.icon;
    }

    public String description() {
        return this.description;
    }

    public static OpeningAnimationType byId(String id) {
        if (id == null) {
            return ROW_SPIN;
        }
        String norm = id.trim().toUpperCase(Locale.ROOT);
        for (OpeningAnimationType t : OpeningAnimationType.values()) {
            if (!t.id.equalsIgnoreCase(norm)) continue;
            return t;
        }
        return ROW_SPIN;
    }
}

