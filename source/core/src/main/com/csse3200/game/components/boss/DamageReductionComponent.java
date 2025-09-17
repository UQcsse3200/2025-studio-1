package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Adds a damage reduction component
 * while activated, reduces the "determined damage" by a certain percentage (1 = complete immunity)
 * over a specified duration.
 * Does not modify health or affect "damage output"
 * requires calling {@link #apply(int)} before deducting health.
 */
public class DamageReductionComponent extends Component {
    private final GameTime time = ServiceLocator.getTimeSource();
    private boolean active = false;
    private float reduction = 0f;
    private long endAtMs = 0L;

    /**
     * Enable damage reduction
     * {@code reduction} ∈ [0,1]
     * {@code durationSec} is the duration in seconds.
     */
    public void start(float reduction, float durationSec) {
        this.active = true;
        this.reduction = Math.max(0f, Math.min(1f, reduction));
        this.endAtMs = time.getTime() + (long) (durationSec * 1000);
    }

    public void stop() {
        active = false;
        reduction = 0f;
        endAtMs = 0L;
    }

    @Override
    public void update() {
        if (active && time.getTime() >= endAtMs) {
            stop();
        }
    }

    /**
     * Applies a reduction to incoming damage
     * returns unchanged if not active or {@code incoming <= 0}.
     * The return value is the rounded damage.
     */
    public int apply(int incoming) {
        if (!active || incoming <= 0) {
            return incoming;
        }
        float keep = 1f - reduction;
        return Math.round(incoming * keep);
    }

    public boolean isActive() {
        return active;
    }
}