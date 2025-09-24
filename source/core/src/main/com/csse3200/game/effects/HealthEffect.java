package com.csse3200.game.effects;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;

/**
 * An {@link Effect} that modifies an entity's health by applying
 * damage or healing.
 * <p>
 * The effect decreases the target entity's health by the specified
 * {@code damage} amount when applied. A negative damage value can
 * be used to heal the entity instead.
 */
public class HealthEffect extends Effect {
    /**
     * The amount of health to remove (or add, if negative).
     */
    private final int damage;

    /**
     * Creates a new health effect.
     *
     * @param damage the amount of health to subtract from the target entity;
     *               use negative values to heal
     */
    public HealthEffect(int damage) {
        this.damage = damage;
    }

    /**
     * Applies the health effect to the given entity.
     * <p>
     * If the entity has a {@link CombatStatsComponent}, its health is reduced
     * by {@code damage}. Otherwise, the effect has no impact.
     *
     * @param entity the entity receiving the effect
     * @return {@code true} if the effect was applied,
     * {@code false} if the entity had no combat stats
     */
    @Override
    public boolean apply(Entity entity) {
        if (!entity.hasComponent(CombatStatsComponent.class)) {
            return false;
        }

        CombatStatsComponent combatStats = entity.getComponent(CombatStatsComponent.class);
        combatStats.setHealth(combatStats.getHealth() - damage);
        return true;
    }
}
