package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeleporterComponentTest {
    private MockedStatic<ServiceLocator> serviceLocatorMock;
    private EntityService entityService;
    private Array<Entity> worldEntities;

    private Entity makeEnemy(int health) {
        Entity enemy = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getHealth()).thenReturn(health);
        when(enemy.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(enemy.getComponent(GhostAnimationController.class)).thenReturn(mock(GhostAnimationController.class));
        return enemy;
    }

    @BeforeEach
    void setup() {
        // Mock Gdx.app so any logs don't NPE
        Gdx.app = mock(Application.class);

        entityService = mock(EntityService.class);
        worldEntities = new Array<>();
        when(entityService.getEntities()).thenReturn(worldEntities);

        serviceLocatorMock = Mockito.mockStatic(ServiceLocator.class);
        serviceLocatorMock.when(ServiceLocator::getEntityService).thenReturn(entityService);
        serviceLocatorMock.when(ServiceLocator::getGlobalEvents).thenReturn(new EventHandler());
    }

    @AfterEach
    void tearDown() {
        serviceLocatorMock.close();
    }

    @Test
    @DisplayName("isAnyEnemyAlive returns true when an enemy with health > 0 exists")
    void testIsAnyEnemyAliveTrue() {
        worldEntities.add(makeEnemy(10));

        var teleporter = new TeleporterComponent();
        var host = new Entity().addComponent(teleporter);
        host.create();

        assertTrue(teleporter.isAnyEnemyAlive());
    }

    @Test
    @DisplayName("isAnyEnemyAlive returns false when no enemies or health <= 0")
    void testIsAnyEnemyAliveFalse() {
        // No entities
        var teleporter = new TeleporterComponent();
        var host = new Entity().addComponent(teleporter);
        host.create();
        assertFalse(teleporter.isAnyEnemyAlive());

        // Enemy but dead
        worldEntities.add(makeEnemy(0));
        assertFalse(teleporter.isAnyEnemyAlive());
    }

    @Test
    @DisplayName("startTeleport is blocked when enemies are alive")
    void testStartTeleportBlocked() throws Exception {
        worldEntities.add(makeEnemy(5));

        var teleporter = new TeleporterComponent();
        var host = new Entity().addComponent(teleporter);
        host.create();

        teleporter.startTeleport("MainHall");

        var cls = TeleporterComponent.class;
        var teleportingField = cls.getDeclaredField("teleporting");
        teleportingField.setAccessible(true);
        boolean teleporting = teleportingField.getBoolean(teleporter);

        var pendingField = cls.getDeclaredField("pendingDestination");
        pendingField.setAccessible(true);
        Object pending = pendingField.get(teleporter);

        assertFalse(teleporting, "Teleporting should not start while enemies are alive");
        assertNull(pending, "No destination should be pending while blocked");
    }

    @Test
    @DisplayName("startTeleport proceeds when no enemies are alive")
    void testStartTeleportProceeds() throws Exception {
        // No enemies -> allowed
        var teleporter = new TeleporterComponent();
        var host = new Entity().addComponent(teleporter);
        host.create();

        teleporter.startTeleport("MainHall");

        var cls = TeleporterComponent.class;
        var teleportingField = cls.getDeclaredField("teleporting");
        teleportingField.setAccessible(true);
        boolean teleporting = teleportingField.getBoolean(teleporter);

        var pendingField = cls.getDeclaredField("pendingDestination");
        pendingField.setAccessible(true);
        Object pending = pendingField.get(teleporter);

        assertTrue(teleporting, "Teleporting should start when not blocked by enemies");
        assertEquals("MainHall", pending, "Pending destination should be set");
    }
}

