package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.rendering.RenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RemoteOpenComponent.
 * Verifies correct toggle behavior of the companion control panel:
 * - Does nothing when no key pressed or stage is null
 * - Creates panel on first press
 * - Removes panel on second press
 * - Properly cleans up during dispose()
 */
class RemoteOpenComponentTest {

    private RemoteOpenComponent comp;
    private Entity owner;

    @BeforeEach
    void setup() {
        // Mock Gdx.input (static LibGDX field)
        Gdx.input = mock(Input.class);

        comp = new RemoteOpenComponent();
        owner = mock(Entity.class);
        comp.setEntity(owner);
    }

    /** Helper to read the private field 'panel' via reflection */
    private static Actor getPanel(RemoteOpenComponent c) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panel");
        f.setAccessible(true);
        return (Actor) f.get(c);
    }

    /** Helper to set the private field 'panel' via reflection */
    private static void setPanel(RemoteOpenComponent c, Actor panel) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panel");
        f.setAccessible(true);
        f.set(c, panel);
    }

    @Test
    void update_noKeyPressed_doesNothing() throws Exception {
        // No key pressed
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(false);

        // Static mocks to ensure no accidental ServiceLocator or UI access
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            comp.update();

            // Neither ServiceLocator nor CompanionControlPanel should be touched
            sl.verifyNoInteractions();
            ccp.verifyNoInteractions();

            assertNull(getPanel(comp));
        }
    }

    @Test
    void update_firstPress_createsPanel_whenStageAvailable() throws Exception {
        // Simulate key press (default key: C)
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // Prepare RenderService and Stage
        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class);
        when(renderService.getStage()).thenReturn(stage);

        // Prepare CompanionControlPanel.attach return value
        Table panel = mock(Table.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);
            ccp.when(() -> CompanionControlPanel.attach(stage, owner)).thenReturn(panel);

            comp.update();

            // Verify attach() is called once
            ccp.verify(() -> CompanionControlPanel.attach(stage, owner), times(1));

            // Panel is stored successfully (panel extends Actor)
            assertSame(panel, getPanel(comp));
        }
    }

    @Test
    void update_secondPress_removesPanel_whenAlreadyOnStage() throws Exception {
        // Prepare a state with an existing panel already on stage
        Actor panel = mock(Actor.class);
        Stage stage = mock(Stage.class);
        when(panel.getStage()).thenReturn(stage);
        setPanel(comp, panel);

        // Simulate key press
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // RenderService is available, but attach() should not be called (removal path)
        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(stage);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

            comp.update();

            // Should not call attach()
            ccp.verifyNoInteractions();
            // Should remove existing panel
            verify(panel, times(1)).remove();
            // Panel reference should be cleared
            assertNull(getPanel(comp));
        }
    }

    @Test
    void update_stageNull_doesNothing() throws Exception {
        // Simulate key press
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // RenderService returns null stage
        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(null);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

            comp.update();

            // No attach should be called
            ccp.verifyNoInteractions();
            assertNull(getPanel(comp));
        }
    }

    @Test
    void dispose_removesPanel_ifExists() throws Exception {
        // Prepare an existing panel
        Actor panel = mock(Actor.class);
        setPanel(comp, panel);

        comp.dispose();

        // Should remove the panel and clear reference
        verify(panel, times(1)).remove();
        assertNull(getPanel(comp));
    }
}

