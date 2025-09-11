package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;
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

    @Override
    public boolean apply(Entity center) {
        // for entities around center and in radius
        boolean applied = false;
        for (Effect effect : effects) {
            if (effect.apply(center)) { // replace center with iterative entity
                applied = true;
            }
        }
        return applied;
    }
}
