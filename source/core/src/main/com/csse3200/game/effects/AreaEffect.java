package com.csse3200.game.effects;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * An {@link Effect} that applies multiple inner effects to all valid entities
 * within a certain radius of a central entity.
 * <p>
 * {@code AreaEffect} is typically used for area-of-effect (AoE) items,
 * such as explosions, healing zones, or buffs/debuffs that impact
 * multiple nearby entities at once.
 * <p>
 * Validation rules:
 * <ul>
 *   <li>The effect list cannot be {@code null} or empty.</li>
 *   <li>Inner effects cannot contain {@code null} values.</li>
 *   <li>Nested {@code AreaEffect}s are not allowed (to prevent infinite recursion).</li>
 *   <li>Radius must be strictly positive.</li>
 * </ul>
 */
public class AreaEffect extends Effect {

    /**
     * The list of effects to apply to each entity within the area.
     */
    public ArrayList<Effect> effects;

    /**
     * The radius of the area (in world units).
     */
    public int radius;

    /**
     * Creates a new {@code AreaEffect}.
     *
     * @param effects list of effects to apply to entities in range
     * @param radius  radius of the effect area (must be positive)
     * @throws IllegalArgumentException if arguments are invalid (see validation rules)
     */
    public AreaEffect(List<Effect> effects, int radius) {
        if (effects == null) {
            throw new IllegalArgumentException("Effects list cannot be null");
        }
        if (effects.isEmpty()) {
            throw new IllegalArgumentException("Effects list cannot be empty");
        }
        if (effects.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Effects list cannot contain null");
        }
        if (effects.stream().anyMatch(e -> e instanceof AreaEffect)) {
            throw new IllegalArgumentException("Cannot create AreaEffect from an AreaEffect");
        }
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }

        this.effects = new ArrayList<>(effects);
        this.radius = radius;
    }

    /**
     * Gets the list of inner effects that this area effect applies.
     *
     * @return list of effects
     */
    public ArrayList<Effect> getEffects() {
        return effects;
    }

    /**
     * Gets the radius of the area in which this effect applies.
     *
     * @return radius of the effect
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Applies this area effect to all entities within range of the given center entity.
     * <p>
     * Entities are affected if:
     * <ul>
     *   <li>They are within the radius (distance calculated from their center positions).</li>
     *   <li>They have a {@link CombatStatsComponent}.</li>
     *   <li>They are not the player (identified by {@link PlayerActions}).</li>
     * </ul>
     * Each valid entity receives all inner effects in sequence.
     *
     * @param center the entity at the center of the area effect
     * @return {@code true} if at least one effect was successfully applied, otherwise {@code false}
     */
    @Override
    public boolean apply(Entity center) {
        boolean applied = false;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();

        for (Entity entity : entities) {
            float distanceFromCenter = entity.getCenterPosition().dst(center.getCenterPosition());
            boolean isPlayer = entity.hasComponent(PlayerActions.class);

            if (distanceFromCenter <= radius
                    && entity.hasComponent(CombatStatsComponent.class)
                    && !isPlayer) {

                // Apply all inner effects to the entity
                for (Effect effect : effects) {
                    if (effect.apply(entity)) {
                        applied = true;
                    }
                }
            }
        }

        return applied;
    }
}
