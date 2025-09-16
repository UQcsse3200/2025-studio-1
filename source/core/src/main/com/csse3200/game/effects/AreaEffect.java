package com.csse3200.game.effects;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AreaEffect extends Effect {

    public ArrayList<Effect> effects;
    public int radius;

    // Attempts to initialise an AreaEffect and checks for invalid arguments
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


    public ArrayList<Effect> getEffects() {
        return effects;
    }

    public int getRadius() {
        return radius;
    }

    @Override
    public boolean apply(Entity center) {
        boolean applied = false;
        // Find entities in range
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
        for (Entity entity : entities) {
            float distanceFromCenter = entity.getCenterPosition().dst(center.getCenterPosition());
            boolean isPlayer = entity.hasComponent(PlayerActions.class);
            if (distanceFromCenter <= radius && entity.hasComponent(CombatStatsComponent.class) && !isPlayer) {

                // Applies effects to entities
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
