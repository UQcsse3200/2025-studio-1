package com.csse3200.game.components.shop;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.player.InteractComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InteractComponentTest {
    private final Vector2 origin = new Vector2(0, 0);

    private EntityService entityService;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        entityService = new EntityService();
        ServiceLocator.registerEntityService(entityService);
        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        Body physicsBody = mock(Body.class);
        when(physicsEngine.createBody(org.mockito.ArgumentMatchers.any())).thenReturn(physicsBody);
        ServiceLocator.registerPhysicsService(new PhysicsService(physicsEngine));
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    private Entity makePlayer() {
        Entity player = new Entity().addComponent(new InteractComponent());
        player.setPosition(origin);
        entityService.register(player);
        return player;
    }

    private void makeShop(float x, boolean[] firedFlag) {
        Entity shop = InteractableStationFactory.createBaseStation();
        shop.setPosition(new Vector2(x, (float) 0));
        entityService.register(shop);
        shop.getEvents().addListener("interact", () -> firedFlag[0] = true);
    }

    @Test
    @DisplayName("Triggers interact when in range")
    void triggersInteractWhenInRange() {
        Entity player = makePlayer();
        boolean[] fired = {false};
        makeShop(1.0f, fired); // within INTERACT_RANGE (2.0)

        // Trigger interaction via the component
        player.getEvents().trigger("interact");

        assertTrue(fired[0], "Shop's 'interact' should have fired when in range");
    }


    @Test
    @DisplayName("Does nothing when no shop in range")
    void doesNothingWhenOutOfRange() {
        Entity player = makePlayer();
        boolean[] fired = {false};
        makeShop(5f, fired); // out of range

        player.getEvents().trigger("interact");

        assertFalse(fired[0], "No shop in range => no interaction should occur");
    }

    @Test
    @DisplayName("Ignores non-interactable entities")
    void ignoresNonInteractables() {
        Entity player = makePlayer();
        boolean[] fired = {false};

        Entity notAShop = new Entity();
        notAShop.setPosition(new Vector2(1.0f, 0));
        notAShop.setInteractable(false); // explicitly non-interactable
        notAShop.getEvents().addListener("interact", () -> fired[0] = true);
        entityService.register(notAShop);

        player.getEvents().trigger("interact");

        assertFalse(fired[0], "Non-interactable entity should not fire");
    }
}
