
package ro.andreilarazboi.donutcore.crates.opening.anim;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import ro.andreilarazboi.donutcore.crates.opening.OpeningAnimationType;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.CarouselAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.CascadeDropAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.CenterPulseAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.CircleSpinEatAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.DoubleSpinMatchAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.ExplosionEliminationAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.FlickerLockAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.MatrixFillAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.RowSpinAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.SpiralEliminationAnimation;
import ro.andreilarazboi.donutcore.crates.opening.anim.impl.WaveSweepAnimation;

public class OpeningAnimationRegistry {
    private final Map<OpeningAnimationType, Supplier<OpeningAnimation>> map = new EnumMap<OpeningAnimationType, Supplier<OpeningAnimation>>(OpeningAnimationType.class);

    public OpeningAnimationRegistry() {
        this.map.put(OpeningAnimationType.ROW_SPIN, RowSpinAnimation::new);
        this.map.put(OpeningAnimationType.CAROUSEL, CarouselAnimation::new);
        this.map.put(OpeningAnimationType.CENTER_PULSE, CenterPulseAnimation::new);
        this.map.put(OpeningAnimationType.FLICKER_LOCK, FlickerLockAnimation::new);
        this.map.put(OpeningAnimationType.CIRCLE_SPIN, CircleSpinEatAnimation::new);
        this.map.put(OpeningAnimationType.SPIRAL_REVEAL, SpiralEliminationAnimation::new);
        this.map.put(OpeningAnimationType.WAVE_SWEEP, WaveSweepAnimation::new);
        this.map.put(OpeningAnimationType.EXPLOSION_REVEAL, ExplosionEliminationAnimation::new);
        this.map.put(OpeningAnimationType.CASCADE_DROP, CascadeDropAnimation::new);
        this.map.put(OpeningAnimationType.MATRIX_RAIN, MatrixFillAnimation::new);
        this.map.put(OpeningAnimationType.DOUBLE_SPIN, DoubleSpinMatchAnimation::new);
    }

    public OpeningAnimation create(OpeningAnimationType type) {
        Supplier<OpeningAnimation> sup = this.map.get((Object)type);
        return sup == null ? null : sup.get();
    }
}

