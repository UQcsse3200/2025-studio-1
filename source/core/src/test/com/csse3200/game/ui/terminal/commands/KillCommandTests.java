package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Mockito-based tests for KillCommand without unnecessary stubs.
 * Assumes KillCommand:
 *  - uses ServiceLocator.getPlayer() and ServiceLocator.getEntityService()
 *  - treats enemies as entities whose HitboxComponent has layer == PhysicsLayer.NPC
 *  - kills by calling CombatStatsComponent#setHealth(0)
 */
@ExtendWith(MockitoExtension.class)
public class KillCommandTests {

    @Test
    void killAllEnemies_killsOnlyAliveNpcEnemies_andSkipsPlayerAndProjectiles() {
        // ---- world + services ----
        EntityService es = mock(EntityService.class);
        Entity player = mock(Entity.class);

        // ---- entities ----
        Entity enemyAlive = mock(Entity.class);
        Entity enemyDead  = mock(Entity.class);
        Entity projectile = mock(Entity.class);

        // components
        CombatStatsComponent aliveStats = mock(CombatStatsComponent.class);
        when(aliveStats.getHealth()).thenReturn(10);

        CombatStatsComponent deadStats  = mock(CombatStatsComponent.class);
        when(deadStats.getHealth()).thenReturn(0);

        CombatStatsComponent projectileStats = mock(CombatStatsComponent.class); // no getHealth() stub

        HitboxComponent enemyHb = mock(HitboxComponent.class);
        when(enemyHb.getLayer()).thenReturn(PhysicsLayer.NPC);

        HitboxComponent enemyDeadHb = mock(HitboxComponent.class);
        when(enemyDeadHb.getLayer()).thenReturn(PhysicsLayer.NPC);

        HitboxComponent projectileHb = mock(HitboxComponent.class);
        when(projectileHb.getLayer()).thenReturn(PhysicsLayer.ENEMY_PROJECTILE);

        // wire components (each exactly once)
        when(enemyAlive.getComponent(CombatStatsComponent.class)).thenReturn(aliveStats);
        when(enemyAlive.getComponent(HitboxComponent.class)).thenReturn(enemyHb);

        when(enemyDead.getComponent(CombatStatsComponent.class)).thenReturn(deadStats);
        when(enemyDead.getComponent(HitboxComponent.class)).thenReturn(enemyDeadHb);

        when(projectile.getComponent(CombatStatsComponent.class)).thenReturn(projectileStats);
        when(projectile.getComponent(HitboxComponent.class)).thenReturn(projectileHb);

        Array<Entity> world = new Array<>();
        world.addAll(player, enemyAlive, enemyDead, projectile);
        when(es.getEntities()).thenReturn(world);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            KillCommand cmd = new KillCommand();
            boolean ok = cmd.action(new ArrayList<>(List.of("@a")));
            assertTrue(ok, "kill @a should return true when it executes");

            verify(aliveStats, times(1)).setHealth(0);
            verify(deadStats,  never()).setHealth(anyInt());
            verify(projectileStats, never()).setHealth(anyInt());
        }
    }

    @Test
    void killPlayer_setsPlayerHealthToZero_whenPlayerHasCombatStats() {
        Entity player = mock(Entity.class);
        CombatStatsComponent playerStats = mock(CombatStatsComponent.class);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(playerStats);

        // No need to stub getEntityService() – it’s not used for @p
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            KillCommand cmd = new KillCommand();
            boolean ok = cmd.action(new ArrayList<>(List.of("@p")));
            assertTrue(ok, "kill @p should return true");

            verify(playerStats, times(1)).setHealth(0);
        }
    }

    @Test
    void killPlayer_returnsFalse_ifPlayerMissing() {
        // Only stub what’s needed: getPlayer() -> null
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            KillCommand cmd = new KillCommand();
            boolean ok = cmd.action(new ArrayList<>(List.of("@p")));
            assertFalse(ok, "kill @p should fail when no player is available");
        }
    }

    @Test
    void missingArgs_returnsFalse_andDoesNothing() {
        // Don’t stub anything – action returns before any ServiceLocator calls.
        KillCommand cmd = new KillCommand();
        assertFalse(cmd.action(new ArrayList<>()), "missing selector should return false");
    }

    @Test
    void unknownSelector_returnsFalse_andDoesNothing() {
        // Don’t stub anything – action returns before any ServiceLocator calls.
        KillCommand cmd = new KillCommand();
        assertFalse(cmd.action(new ArrayList<>(List.of("@x"))),
                "unknown selector should return false");
    }
}
