
package ro.andreilarazboi.donutcore.crates.opening.anim;


public interface OpeningAnimation {
    public int inventorySize();

    public void init(AnimationContext var1);

    public boolean tick();

    default public long postWinDelayTicks() {
        return 90L;
    }
}

