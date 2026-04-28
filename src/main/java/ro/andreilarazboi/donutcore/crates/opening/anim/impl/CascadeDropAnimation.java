
package ro.andreilarazboi.donutcore.crates.opening.anim.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ro.andreilarazboi.donutcore.crates.opening.anim.BaseAnimation;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CascadeDropAnimation
extends BaseAnimation {
    private final int[] cols = new int[]{2, 3, 4, 5, 6};
    private final int[] rows = new int[]{1, 2, 3, 4};
    private List<Integer> stageSlots;
    private final Set<Integer> covered = new HashSet<Integer>();
    private final Map<Integer, ItemStack> baseItem = new HashMap<Integer, ItemStack>();
    private int winnerSlot = -1;
    private int chosenCol = 0;
    private int dropletRowIdx = -1;
    private int targetRowIdx = -1;
    private int lastDropletSlot = -1;

    @Override
    public int inventorySize() {
        return 54;
    }

    @Override
    protected void onInit() {
        this.fillAll(Material.BLACK_STAINED_GLASS_PANE);
        this.stageSlots = new ArrayList<Integer>();
        for (int r : this.rows) {
            for (int c : this.cols) {
                this.stageSlots.add(r * 9 + c);
            }
        }
        this.winnerSlot = this.stageSlots.get(this.ctx.rng().nextInt(this.stageSlots.size()));
        java.util.Iterator<Integer> object = this.stageSlots.iterator();
        while (object.hasNext()) {
            int s = object.next();
            this.baseItem.put(s, this.randomFromPool());
            this.inv.setItem(s, this.safeClone(this.baseItem.get(s)));
        }
    }

    @Override
    protected void onTick() {
        int remainingToCover = 0;
        for (int s : this.stageSlots) {
            if (s == this.winnerSlot || this.covered.contains(s)) continue;
            ++remainingToCover;
        }
        if (remainingToCover <= 0) {
            for (int s : this.stageSlots) {
                this.inv.setItem(s, this.pane(Material.BLACK_STAINED_GLASS_PANE));
            }
            this.inv.setItem(this.winnerSlot, this.safeClone(this.ctx.reward()));
            this.finishWin();
            return;
        }
        if (this.dropletRowIdx == -1) {
            this.chosenCol = this.cols[this.ctx.rng().nextInt(this.cols.length)];
            this.targetRowIdx = -1;
            for (int i = this.rows.length - 1; i >= 0; --i) {
                int slot = this.rows[i] * 9 + this.chosenCol;
                if (slot == this.winnerSlot || this.covered.contains(slot)) continue;
                this.targetRowIdx = i;
                break;
            }
            if (this.targetRowIdx == -1) {
                this.wait = 0;
                return;
            }
            this.dropletRowIdx = 0;
            this.lastDropletSlot = -1;
        }
        int dropletSlot = this.rows[this.dropletRowIdx] * 9 + this.chosenCol;
        if (this.lastDropletSlot != -1 && this.lastDropletSlot != this.winnerSlot && !this.covered.contains(this.lastDropletSlot)) {
            this.inv.setItem(this.lastDropletSlot, this.safeClone(this.baseItem.get(this.lastDropletSlot)));
        }
        this.inv.setItem(dropletSlot, this.pane(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
        this.lastDropletSlot = dropletSlot;
        this.ctx.player().playSound(this.ctx.player().getLocation(), this.ctx.tick(), 0.5f, 1.05f);
        if (this.dropletRowIdx >= this.targetRowIdx) {
            this.covered.add(dropletSlot);
            this.inv.setItem(dropletSlot, this.pane(Material.BLUE_STAINED_GLASS_PANE));
            this.inv.setItem(this.winnerSlot, this.safeClone(this.baseItem.get(this.winnerSlot)));
            this.dropletRowIdx = -1;
            this.targetRowIdx = -1;
            this.lastDropletSlot = -1;
            this.wait = this.cascadeDelay(remainingToCover);
            return;
        }
        ++this.dropletRowIdx;
        this.wait = 0;
    }

    private int cascadeDelay(int remainingToCover) {
        if (remainingToCover >= 14) {
            return 0;
        }
        if (remainingToCover >= 10) {
            return 1;
        }
        if (remainingToCover >= 7) {
            return 2;
        }
        if (remainingToCover >= 5) {
            return 4;
        }
        return 6;
    }
}

