package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal safety/initialisation checks for HoverBobComponent that do not require a TimeSource.
 */
class HoverBobComponentTest {

    @Test
    void createAnchorsToInitialY_andNoCrashWithoutTimeSource() {
        Entity e = new Entity();
        e.setPosition(1f, 2f);

        // Use any amplitude/speed; we won't call update() (which needs TimeSource).
        HoverBobComponent bob = new HoverBobComponent(0.1f, (float) (Math.PI * 2));
        e.addComponent(bob);

        // Should not throw; just captures anchor from current Y.
        assertDoesNotThrow(bob::create);

        // Position unchanged by create()
        assertEquals(1f, e.getPosition().x, 1e-6);
        assertEquals(2f, e.getPosition().y, 1e-6);
    }
}
