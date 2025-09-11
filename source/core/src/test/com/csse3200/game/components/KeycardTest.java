package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.player.InventoryComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeycardTest {

    private Entity player;
    private InventoryComponent inventory;

    @BeforeEach
    void setUp() {
        player = new Entity();
        inventory = new InventoryComponent(1);
        player.addComponent(inventory);
    }

    @Test
    void testKeycardPickupAddsToInventory() {
        KeycardPickupComponent pickup = new KeycardPickupComponent(1);
        Entity keycard = new Entity().addComponent(pickup);


        pickup.simulateCollisionWith(player);

        assertEquals(1, inventory.getKeycardLevel());
    }

    @Test
    void testGateStaysLockedWithoutKeycard() {
        KeycardGateComponent gate = new KeycardGateComponent(1, () -> {});
        Entity door = new Entity().addComponent(gate);

        gate.simulateCollisionWith(player);

        assertFalse(gate.isUnlocked());
    }
}