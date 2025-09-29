package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class SpawnCommandTests {
    private SpawnCommand command;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        command = new SpawnCommand();
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void returnsFalseWhenNoGameAreaRegistered() {
        var es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(new Array<>());
        ServiceLocator.registerEntityService(es);

        assertFalse(command.action(new ArrayList<>()));
    }

    @Test
    void returnsFalseWhenNoEntityServiceRegistered() {
        var ga = mock(GameArea.class);
        ServiceLocator.registerGameArea(ga);

        assertFalse(command.action(new ArrayList<>()));
        verifyNoInteractions(ga);
    }

    @Test
    void returnsFalseWhenNoPlayerFound() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);

        // Only non-player entities or empty list
        var entities = new Array<Entity>();
        var nonPlayer = new Entity().addComponent(new CombatStatsComponent(10)); // lacks StaminaComponent
        entities.add(nonPlayer);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        assertFalse(command.action(new ArrayList<>()));
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void spawnsWhenPlayerFound() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);
        when(ga.getRoomName()).thenReturn("Reception");

        // Player must have BOTH CombatStatsComponent and StaminaComponent
        var player = new Entity()
                .addComponent(new CombatStatsComponent(20))
                .addComponent(new StaminaComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<String>();
        args.add("Slime");

        var ok = command.action(args);
        assertTrue(ok, "Expected spawn to succeed when player present and args provided");

        verify(ga, times(1))
                .spawn(eq("Slime"), eq("Reception"), eq(1), eq(1f), same(player));
    }

    @Test
    void returnsFalseWhenArgsEmpty_MissingName() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);
        when(ga.getRoomName()).thenReturn("Reception");

        var player = new Entity()
                .addComponent(new CombatStatsComponent(20))
                .addComponent(new StaminaComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        // Now returns false instead of throwing NoSuchElementException
        boolean ok = command.action(new ArrayList<>());
        assertFalse(ok);
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void returnsFalseWhenArgsNull_MissingName() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);
        when(ga.getRoomName()).thenReturn("Reception");

        var player = new Entity()
                .addComponent(new CombatStatsComponent(20))
                .addComponent(new StaminaComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        // Now returns false instead of throwing NullPointerException
        boolean ok = command.action(null);
        assertFalse(ok);
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }
}
