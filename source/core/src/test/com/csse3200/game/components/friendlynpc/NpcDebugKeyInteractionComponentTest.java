package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Simulates key presses (Y and 6) to ensure the component calls menu actions.
 */
class NpcDebugKeyInteractionComponentTest {

    @BeforeEach
    void bootGdx() {
        // Safe default app so any incidental Gdx.app calls don't NPE
        Gdx.app = mock(Application.class);
        // Fresh input mock each test
        Gdx.input = mock(Input.class);
    }

    @Test
    void yOpensMenu_whenPlayerInRange_andSixShowsInstructions() {
        // Arrange entity with menu + gate
        Entity player = new Entity();
        player.setPosition(0, 0);

        Entity npc = new Entity();
        npc.setPosition(0, 0);

        NpcTwoOptionMenuComponent menu = spy(new NpcTwoOptionMenuComponent());
        NpcProximityGateComponent gate = new NpcProximityGateComponent(player, 3f);

        npc.addComponent(menu);
        npc.addComponent(gate);

        NpcDebugKeyInteractionComponent inputComp = new NpcDebugKeyInteractionComponent();
        npc.addComponent(inputComp);

        // Component needs create() to cache sibling components
        menu.create();
        gate.create();
        inputComp.create();

        // Simulate keys: Y then 6 pressed this frame
        when(Gdx.input.isKeyJustPressed(Input.Keys.Y)).thenReturn(true);
        when(Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)).thenReturn(true);

        // Act
        inputComp.update();

        // Assert expected calls
        verify(menu, times(1)).open();
        verify(menu, times(1)).chooseInstructions();
    }

    @Test
    void noActionWithoutRequiredComponents() {
        Entity npc = new Entity();
        NpcDebugKeyInteractionComponent inputComp = new NpcDebugKeyInteractionComponent();
        npc.addComponent(inputComp);
        inputComp.create();

        // Even if keys are "pressed", without menu/gate nothing should crash
        when(Gdx.input.isKeyJustPressed(anyInt())).thenReturn(true);
        inputComp.update();
        // No verify() needed; test passes if no exception thrown
    }
}
