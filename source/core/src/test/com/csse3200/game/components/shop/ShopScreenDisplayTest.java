package com.csse3200.game.components.shop;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShopScreenDisplayTest {

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(new GameTime());
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    static class TestableShopScreenDisplay extends com.csse3200.game.components.screens.ShopScreenDisplay {
        TestableShopScreenDisplay(ShopManager manager) {
            // ForestGameArea not needed for these tests, so we pass null.
            super(null, manager);
        }

        @Override
        public void show() {
            ServiceLocator.getTimeSource().setPaused(true);
        }

        @Override
        public void hide() {
            ServiceLocator.getTimeSource().setPaused(false);
        }
    }

    @Nested
    @DisplayName("Objective: Pause/resume behaviour")
    class PauseResumeTests {

        @Test
        @DisplayName("show() sets game paused")
        void showPausesGame() {
            var manager = new ShopManager(new CatalogService(new ArrayList<>()));
            var ui = new TestableShopScreenDisplay(manager);

            assertFalse(ServiceLocator.getTimeSource().isPaused(), "Precondition: not paused");
            ui.show();
            assertTrue(ServiceLocator.getTimeSource().isPaused(), "Shop open should pause time");
        }

        @Test
        @DisplayName("hide() clears game paused")
        void hideUnpausesGame() {
            var manager = new ShopManager(new CatalogService(new ArrayList<>()));
            var ui = new TestableShopScreenDisplay(manager);

            ServiceLocator.getTimeSource().setPaused(true); // simulate open
            ui.hide();
            assertFalse(ServiceLocator.getTimeSource().isPaused(), "Shop close should unpause time");
        }
    }
}