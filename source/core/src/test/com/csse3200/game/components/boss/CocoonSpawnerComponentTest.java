package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CocoonSpawnerComponent
 */
@ExtendWith(MockitoExtension.class)
class CocoonSpawnerComponentTest {

    private Entity boss;
    private CombatStatsComponent bossStats;

    @BeforeEach
    void setUp() {
        boss = new Entity();
        bossStats = new CombatStatsComponent(100);
    }

    @Test
    void testInit() {
        Vector2[] positions = {new Vector2(5, 5), new Vector2(10, 5)};
        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertNotNull(spawner);
        assertFalse(spawner.areCocoonsSpawned());
        assertEquals(0, spawner.getRemainingCocoonsCount());
    }

    @Test
    void testInitialState() {
        Vector2[] positions = {new Vector2(5, 5)};
        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertFalse(spawner.areCocoonsSpawned());
        assertEquals(0, spawner.getRemainingCocoonsCount());
        assertNotNull(spawner.getActiveCocoons());
        assertTrue(spawner.getActiveCocoons().isEmpty());
    }

    @Test
    void testCreate() {
        Vector2[] positions = {new Vector2(5, 5)};
        boss.addComponent(bossStats);

        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);
        boss.addComponent(spawner);
        boss.create();

        assertFalse(spawner.areCocoonsSpawned());
    }

    @Test
    void testDifferentThresholds() {
        Vector2[] positions = {new Vector2(5, 5)};

        CocoonSpawnerComponent spawner1 = new CocoonSpawnerComponent(0.3f, positions);
        CocoonSpawnerComponent spawner2 = new CocoonSpawnerComponent(0.5f, positions);

        assertNotNull(spawner1);
        assertNotNull(spawner2);
    }

    @Test
    void testMultiplePositions() {
        Vector2[] positions = {
                new Vector2(5, 5),
                new Vector2(10, 5),
                new Vector2(15, 5)
        };

        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertNotNull(spawner);
        assertEquals(0, spawner.getRemainingCocoonsCount());
    }

    @Test
    void testEmptyPositions() {
        Vector2[] positions = {};

        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertNotNull(spawner);
        assertEquals(0, spawner.getRemainingCocoonsCount());
    }

    @Test
    void testGetActiveCocoons() {
        Vector2[] positions = {new Vector2(5, 5)};
        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        var cocoons1 = spawner.getActiveCocoons();
        var cocoons2 = spawner.getActiveCocoons();

        assertNotSame(cocoons1, cocoons2);
        assertTrue(cocoons1.isEmpty());
    }

    @Test
    void testForceCleanup() {
        Vector2[] positions = {new Vector2(5, 5)};
        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertDoesNotThrow(() -> spawner.forceCleanupCocoons());
        assertEquals(0, spawner.getRemainingCocoonsCount());
    }

    @Test
    void testDispose() {
        Vector2[] positions = {new Vector2(5, 5)};
        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);

        assertDoesNotThrow(() -> spawner.dispose());
        assertEquals(0, spawner.getRemainingCocoonsCount());
    }

    @Test
    void testWithoutCombatStats() {
        Vector2[] positions = {new Vector2(5, 5)};

        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);
        boss.addComponent(spawner);

        assertDoesNotThrow(() -> boss.create());
    }

    @Test
    void testUpdateBeforeSpawn() {
        Vector2[] positions = {new Vector2(5, 5)};
        boss.addComponent(bossStats);

        CocoonSpawnerComponent spawner = new CocoonSpawnerComponent(0.3f, positions);
        boss.addComponent(spawner);
        boss.create();

        assertDoesNotThrow(() -> spawner.update());
        assertFalse(spawner.areCocoonsSpawned());
    }
}