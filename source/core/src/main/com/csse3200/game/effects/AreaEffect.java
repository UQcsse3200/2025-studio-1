package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class AreaEffect extends Effect {
    private static final Logger log = LoggerFactory.getLogger(AreaEffect.class);

    public ArrayList<Effect> effects;
    public int radius;

    public AreaEffect(ArrayList<Effect> effects, int radius) {
        this.effects = effects;
        this.radius = radius;
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
