package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DoorOverrideCommand.
 */
@ExtendWith(MockitoExtension.class)
class DoorOverrideCommandTest {

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void invalidArgs_shouldReturnFalse_andNotTouchAnything() {
        DoorOverrideCommand cmd = new DoorOverrideCommand();

        try (MockedStatic<KeycardGateComponent> keycardMock = mockStatic(KeycardGateComponent.class)) {
            assertFalse(cmd.action(null));
            assertFalse(cmd.action(new ArrayList<>()));
            assertFalse(cmd.action(new ArrayList<>(List.of("maybe"))));
            assertFalse(cmd.action(new ArrayList<>(List.of("on", "extra"))));

            keycardMock.verifyNoInteractions();
        }
    }

    @Test
    void enable_shouldSetGlobalOverrideTrue_andUnlockAllDoors() {
        DoorOverrideCommand cmd = new DoorOverrideCommand();

        // Mock two door entities + one non-door
        Entity e1 = mock(Entity.class);
        Entity e2 = mock(Entity.class);
        Entity e3 = mock(Entity.class);

        DoorComponent d1 = mock(DoorComponent.class);
        DoorComponent d2 = mock(DoorComponent.class);

        when(e1.getComponent(DoorComponent.class)).thenReturn(d1);
        when(e2.getComponent(DoorComponent.class)).thenReturn(d2);
        when(e3.getComponent(DoorComponent.class)).thenReturn(null);

        Array<Entity> entities = new Array<>();
        entities.addAll(e1, e2, e3);

        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerEntityService(es);

        try (MockedStatic<KeycardGateComponent> keycardMock = mockStatic(KeycardGateComponent.class)) {
            boolean ok = cmd.action(new ArrayList<>(List.of("on")));
            assertTrue(ok);

            keycardMock.verify(() -> KeycardGateComponent.setGlobalOverride(true), times(1));
            verify(d1, times(1)).setOverrideUnlocked(true);
            verify(d2, times(1)).setOverrideUnlocked(true);
            // Non-door should not be touched
            verify(e3, times(1)).getComponent(DoorComponent.class);
            verifyNoMoreInteractions(d1, d2);
        }
    }

    @Test
    void disable_shouldSetGlobalOverrideFalse_andLockAllDoors() {
        DoorOverrideCommand cmd = new DoorOverrideCommand();

        Entity e1 = mock(Entity.class);
        Entity e2 = mock(Entity.class);

        DoorComponent d1 = mock(DoorComponent.class);
        DoorComponent d2 = mock(DoorComponent.class);

        when(e1.getComponent(DoorComponent.class)).thenReturn(d1);
        when(e2.getComponent(DoorComponent.class)).thenReturn(d2);

        Array<Entity> entities = new Array<>();
        entities.addAll(e1, e2);

        EntityService es = mock(EntityService.class);
        when(es.getEntities()).thenReturn(entities);
        ServiceLocator.registerEntityService(es);

        try (MockedStatic<KeycardGateComponent> keycardMock = mockStatic(KeycardGateComponent.class)) {
            boolean ok = cmd.action(new ArrayList<>(List.of("off")));
            assertTrue(ok);

            keycardMock.verify(() -> KeycardGateComponent.setGlobalOverride(false), times(1));
            verify(d1, times(1)).setOverrideUnlocked(false);
            verify(d2, times(1)).setOverrideUnlocked(false);
            verifyNoMoreInteractions(d1, d2);
        }
    }

    @Test
    void noEntityServiceRegistered_shouldStillFlipGlobal_andReturnTrue() {
        DoorOverrideCommand cmd = new DoorOverrideCommand();

        // Ensure there's no EntityService registered
        try {
            ServiceLocator.clear();
        } catch (Throwable ignored) {}

        try (MockedStatic<KeycardGateComponent> keycardMock = mockStatic(KeycardGateComponent.class)) {
            boolean ok = cmd.action(new ArrayList<>(List.of("on")));
            assertTrue(ok);

            // Global override still flips
            keycardMock.verify(() -> KeycardGateComponent.setGlobalOverride(true), times(1));
        }
    }
}
