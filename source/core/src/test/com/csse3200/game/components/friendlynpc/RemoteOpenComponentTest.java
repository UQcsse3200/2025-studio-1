package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the new RemoteOpenComponent (UI tip/panel version).
 */
class RemoteOpenComponentTest {

    private RemoteOpenComponent comp;
    private Entity owner;

    @BeforeEach
    void setup() {
        // Mock LibGDX input
        Gdx.input = mock(Input.class);

        comp = new RemoteOpenComponent();
        owner = mock(Entity.class);
        comp.setEntity(owner);
    }

    // ---- reflection helpers to access private fields ----
    private static Table getTip(RemoteOpenComponent c) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("tipRoot");
        f.setAccessible(true);
        return (Table) f.get(c);
    }

    private static Table getPanel(RemoteOpenComponent c) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panelRoot");
        f.setAccessible(true);
        return (Table) f.get(c);
    }

    private static void setTip(RemoteOpenComponent c, Table tip) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("tipRoot");
        f.setAccessible(true);
        f.set(c, tip);
    }

    private static void setPanel(RemoteOpenComponent c, Table panel) throws Exception {
        Field f = RemoteOpenComponent.class.getDeclaredField("panelRoot");
        f.setAccessible(true);
        f.set(c, panel);
    }

    @Test
    void create_showsTip_whenStageAvailable() throws Exception {
        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class);
        when(renderService.getStage()).thenReturn(stage);

        Table tip = mock(Table.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<Remotetipdisplay> tipDisp = mockStatic(Remotetipdisplay.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);
            tipDisp.when(() -> Remotetipdisplay.attach(stage, 0.22f)).thenReturn(tip);

            comp.create();

            assertSame(tip, getTip(comp));
            assertNull(getPanel(comp));
        }
    }

    @Test
    void update_noKeyPressed_doesNothing() throws Exception {
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(false);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<Remotetipdisplay> tipDisp = mockStatic(Remotetipdisplay.class);
             MockedStatic<CompanionControlPanel> panel = mockStatic(CompanionControlPanel.class)) {

            comp.update();

            sl.verifyNoInteractions();
            tipDisp.verifyNoInteractions();
            panel.verifyNoInteractions();

            assertNull(getTip(comp));
            assertNull(getPanel(comp));
        }
    }

    @Test
    void firstPress_hidesTip_andShowsPanel() throws Exception {
        // prepare stage
        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class);
        when(renderService.getStage()).thenReturn(stage);

        // existing tip on stage
        Table tip = mock(Table.class);
        when(tip.getStage()).thenReturn(stage);
        setTip(comp, tip);

        // key press
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        // panel to be returned
        Table panel = mock(Table.class);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);
            ccp.when(() -> CompanionControlPanel.attach(stage, owner, 0.44f)).thenReturn(panel);

            comp.update();

            // tip removed, panel shown
            verify(tip, times(1)).remove();
            assertNull(getTip(comp));
            assertSame(panel, getPanel(comp));
        }
    }

    @Test
    void secondPress_hidesPanel_andShowsTip() throws Exception {
        RenderService renderService = mock(RenderService.class);
        Stage stage = mock(Stage.class);
        when(renderService.getStage()).thenReturn(stage);
        Table panel = mock(Table.class);
        when(panel.getStage()).thenReturn(stage);
        setPanel(comp, panel);
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);
        Table tip = mock(Table.class);
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<Remotetipdisplay> tipDisp = mockStatic(Remotetipdisplay.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {
            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);
            tipDisp.when(() -> Remotetipdisplay.attach(stage, 0.22f)).thenReturn(tip);
            comp.update();
            verify(panel, times(1)).remove();
            assertSame(tip, getTip(comp));
            assertNull(getPanel(comp));
            ccp.verifyNoInteractions();
        }
    }

    @Test
    void update_stageNull_doesNothing() throws Exception {
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);

        RenderService renderService = mock(RenderService.class);
        when(renderService.getStage()).thenReturn(null);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class);
             MockedStatic<Remotetipdisplay> tipDisp = mockStatic(Remotetipdisplay.class);
             MockedStatic<CompanionControlPanel> ccp = mockStatic(CompanionControlPanel.class)) {

            sl.when(ServiceLocator::getRenderService).thenReturn(renderService);

            comp.update();

            tipDisp.verifyNoInteractions();
            ccp.verifyNoInteractions();
            assertNull(getTip(comp));
            assertNull(getPanel(comp));
        }
    }

    @Test
    void dispose_removesBoth_ifExist() throws Exception {
        Table tip = mock(Table.class);
        Table panel = mock(Table.class);
        setTip(comp, tip);
        setPanel(comp, panel);

        comp.dispose();

        verify(tip, times(1)).remove();
        verify(panel, times(1)).remove();
        assertNull(getTip(comp));
        assertNull(getPanel(comp));
    }
}


