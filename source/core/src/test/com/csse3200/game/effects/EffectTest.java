package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EffectTest {

    @Test
    void createEffect() {
        Effect effect = new Effect() {
            @Override
            public boolean apply(Entity entity) {
                return false;
            }
        };

        assertEquals(Effect.class, effect.getClass());
    }
}
