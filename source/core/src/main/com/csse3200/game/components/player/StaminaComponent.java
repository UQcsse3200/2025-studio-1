package com.csse3200.game.components.player;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class StaminaComponent extends Component {
    // Stamina Constants
    private int MAX_STAMINA = 100;
    private int INITIAL_STAMINA = 100;
    private float DRAIN_PER_SEC = 30f;
    private float REGEN_PER_SEC = 10f; // stamina/sec when not spending
    private final static float TICK_SEC = 0.1f;
    private long REGEN_DELAY_MS = 800; // time between last spend to regen

    // Stamina management
    private float stamina = INITIAL_STAMINA;
    private boolean infiniteStamina = false;

    private boolean moving = false;
    private boolean sprinting = false;
    private boolean dashing = false;
    private boolean grounded = true;

    private long lastStaminaSpendMs = 0L;
    private int lastEmittedStamina = -1;

    private GameTime time;
    private float tickAccumulator = 0f;

    @Override
    public void create() {
        time = ServiceLocator.getTimeSource();
        emitChanged();
        if (AvatarRegistry.get() != null) {
            float playerSpeed = AvatarRegistry.get().moveSpeed();
            MAX_STAMINA = 100 + Math.round(10f * (playerSpeed - 1f));
            INITIAL_STAMINA = MAX_STAMINA;
            DRAIN_PER_SEC = 18f + 3f * (playerSpeed - 3f);
            REGEN_PER_SEC = 10f - (playerSpeed - 3f);
            REGEN_DELAY_MS = (long) (700 + 100 * (playerSpeed - 3f));

        }
    }

    /**
     * Cancels the repeating stamina task when this component is disposed to prevent leaks.
     */
    @Override
    public void dispose() {
    }

    @Override
    public void update() {
        float dt = time.getDeltaTime();
        if (dt <= 0f) {
            return;
        }

        tickAccumulator += dt;
        while (tickAccumulator >= TICK_SEC) {
            tick();
            tickAccumulator -= TICK_SEC;
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
        lastStaminaSpendMs = time.getTime();
        emitChanged();
        return true;
    }

    /**
     * Returns the current stamina value.
     *
     * @return current stamina
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
     * Drains the player's stamina if they have executed a movement.
     */
    private void checkDrainStamina() {
        final long now = time.getTime();

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
        final long now = time.getTime();
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
