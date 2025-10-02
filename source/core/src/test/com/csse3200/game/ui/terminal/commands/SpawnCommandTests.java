package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    void returnsFalseWhenNoPlayerFound_nonPlayerPresent() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);

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
    void returnsFalseWhenNoPlayerFound_emptyEntityList() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);

        when(es.getEntities()).thenReturn(new Array<>());
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        assertFalse(command.action(new ArrayList<>()));
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void returnsFalseWhenNoPlayerFound_evenWithValidArgs() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);

        // Entities present but none with BOTH CombatStats and Stamina
        var entities = new com.badlogic.gdx.utils.Array<Entity>();
        entities.add(new Entity().addComponent(new CombatStatsComponent(10))); // no Stamina
        entities.add(new Entity().addComponent(new StaminaComponent()));       // no CombatStats

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<String>();
        args.add("Slime"); // valid name so we pass the args guard

        assertFalse(new SpawnCommand().action(args));

        // Should NOT try to spawn or call getRoomName when no player found
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
        verifyNoInteractions(ga);
    }
    
    @Test
    void spawnsWhenPlayerFound() {
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

        var args = new ArrayList<String>();
        args.add("Slime");

        var ok = command.action(args);
        assertTrue(ok, "Expected spawn to succeed when player present and args provided");

        verify(ga, times(1))
                .spawn(eq("Slime"), eq("Reception"), eq(1), eq(1f), same(player));
    }

    @Test
    void spawnsWhenSecondEntityIsPlayer_picksFirstValid() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);
        when(ga.getRoomName()).thenReturn("Lab");

        // First entity is NOT a player, second is a valid player
        var nonPlayer = new Entity().addComponent(new StaminaComponent());
        var player = new Entity()
                .addComponent(new CombatStatsComponent(50))
                .addComponent(new StaminaComponent());

        var entities = new Array<Entity>();
        entities.add(nonPlayer);
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<String>();
        args.add("Bot");

        assertTrue(command.action(args));
        verify(ga).spawn(eq("Bot"), eq("Lab"), eq(1), eq(1f), same(player));
    }

    @Test
    void trimsEnemyNameBeforeSpawning() {
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

        var args = new ArrayList<String>();
        args.add("   Slime  \t");

        assertTrue(command.action(args));
        verify(ga).spawn(eq("Slime"), eq("Reception"), eq(1), eq(1f), same(player));
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

        boolean ok = command.action(null);
        assertFalse(ok);
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void returnsFalseWhenArgFirstIsNull() {
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

        var args = new ArrayList<String>();
        args.add(null); // first element is null

        assertFalse(command.action(args));
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void returnsFalseWhenArgIsBlank() {
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

        var args = new ArrayList<String>();
        args.add("   \t  "); // blank

        assertFalse(command.action(args));
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void returnsFalseWhenArgIsEmptyString() {
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

        var args = new ArrayList<String>();
        args.add(""); // empty string

        assertFalse(new SpawnCommand().action(args));
        verify(ga, never()).spawn(anyString(), anyString(), anyInt(), anyFloat(), any());
    }

    @Test
    void ignoresExtraArgs_usesOnlyFirst() {
        var ga = mock(GameArea.class);
        var es = mock(EntityService.class);
        when(ga.getRoomName()).thenReturn("Reception");

        var player = new Entity()
                .addComponent(new CombatStatsComponent(20))
                .addComponent(new StaminaComponent());
        var entities = new com.badlogic.gdx.utils.Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerGameArea(ga);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<String>();
        args.add("Slime");
        args.add("ShouldBeIgnored");

        assertTrue(new SpawnCommand().action(args));

        // Verify both interactions, in order
        var inOrder = inOrder(ga);
        inOrder.verify(ga).getRoomName();
        inOrder.verify(ga).spawn(eq("Slime"), eq("Reception"), eq(1), eq(1f), same(player));

        verifyNoMoreInteractions(ga);
    }

}
