package com.csse3200.game.components;

import com.badlogic.gdx.utils.Timer;

public class StaminaComponent extends Component {

    private Timer.Task task;

    // Stamina Constants
    private static final int MAX_STAMINA = 100;
    private static final int INITIAL_STAMINA = 100;
    private static final float DRAIN_PER_SEC = 30f;
    private static final float REGEN_PER_SEC = 10f; // stamina/sec when not spending
    private static final float TICK_SEC = 0.1f;

    // Stamina management
    private float stamina = INITIAL_STAMINA;
    private boolean infiniteStamina = false;

    private boolean moving = false;
    private boolean sprinting = false;
    private boolean dashing = false;
    private boolean grounded = true;

    private long lastStaminaSpendMs = 0L;
    private int lastEmittedStamina = -1;

    @Override
    public void create() {
        startTask();
        emitChanged();
    }

    /**
     * Cancels the repeating stamina task when this component is disposed to prevent leaks.
     */
    @Override
    public void dispose() {
        if (task != null) {
            task.cancel();
        }
    }

    public boolean hasStamina(float amount) {
        return infiniteStamina || stamina >= amount;
    }

    public boolean trySpend(float amount) {
        // Check edge case or debug mode
        if (amount <= 0 || infiniteStamina) {
            return true;
        }
        if (stamina < amount) {
            // Insufficient stamina
            return false;
        }

        // Update stamina values
        stamina -= amount;
        lastStaminaSpendMs = System.currentTimeMillis();
        emitChanged();
        return true;
    }

    public float getStamina() {
        return stamina;
    }

    public void setInfiniteStamina(boolean infiniteStamina) {
        this.infiniteStamina = infiniteStamina;
        if (infiniteStamina) {
            stamina = MAX_STAMINA;
            emitChanged();
        }
    }
    public void setMoving(boolean v) {
        moving = v;
    }

    public void setSprinting(boolean v) {
        sprinting = v;
    }

    public void setDashing(boolean v) {
        dashing = v;
    }

    public void setGrounded(boolean v) {
        grounded = v;
    }

    /**
     * Starts (or restarts) the repeating stamina update task.
     * Uses libGDX Timer so that ticks are posted onto the main render thread.
     * Idempotent: if a task is already running it will be cancelled and replaced.
     */
    private void startTask() {
        if (task != null) task.cancel();
        task = Timer.schedule(new Timer.Task() {
            @Override public void run() { tick(); }
        }, TICK_SEC, TICK_SEC);
    }


    /**
     * One stamina "tick". Called at a fixed cadence by {@link #startTask()} ()}.
     * Checks for horizontal movement. Jumps, dashes and other special movement actions
     * stamina changes are implemented in the handlers respectively.
     * <p>
     * Rules:
     * - If {@code infiniteStamina} is enabled, keep stamina pegged at MAX and notify UI.
     * - While sprinting and moving horizontally (and not dashing), drain stamina.
     * - If not spending for {@code STAMINA_REGEN_DELAY_MS}, regenerate up to MAX.
     * - UI is only notified when the integer stamina value actually changes.
     */
    private void tick() {
        final long now = System.currentTimeMillis();

        if (infiniteStamina) {
            stamina = MAX_STAMINA;
            emitChanged();
            return;
        }

        final boolean draining = sprinting && moving && grounded && !dashing;
        if (draining && stamina > 0f) {
            final float drain = DRAIN_PER_SEC * TICK_SEC;
            stamina = Math.max(0f, stamina - drain);
            lastStaminaSpendMs = now;
            if ((int) stamina == 0) {
                entity.getEvents().trigger("outOfStamina");
                sprinting = false;
                entity.getEvents().trigger("sprintStop");
            }
            emitChanged();
            return;
        }

        if (!dashing && (now - lastStaminaSpendMs) >= REGEN_PER_SEC) {
            final float before = stamina;
            stamina = Math.min(MAX_STAMINA, stamina + REGEN_PER_SEC * TICK_SEC);
            if ((int) stamina != (int) before) {
                emitChanged();
            }
        }
    }

    /**
     * Emits a stamina change event to the UI layer, but only if the integer value
     * has actually changed since the last emission. This avoids redundant UI work.
     */
    private void emitChanged() {
        final int curr = (int) stamina;
        if (curr == lastEmittedStamina) {
            return;
        }
        lastEmittedStamina = curr;
        entity.getEvents().trigger("staminaChanged", curr, MAX_STAMINA);
    }
}
