package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TeleportCommandTests {
    private TeleportCommand command;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();
        command = new TeleportCommand();
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
        var area = mock(GameArea.class);
        ServiceLocator.registerGameArea(area);

        assertFalse(command.action(new ArrayList<>()));
        verifyNoInteractions(area);
    }

    @Test
    void returnsFalseWhenNoPlayerFound() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        when(es.getEntities()).thenReturn(new Array<>()); // empty -> no keyboard player
        when(area.getPlayer()).thenReturn(null);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        assertFalse(command.action(new ArrayList<>()));
    }

    @Test
    void travelToCameraCenterSucceeds() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        // Force fallback to EntityService
        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        var cam = new OrthographicCamera();
        cam.position.set(4.0f, -6.5f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        var cmd = new TravelCommand();
        var ok = cmd.action(new ArrayList<>()); // no args => camera center
        assertTrue(ok);

        var pos = player.getPosition();
        assertEquals(4.0f, pos.x, 1e-5);
        assertEquals(-6.5f, pos.y, 1e-5);
    }

    @Test
    void travelCenterKeywordSucceeds() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        var cam = new OrthographicCamera();
        cam.position.set(-8f, 5f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        var cmd = new TravelCommand();
        var ok = cmd.action(new ArrayList<>(java.util.List.of("center")));
        assertTrue(ok);

        var pos = player.getPosition();
        assertEquals(-8f, pos.x, 1e-5);
        assertEquals(5f, pos.y, 1e-5);
    }

    @Test
    void travelToCoordinates() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var cmd = new TravelCommand();
        var args = new ArrayList<>(java.util.List.of("7.75", "9.5"));
        var ok = cmd.action(args);
        assertTrue(ok);

        var pos = player.getPosition();
        assertEquals(7.75f, pos.x, 1e-5);
        assertEquals(9.5f, pos.y, 1e-5);
    }

    @Test
    void teleportToCameraCenterFailsWhenNoCamera() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);
        when(rs.getCamera()).thenReturn(null); // no camera

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        assertFalse(command.action(new ArrayList<>()));
    }

    @Test
    void teleportToCoordinates() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("7.75", "9.5"));
        var ok = command.action(args);
        assertTrue(ok);

        var pos = player.getPosition();
        assertEquals(7.75f, pos.x, 1e-5);
        assertEquals(9.5f, pos.y, 1e-5);
    }

    @Test
    void teleportToNamedAreaSuccess() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);
        when(area.transitionToArea("Reception")).thenReturn(true);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("Reception"));
        var ok = command.action(args);

        assertTrue(ok);
        verify(area, times(1)).transitionToArea("Reception");
    }

    @Test
    void teleportToNamedAreaFailure() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);
        when(area.transitionToArea("UnknownRoom")).thenReturn(false);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("UnknownRoom"));
        var ok = command.action(args);

        assertFalse(ok);
        verify(area, times(1)).transitionToArea("UnknownRoom");
    }

    @Test
    void singleNumericArgIsInvalid() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("123.0"));
        assertFalse(command.action(args)); // single numeric arg should be rejected
        verify(area, never()).transitionToArea(anyString());
    }

    @Test
    void tooManyArgsReturnFalse() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("1", "2", "3"));
        assertFalse(command.action(args));
    }

    @Test
    void invalidArgumentsReturnFalse() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<>(java.util.List.of("12", "nope")); // mixed numeric/non-numeric
        assertFalse(command.action(args));
    }
}
