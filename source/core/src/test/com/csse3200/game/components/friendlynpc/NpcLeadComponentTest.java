package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies begin() behavior and no-throw safety for empty/non-empty paths without ticking time.
 */
class NpcLeadComponentTest {

    @Test
    void beginOnEmptyPathIsNoOpAndDoesNotThrow() {
        Entity npc = new Entity();
        npc.setPosition(0f, 0f);

        NpcLeadComponent lead = new NpcLeadComponent(List.of(), 2.0f, 0.25f);
        npc.addComponent(lead);

        // Should not crash even if there are no waypoints.
        assertDoesNotThrow(lead::begin);

        // Without update(), position should remain the same.
        assertEquals(0f, npc.getPosition().x, 1e-6);
        assertEquals(0f, npc.getPosition().y, 1e-6);
    }

    @Test
    void beginWithPathDoesNotThrow_andCanBeCalledOnceSafely() {
        Entity npc = new Entity();
        npc.setPosition(0f, 0f);

        NpcLeadComponent lead = new NpcLeadComponent(
                List.of(new Vector2(3f, 0f), new Vector2(3f, 2f)),
                2.0f,
                0.25f
        );
        npc.addComponent(lead);

        // Kicks off leading; we don't call update() (no TimeSource), just ensure it's safe.
        assertDoesNotThrow(lead::begin);
    }
}
