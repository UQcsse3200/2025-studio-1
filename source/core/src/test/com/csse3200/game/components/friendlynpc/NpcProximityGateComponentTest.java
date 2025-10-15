package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Checks in-range logic against entity/player positions.
 */
class NpcProximityGateComponentTest {

    @Test
    void detectsPlayerWithinRadius() {
        Entity player = new Entity();
        Entity npc = new Entity();

        player.setPosition(0f, 0f);
        npc.setPosition(3f, 4f); // distance 5

        NpcProximityGateComponent gate = new NpcProximityGateComponent(player, 5f);
        npc.addComponent(gate);

        assertTrue(gate.isPlayerInRange());

        npc.setPosition(6f, 8f); // distance 10
        assertFalse(gate.isPlayerInRange());
    }

    @Test
    void nullPlayerIsNotInRange() {
        Entity npc = new Entity();
        NpcProximityGateComponent gate = new NpcProximityGateComponent(null, 3f);
        npc.addComponent(gate);
        assertFalse(gate.isPlayerInRange());
    }
}
