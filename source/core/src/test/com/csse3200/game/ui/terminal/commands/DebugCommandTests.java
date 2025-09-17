package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DebugCommandTests {

    @Test
    void turnsDebugOn() {
        RenderService render = mock(RenderService.class);
        DebugRenderer debug = mock(DebugRenderer.class);
        when(render.getDebug()).thenReturn(debug);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getRenderService).thenReturn(render);

            DebugCommand cmd = new DebugCommand();
            boolean ok = cmd.action(new ArrayList<>(List.of("on")));

            assertTrue(ok);
            verify(debug).setActive(true);
            verifyNoMoreInteractions(debug);
        }
    }

    @Test
    void turnsDebugOff() {
        RenderService render = mock(RenderService.class);
        DebugRenderer debug = mock(DebugRenderer.class);
        when(render.getDebug()).thenReturn(debug);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getRenderService).thenReturn(render);

            DebugCommand cmd = new DebugCommand();
            boolean ok = cmd.action(new ArrayList<>(List.of("off")));

            assertTrue(ok);
            verify(debug).setActive(false);
            verifyNoMoreInteractions(debug);
        }
    }

    @Test
    void rejectsInvalidArgsWithoutSideEffects() {
        RenderService render = mock(RenderService.class);
        DebugRenderer debug = mock(DebugRenderer.class);
        when(render.getDebug()).thenReturn(debug);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getRenderService).thenReturn(render);

            DebugCommand cmd = new DebugCommand();
            assertFalse(cmd.action(new ArrayList<>()));                    // no args
            assertFalse(cmd.action(new ArrayList<>(List.of("on","off")))); // too many
            assertFalse(cmd.action(new ArrayList<>(List.of("blah"))));     // unknown

            verifyNoInteractions(debug);
        }
    }
}

