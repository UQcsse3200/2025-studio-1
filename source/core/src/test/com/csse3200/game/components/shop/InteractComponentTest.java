package com.csse3200.game.components.shop;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.player.InteractComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class InteractComponentTest {
    private final Vector2 origin = new Vector2(0, 0);

    private EntityService entityService;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        entityService = new EntityService();
        ServiceLocator.registerEntityService(entityService);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    private Entity makePlayer() {
        Entity player = new Entity()
                .addComponent(new InteractComponent());
        player.setPosition(origin);
        entityService.register(player);
        player.create();
        return player;
    }

    private void makeShop(float x, boolean[] firedFlag) {
        Entity shop = new Entity();
        shop.setPosition(new Vector2(x, (float) 0));
        shop.setInteractable(true);
        shop.getEvents().addListener("interact", () -> firedFlag[0] = true);
        entityService.register(shop);
    }

    @Nested
    @DisplayName("Range checks")
    class RangeChecks {

        @Test
        @DisplayName("Triggers interact when in range")
        void triggersInteractWhenInRange() {
            Entity player = makePlayer();
            boolean[] fired = {false};
            makeShop(1.5f, fired); // within INTERACT_RANGE (2.0)

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
        @DisplayName("Triggers all shops within range")
        void triggersAllShopsInRange() {
            Entity player = makePlayer();
            boolean[] fired1 = {false};
            boolean[] fired2 = {false};
            makeShop(1.0f, fired1);
            makeShop(1.5f, fired2);

            player.getEvents().trigger("interact");

            assertTrue(fired1[0], "First shop in range should fire");
            assertTrue(fired2[0], "Second shop in range should fire");
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
}
