
package ro.andreilarazboi.donutcore.crates.opening;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class OpeningAnimationHolder
implements InventoryHolder {
    private final String crateName;
    private Inventory inventory;

    public OpeningAnimationHolder(String crateName) {
        this.crateName = crateName;
    }

    public String getCrateName() {
        return this.crateName;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inv) {
        this.inventory = inv;
    }
}

