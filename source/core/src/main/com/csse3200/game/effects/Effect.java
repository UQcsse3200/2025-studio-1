package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;

/**
 * Base class for all gameplay effects that can be applied to entities.
 * <p>
 * Effects represent changes to an entity's state (e.g., health,
 * stats, buffs, debuffs, or area-of-effect behavior).
 * Subclasses must implement {@link #apply(Entity)} to define
 * how the effect modifies the target entity.
 */
public abstract class Effect {

    /**
     * Creates a new effect.
     * <p>
     * This base constructor exists so subclasses can extend
     * {@code Effect} without requiring their own explicit
     * constructor if no initialization is needed.
     */
    public Effect() {
    }

    /**
     * Applies this effect to the given entity.
     * <p>
     * Implementations should define what happens when the effect
     * is applied. If the effect cannot be applied (e.g., the entity
     * is missing required components), the method should return
     * {@code false}.
     *
     * @param entity the target entity
     * @return {@code true} if the effect was successfully applied,
     * {@code false} otherwise
     */
    public abstract boolean apply(Entity entity);
}
