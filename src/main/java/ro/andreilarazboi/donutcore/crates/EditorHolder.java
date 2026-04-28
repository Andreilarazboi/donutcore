
package ro.andreilarazboi.donutcore.crates;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class EditorHolder
implements InventoryHolder {
    private Inventory inv;

    public void setInventory(Inventory inv) {
        this.inv = inv;
    }

    public Inventory getInventory() {
        return this.inv;
    }
}

