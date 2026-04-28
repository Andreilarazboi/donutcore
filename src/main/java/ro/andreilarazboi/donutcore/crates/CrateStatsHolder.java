
package ro.andreilarazboi.donutcore.crates;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CrateStatsHolder
implements InventoryHolder {
    private final String crate;
    private final boolean historyView;
    private Inventory inventory;

    public CrateStatsHolder(String crate, boolean historyView) {
        this.crate = crate;
        this.historyView = historyView;
    }

    public String getCrate() {
        return this.crate;
    }

    public boolean isHistoryView() {
        return this.historyView;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }
}

