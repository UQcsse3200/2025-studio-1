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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Branch-complete tests for KillCommand.
 */
@ExtendWith(MockitoExtension.class)
class KillCommandTests {
    @Test
    void action_returnsFalse_whenArgsNull() {
        KillCommand cmd = new KillCommand();
        assertFalse(cmd.action(null));
    }

    @Test
    void action_returnsFalse_whenArgsEmpty() {
        KillCommand cmd = new KillCommand();
        assertFalse(cmd.action(new ArrayList<>()));
    }

    @Test
    void action_returnsFalse_whenUnknownSelector() {
        KillCommand cmd = new KillCommand();
        assertFalse(cmd.action(new ArrayList<>(List.of("@x"))));
    }

    @Test
    void killPlayer_returnsFalse_whenPlayerMissing() {
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            KillCommand cmd = new KillCommand();
            assertFalse(cmd.action(new ArrayList<>(List.of("@p"))));
        }
    }

    @Test
    void killPlayer_returnsFalse_whenPlayerHasNoCombatStats() {
        Entity player = mock(Entity.class);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(null);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            KillCommand cmd = new KillCommand();
            assertFalse(cmd.action(new ArrayList<>(List.of("@p"))));
        }
    }

    @Test
    void killPlayer_setsHealthToZero_whenStatsPresent() {
        Entity player = mock(Entity.class);
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(stats);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            KillCommand cmd = new KillCommand();
            assertTrue(cmd.action(new ArrayList<>(List.of("@p"))));
            verify(stats, times(1)).setHealth(0);
        }
    }

    // -------------------- @a (kill all enemies) service/entity edge cases --------------------

    @Test
    void killAllEnemies_returnsFalse_whenEntityServiceMissing() {
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(null);
            // getPlayer must be stubbed because code calls it later only if ES present; here it won't be
            KillCommand cmd = new KillCommand();
            assertFalse(cmd.action(new ArrayList<>(List.of("@a"))));
        }
    }

    @Test
    void killAllEnemies_returnsTrue_whenEntitiesNull() {
        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(null);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);
            // getPlayer may be called; safe to return null here (no iteration happens)
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            KillCommand cmd = new KillCommand();
            assertTrue(cmd.action(new ArrayList<>(List.of("@a")))); // nothing to kill -> true
        }
    }

    @Test
    void killAllEnemies_returnsTrue_whenEntitiesEmpty() {
        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(new Array<>());

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);
            sl.when(ServiceLocator::getPlayer).thenReturn(null);

            KillCommand cmd = new KillCommand();
            assertTrue(cmd.action(new ArrayList<>(List.of("@a")))); // nothing to kill -> true
        }
    }

    @Test
    void killAllEnemies_killsOnlyAliveNpcEnemies_excludesPlayerAndNonNpcAndDead() {
        EntityService es = mock(EntityService.class);

        // Entities
        Entity player = mock(Entity.class);
        Entity enemyAlive = mock(Entity.class);
        Entity enemyDead = mock(Entity.class);
        Entity enemyAlive2 = mock(Entity.class);
        Entity nonNpc = mock(Entity.class);
        Entity npcNoStats = mock(Entity.class);

        // Components/stubs
        CombatStatsComponent statsAlive1 = mock(CombatStatsComponent.class);
        when(statsAlive1.getHealth()).thenReturn(15);

        CombatStatsComponent statsDead = mock(CombatStatsComponent.class);
        when(statsDead.getHealth()).thenReturn(0);

        CombatStatsComponent statsAlive2 = mock(CombatStatsComponent.class);
        when(statsAlive2.getHealth()).thenReturn(5);

        CombatStatsComponent statsNonNpc = mock(CombatStatsComponent.class);
        when(statsNonNpc.getHealth()).thenReturn(50);

        HitboxComponent hbNpc = mock(HitboxComponent.class);
        when(hbNpc.getLayer()).thenReturn(PhysicsLayer.NPC);

        HitboxComponent hbNpc2 = mock(HitboxComponent.class);
        when(hbNpc2.getLayer()).thenReturn(PhysicsLayer.NPC);

        HitboxComponent hbNonNpc = mock(HitboxComponent.class);
        when(hbNonNpc.getLayer()).thenReturn(PhysicsLayer.ENEMY_PROJECTILE);

        // Wire components to entities
        when(enemyAlive.getComponent(HitboxComponent.class)).thenReturn(hbNpc);
        when(enemyAlive.getComponent(CombatStatsComponent.class)).thenReturn(statsAlive1);

        when(enemyDead.getComponent(HitboxComponent.class)).thenReturn(hbNpc);
        when(enemyDead.getComponent(CombatStatsComponent.class)).thenReturn(statsDead);

        when(enemyAlive2.getComponent(HitboxComponent.class)).thenReturn(hbNpc2);
        when(enemyAlive2.getComponent(CombatStatsComponent.class)).thenReturn(statsAlive2);

        when(nonNpc.getComponent(HitboxComponent.class)).thenReturn(hbNonNpc);
        when(nonNpc.getComponent(CombatStatsComponent.class)).thenReturn(statsNonNpc);

        when(npcNoStats.getComponent(HitboxComponent.class)).thenReturn(hbNpc);
        when(npcNoStats.getComponent(CombatStatsComponent.class)).thenReturn(null);

        // World list includes player & all types
        Array<Entity> world = new Array<>();
        world.addAll(player, enemyAlive, enemyDead, enemyAlive2, nonNpc, npcNoStats);
        when(es.getEntities()).thenReturn(world);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            KillCommand cmd = new KillCommand();
            assertTrue(cmd.action(new ArrayList<>(List.of("@a"))));

            // Only alive NPCs, excluding the player, must be killed
            verify(statsAlive1, times(1)).setHealth(0);
            verify(statsAlive2, times(1)).setHealth(0);

            // Dead NPC unchanged
            verify(statsDead, never()).setHealth(anyInt());

            // Non-NPC unchanged
            verify(statsNonNpc, never()).setHealth(anyInt());
        }
    }
}
