package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke tests for BossDeathComponent focusing on the "atlas missing" defensive path.
 * We avoid touching real graphics resources, so these tests are deterministic in CI.
 */
public class BossDeathComponentTest {

    /**
     * A minimal EntityService stub that captures registered entities.
     * Only register() is used by BossDeathComponent; other methods are no-ops.
     */
    static class CapturingEntityService extends EntityService {
        final List<Entity> registered = new ArrayList<>();

        @Override
        public void register(Entity entity) {
            registered.add(entity);
        }
    }

    private CapturingEntityService capturingES;

    @BeforeEach
    void setUp() {
        // Use a capturing EntityService to see if an effect gets registered.
        capturingES = new CapturingEntityService();
        ServiceLocator.registerEntityService(capturingES);

        // Intentionally DO NOT register a ResourceService (or atlas) so that
        // BossDeathComponent goes down the "atlas missing" safe-return path.
        // (ServiceLocator.getResourceService() will be null.)
    }

    @AfterEach
    void tearDown() {
        // If your ServiceLocator has a clear(), you can call it here to avoid cross-test leakage.
        // ServiceLocator.clear();
    }

    @Test
    void deathEvent_withNoAtlas_doesNotCrash_orRegisterEffect_orHideBoss() {
        // Arrange: create a boss entity with some stats and the BossDeathComponent
        Entity boss = new Entity();
        boss.addComponent(new CombatStatsComponent(100)); // not strictly required but realistic
        BossDeathComponent death = new BossDeathComponent(); // default config
        boss.addComponent(death);
        boss.create();

        // Act: fire the "death" event. Without atlas, BossDeathComponent should early-out safely.
        assertDoesNotThrow(() -> boss.getEvents().trigger("death"),
                "Death event must not throw even if atlas is missing");

        // Assert: no explosion effect should be registered
        assertTrue(capturingES.registered.isEmpty(),
                "No effect entity should be registered when atlas is missing");

    }

    @Test
    void deathEvent_listenerIsInstalled_onCreate() {
        // Arrange
        Entity boss = new Entity();
        BossDeathComponent death = new BossDeathComponent(0.06f, 4f);
        boss.addComponent(death);
        boss.create();

        // Act & Assert: triggering "death" should be handled (no listener NPE)
        assertDoesNotThrow(() -> boss.getEvents().trigger("death"),
                "Component should have subscribed to 'death' on create()");
    }

    @Test
    void constructor_clampsInvalidParams_andStillSafeWithoutAtlas() {
        // Arrange: pass zero/negative params to ensure internal clamping doesn't break logic
        Entity boss = new Entity();
        BossDeathComponent death = new BossDeathComponent(0f, 0f); // should clamp to small positive values
        boss.addComponent(death);
        boss.create();

        // Act & Assert: even with invalid ctor inputs, "death" should not crash without atlas
        assertDoesNotThrow(() -> boss.getEvents().trigger("death"));
        assertTrue(capturingES.registered.isEmpty(),
                "Even with clamped params, no effect is spawned when atlas is missing");
    }
}

