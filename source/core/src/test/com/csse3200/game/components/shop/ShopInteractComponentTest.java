package com.csse3200.game.components.shop;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.ShopInteractComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ShopInteractComponentTest {

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

    private ShopInteractComponent makePlayer(float x, float y, float range) {
        Entity player = new Entity();
        ShopInteractComponent input = new ShopInteractComponent(range);
        player.addComponent(input);
        player.setPosition(new Vector2(x, y));
        return input;
    }

    private Entity makeShop(float x, float y, boolean[] firedFlag) {
        Entity shop = new Entity();
        shop.addComponent(new ShopManager(new CatalogService(new ArrayList<>())));
        shop.getEvents().addListener("interact", () -> firedFlag[0] = true);
        shop.setPosition(new Vector2(x, y));
        entityService.register(shop);
        return shop;
    }

    @Nested
    @DisplayName("Range checks")
    class RangeChecks {

        @Test
        @DisplayName("Interact Keypress in range")
        void triggersInteractWhenInRange() {
            ShopInteractComponent input = makePlayer(0, 0, 5f);
            boolean[] fired = {false};
            makeShop(3, 0, fired); // distance 3 <= range 5

            boolean handled = input.keyPressed(Input.Keys.NUM_0);

            assertTrue(handled, "Key press should be handled when a shop is hit");
            assertTrue(fired[0], "Shop's 'interact' should have fired");
        }

        @Test
        @DisplayName("Within Range check")
        void returnsFalseWhenNoShopInRange() {
            ShopInteractComponent input = makePlayer(0, 0, 5f);
            boolean[] fired = {false};
            makeShop(10, 0, fired); // distance 10 > range 5

            boolean handled = input.keyPressed(Input.Keys.NUM_0);

            assertFalse(handled, "No shop in range => input not handled");
            assertFalse(fired[0], "No shop in range => no interaction should occur");
        }

        @Test
        @DisplayName("Use Nearest Shop")
        void triggersOnlyNearestWhenMultipleInRange() {
            ShopInteractComponent input = makePlayer(0, 0, 6f);
            boolean[] nearHit = {false};
            boolean[] farHit  = {false};
            makeShop(4, 0, farHit);   // distance 4
            makeShop(2, 0, nearHit);  // distance 2 (nearest)

            boolean handled = input.keyPressed(Input.Keys.NUM_0);

            assertTrue(handled, "A shop is in range, so input should be handled");
            assertTrue(nearHit[0], "Nearest shop should be triggered");
            assertFalse(farHit[0], "Farther shop should NOT be triggered");
        }
    }

    @Nested
    @DisplayName("Key handling")
    class KeyHandling {

        @Test
        @DisplayName("Ignores non-zero keys")
        void ignoresNonZeroKeys() {
            ShopInteractComponent input = makePlayer(0, 0, 5f);
            boolean[] fired = {false};
            makeShop(3, 0, fired);

            boolean handled = input.keyPressed(Input.Keys.A);

            assertFalse(handled, "Non-zero keys should be ignored");
            assertFalse(fired[0], "Ignoring key should not trigger interact");
        }
    }
}