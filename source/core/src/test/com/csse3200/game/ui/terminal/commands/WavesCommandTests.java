package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WavesCommandTests {
    private static ArrayList<String> noArgs() {
        return new ArrayList<>();
    }

    @Test
    void triggersWaveHandling_whenPlayerFound() {
        GameArea ga = mock(GameArea.class);
        EntityService es = mock(EntityService.class);

        Entity enemy = mock(Entity.class);
        Entity player = mock(Entity.class);

        // enemy: has CombatStats, but no Stamina -> not a player
        when(enemy.getComponent(CombatStatsComponent.class)).thenReturn(mock(CombatStatsComponent.class));
        when(enemy.getComponent(StaminaComponent.class)).thenReturn(null);

        // player: has CombatStats and Stamina -> is a player
        when(player.getComponent(CombatStatsComponent.class)).thenReturn(mock(CombatStatsComponent.class));
        when(player.getComponent(StaminaComponent.class)).thenReturn(mock(StaminaComponent.class));

        Array<Entity> entities = new Array<>();
        entities.add(enemy);
        entities.add(player);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameArea).thenReturn(ga);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            WavesCommand cmd = new WavesCommand();
            boolean ok = cmd.action(noArgs());

            assertTrue(ok);
            verify(ga, times(1)).startWaves(player);
            verifyNoMoreInteractions(ga);
        }
    }

    @Test
    void returnsFalse_whenNoGameArea() {
        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(new Array<>()); // doesn’t matter

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameArea).thenReturn(null);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            WavesCommand cmd = new WavesCommand();
            assertFalse(cmd.action(noArgs()));
        }
    }

    @Test
    void returnsFalse_whenNoEntityService() {
        GameArea ga = mock(GameArea.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameArea).thenReturn(ga);
            sl.when(ServiceLocator::getEntityService).thenReturn(null);

            WavesCommand cmd = new WavesCommand();
            assertFalse(cmd.action(noArgs()));
            verifyNoInteractions(ga);
        }
    }

    @Test
    void returnsFalse_whenNoPlayerInEntities() {
        GameArea ga = mock(GameArea.class);
        EntityService es = mock(EntityService.class);

        Entity e1 = mock(Entity.class);
        Entity e2 = mock(Entity.class);
        when(e1.getComponent(CombatStatsComponent.class)).thenReturn(mock(CombatStatsComponent.class));
        when(e1.getComponent(StaminaComponent.class)).thenReturn(null);
        when(e2.getComponent(CombatStatsComponent.class)).thenReturn(null); // no combat stats either

        Array<Entity> entities = new Array<>();
        entities.add(e1);
        entities.add(e2);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameArea).thenReturn(ga);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            WavesCommand cmd = new WavesCommand();
            assertFalse(cmd.action(noArgs()));
            verifyNoInteractions(ga);
        }
    }

    @Test
    void doesNotThrow_withEmptyEntities() {
        GameArea ga = mock(GameArea.class);
        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(new Array<>());

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getGameArea).thenReturn(ga);
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            WavesCommand cmd = new WavesCommand();
            assertDoesNotThrow(() -> cmd.action(noArgs()));
        }
    }
}
