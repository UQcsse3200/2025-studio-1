package com.csse3200.game.components.player;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;

public class StaminaComponent extends Component {

    private Timer.Task task;

    // Stamina Constants
    private static final int MAX_STAMINA = 100;
    private static final int INITIAL_STAMINA = 100;
    private static final float DRAIN_PER_SEC = 30f;
    private static final float REGEN_PER_SEC = 10f; // stamina/sec when not spending
    private static final float TICK_SEC = 0.1f;
    private static final long REGEN_DELAY_MS = 800; // time between last spend to regen

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

    /**
     * Returns whether the player has enough stamina to perform the movement.
     *
     * @param amount the stamina cost
     * @return if the player has sufficient stamina
     */
    public boolean hasStamina(float amount) {
        return infiniteStamina || stamina >= amount;
    }

    /**
     * Attempt to execute a movement.
     *
     * @param amount the amount of stamina required.
     * @return whether the movement can be completed.
     */
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

    /**
     * Returns the current stamina value.
     *
     * @return current stamina (floating point)
     */
    public float getStamina() {
        return stamina;
    }

    /**
     * Sets the current stamina value, clamped between 0 and MAX_STAMINA.
     * Triggers a UI update if the integer value changes.
     *
     * @param value new stamina value
     */
    public void setStamina(float value) {
        float clamped = Math.max(0f, Math.min(MAX_STAMINA, value));
        if ((int) clamped != (int) stamina) {
            stamina = clamped;
            emitChanged();
        } else {
            stamina = clamped;
        }
    }

    /**
     * Updates the infiniteStamina parameter
     *
     * @param infiniteStamina whether the player should have infinite stamina.
     */
    public void setInfiniteStamina(boolean infiniteStamina) {
        this.infiniteStamina = infiniteStamina;
        if (infiniteStamina) {
            stamina = MAX_STAMINA;
            emitChanged();
        }
    }

    /**
     * Set if the player is moving
     */
    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    /**
     * Set if the player is sprinting
     */
    public void setSprinting(boolean sprinting) {
        this.sprinting = sprinting;
    }

    /**
     * Set if the player is dashing
     */
    public void setDashing(boolean dashing) {
        this.dashing = dashing;
    }

    /**
     * Set if the player is grounded
     */
    public void setGrounded(boolean grounded) {
        this.grounded = grounded;
    }

    /**
     * Starts (or restarts) the repeating stamina update task.
     * Uses libGDX Timer so that ticks are posted onto the main render thread.
     * If a task is already running it will be cancelled and replaced.
     */
    private void startTask() {
        if (task != null) task.cancel();
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                tick();
            }
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
        if (infiniteStamina) {
            stamina = MAX_STAMINA;
            emitChanged();
            return;
        }
        checkDrainStamina();
        regenerateStamina();
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
        // Keep a live copy in the global cache for safe cross-area restoration
        try {
            com.csse3200.game.services.ServiceLocator.setCachedPlayerStamina(stamina);
        } catch (Exception ignored) {
        }
    }

    /**
     * Drains the player's stamina if they have executed a movement.
     */
    private void checkDrainStamina() {
        final long now = System.currentTimeMillis();

        final boolean draining = sprinting && moving && grounded && !dashing;
        if (draining && stamina > 0f) {
            // Drain the player's stamina
            final float drain = DRAIN_PER_SEC * TICK_SEC;
            // Ensure stamina is not below 0.
            stamina = Math.max(0f, stamina - drain);
            lastStaminaSpendMs = now;
            if ((int) stamina == 0) {
                // No stamina, ensure sprint is stopped
                entity.getEvents().trigger("outOfStamina");
                sprinting = false;
                entity.getEvents().trigger("sprintStop");
            }
            emitChanged();
        }
    }

    /**
     * Regenerate stamina if player is standing or walking.
     */
    private void regenerateStamina() {
        final long now = System.currentTimeMillis();
        if (!dashing && (now - lastStaminaSpendMs) >= REGEN_DELAY_MS) {
            // No stamina expending event occurred
            final float before = stamina;
            stamina = Math.min(MAX_STAMINA, stamina + REGEN_PER_SEC * TICK_SEC);
            if ((int) stamina != (int) before) {
                emitChanged();
            }
        }
    }
}
