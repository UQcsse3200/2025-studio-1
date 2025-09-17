package com.csse3200.game.components.items;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.effects.AreaEffect;
import com.csse3200.game.effects.Effect;

import java.util.ArrayList;

/**
 * Component representing a consumable item that can apply one or more {@link Effect}s
 * to an entity when used.
 * <p>
 * A consumable may directly apply effects (e.g., healing, buffs), or it may
 * trigger projectile-like behavior if the entity also has a
 * {@link WeaponsStatsComponent}.
 * <p>
 * Effects can also be nested within an {@link AreaEffect}, which bundles
 * multiple effects together.
 */
public class ConsumableComponent extends Component {
    /**
     * The list of effects applied by this consumable.
     */
    private final ArrayList<Effect> effects;

    /**
     * The duration (in ticks or seconds, depending on game logic) for which
     * the consumable's effects remain active.
     */
    private final int duration;

    /**
     * Creates a consumable component with the specified effects and duration.
     *
     * @param effects  list of effects this consumable applies
     * @param duration how long the effects last
     */
    public ConsumableComponent(ArrayList<Effect> effects, int duration) {
        this.effects = effects;
        this.duration = duration;
    }

    /**
     * Retrieves all effects associated with this consumable.
     *
     * @return the list of effects
     */
    public ArrayList<Effect> getEffects() {
        return effects;
    }

    /**
     * Retrieves the first effect of the specified type from this consumable.
     * <p>
     * If the consumable contains an {@link AreaEffect}, its inner effects are
     * also searched recursively.
     *
     * @param effectClass the class of effect to search for
     * @return the first matching effect, or {@code null} if none found
     */
    public Effect getEffect(Class<? extends Effect> effectClass) {
        for (Effect e : effects) {
            if (e instanceof AreaEffect area) {
                for (Effect inner : area.getEffects()) {
                    if (effectClass.isInstance(inner)) {
                        return inner;
                    }
                }
            }
            if (effectClass.isInstance(e)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the duration of this consumable's effects.
     *
     * @return duration of effects
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Checks whether this consumable triggers projectile behavior.
     * <p>
     * A consumable is considered to fire a projectile if its entity
     * has a {@link WeaponsStatsComponent}.
     *
     * @return {@code true} if the consumable fires a projectile,
     *         {@code false} otherwise
     */
    public boolean firesProjectile() {
        return entity.hasComponent(WeaponsStatsComponent.class);
    }
}
