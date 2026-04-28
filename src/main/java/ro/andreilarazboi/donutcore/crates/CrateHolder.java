
package ro.andreilarazboi.donutcore.crates;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CrateHolder
implements InventoryHolder {
    private final String crateName;
    private final boolean preview;
    private Inventory inventory;

    public CrateHolder(String crateName, boolean preview) {
        this.crateName = crateName;
        this.preview = preview;
    }

    public String getCrateName() {
        return this.crateName;
    }

    public boolean isPreview() {
        return this.preview;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}

