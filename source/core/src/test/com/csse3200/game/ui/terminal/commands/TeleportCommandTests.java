package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TeleportCommandTests {
    private TeleportCommand command;

    private static Stream<Arguments> equalsIgnoreCaseTrimCases() {
        return Stream.of(
                // Left side null => short-circuit false (missed branch if not tested)
                arguments(null, "center", false),

                // True after trim + ignore case
                arguments("  CeNtEr  ", "center", true),

                // Left true + right false (non-match)
                arguments("Reception", "center", false),

                // Left true + right false (blank after trim)
                arguments("   ", "center", false),

                // Right arg null => equalsIgnoreCase(null) is false
                arguments("center", null, false)
        );
    }

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

    // ---------- TRAVEL TESTSgit reset --soft HEAD~1 ----------

    @Test
    void returnsFalseWhenNoPlayerFound() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        when(es.getEntities()).thenReturn(new Array<>()); // empty -> no keyboard player
        when(area.getPlayer()).thenReturn(null);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        // Ensure no stray global player can make this pass
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(null);
            assertFalse(command.action(new ArrayList<>()));
        }
    }

    @Test
    void travelToCameraCenterSucceeds() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        var cam = new OrthographicCamera();
        cam.position.set(4.0f, -6.5f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.clear();
        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        var cmd = new TravelCommand();
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            var ok = cmd.action(new ArrayList<>()); // no args => camera center
            assertTrue(ok);
        }

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

        ServiceLocator.clear();
        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        var cmd = new TravelCommand();
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            var ok = cmd.action(new ArrayList<>(java.util.List.of("center")));
            assertTrue(ok);
        }

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

        ServiceLocator.clear();
        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var cmd = new TravelCommand();
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            var ok = cmd.action(new ArrayList<>(java.util.List.of("7.75", "9.5")));
            assertTrue(ok);
        }

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

        // Ensure TeleportCommand uses THIS player even if a global exists
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);

            var args = new ArrayList<>(java.util.List.of("7.75", "9.5"));
            var ok = command.action(args);
            assertTrue(ok);
        }

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

    @Test
    void teleportNoArgsCenterSucceedsWithGlobalPlayer() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        when(area.getPlayer()).thenReturn(null);

        var cam = new OrthographicCamera();
        cam.position.set(3f, 2f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            assertTrue(command.action(new ArrayList<>())); // no args => center
        }

        Vector2 pos = player.getPosition();
        assertEquals(3f, pos.x, 1e-5);
        assertEquals(2f, pos.y, 1e-5);
    }

    @Test
    void teleportNullArgs_centerSucceeds() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new com.badlogic.gdx.utils.Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        var cam = new OrthographicCamera();
        cam.position.set(10f, -4f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        try (var sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            assertTrue(new TeleportCommand().action(null)); // <-- args == null
        }

        var pos = player.getPosition();
        assertEquals(10f, pos.x, 1e-5);
        assertEquals(-4f, pos.y, 1e-5);
    }

    @Test
    void teleportCenterKeywordSucceedsAndTrimsCase() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        when(area.getPlayer()).thenReturn(null);

        var cam = new OrthographicCamera();
        cam.position.set(-2f, 9f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            assertTrue(command.action(new ArrayList<>(java.util.List.of("  CeNtEr  "))));
        }

        Vector2 pos = player.getPosition();
        assertEquals(-2f, pos.x, 1e-5);
        assertEquals(9f, pos.y, 1e-5);
    }

    @Test
    void teleportCenterFailsWhenNoRenderService() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        when(area.getPlayer()).thenReturn(null);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        // Intentionally DO NOT register RenderService

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(player);
            assertFalse(command.action(new ArrayList<>())); // fails due to missing RenderService
        }
    }

    @Test
    void teleportFallbackResolveFromEntityServiceSucceeds() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);
        var rs = mock(RenderService.class);

        // No global player; the fallback resolver should find this one
        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(es.getEntities()).thenReturn(entities);
        when(area.getPlayer()).thenReturn(null);

        var cam = new OrthographicCamera();
        cam.position.set(1.25f, -3.5f, 0f);
        when(rs.getCamera()).thenReturn(cam);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);
        ServiceLocator.registerRenderService(rs);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class, CALLS_REAL_METHODS)) {
            sl.when(ServiceLocator::getPlayer).thenReturn(null); // force fallback path
            assertTrue(command.action(new ArrayList<>()));       // no args => center via fallback-resolved player
        }

        Vector2 pos = player.getPosition();
        assertEquals(1.25f, pos.x, 1e-5);
        assertEquals(-3.5f, pos.y, 1e-5);
    }

    @Test
    void twoArgsWithNullX_returnsFalse() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        // x is null -> parseFloat(null) hits the s == null branch
        var args = new ArrayList<String>();
        args.add(null);
        args.add("5");

        assertFalse(new TeleportCommand().action(args));
        verify(area, never()).transitionToArea(anyString());
    }

    @Test
    void twoArgsWithNullY_returnsFalse() {
        var area = mock(GameArea.class);
        var es = mock(EntityService.class);

        var player = new Entity().addComponent(new KeyboardPlayerInputComponent());
        var entities = new Array<Entity>();
        entities.add(player);

        when(area.getPlayer()).thenReturn(null);
        when(es.getEntities()).thenReturn(entities);

        ServiceLocator.registerGameArea(area);
        ServiceLocator.registerEntityService(es);

        var args = new ArrayList<String>();
        args.add("3.25");
        args.add(null); // y is null

        assertFalse(new TeleportCommand().action(args));
        verify(area, never()).transitionToArea(anyString());
    }

    @Test
    void equalsIgnoreCaseTrim_nullLeft_returnsFalse_coversShortCircuit() throws Exception {
        Method m = TeleportCommand.class.getDeclaredMethod(
                "equalsIgnoreCaseTrim", String.class, String.class);
        m.setAccessible(true);

        // a == null -> short-circuit to false (covers the missed branch)
        boolean res = (boolean) m.invoke(null, (String) null, "center");
        assertFalse(res);
    }

    @Test
    void equalsIgnoreCaseTrim_trueAfterTrimAndCase() throws Exception {
        Method m = TeleportCommand.class.getDeclaredMethod(
                "equalsIgnoreCaseTrim", String.class, String.class);
        m.setAccessible(true);

        assertTrue((boolean) m.invoke(null, "  CeNtEr  ", "center")); // left true + right true
    }

    @ParameterizedTest(name = "equalsIgnoreCaseTrim(\"{0}\", \"{1}\") => {2}")
    @MethodSource("equalsIgnoreCaseTrimCases")
    void equalsIgnoreCaseTrim_parameterized(String a, String b, boolean expected) throws Exception {
        Method m = TeleportCommand.class.getDeclaredMethod(
                "equalsIgnoreCaseTrim", String.class, String.class);
        m.setAccessible(true);

        boolean res = (boolean) m.invoke(null, a, b);
        assertEquals(expected, res);
    }
}
