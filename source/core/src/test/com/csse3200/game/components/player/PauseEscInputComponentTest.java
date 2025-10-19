package com.csse3200.game.components.player;

import com.badlogic.gdx.Input;
import com.csse3200.game.components.screens.PauseEscInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class PauseEscInputComponentTest {
    private PauseEscInputComponent component;
    private Entity mockEntity;
    private EventHandler mockEvents;

    @BeforeEach
    void setUp() {
        component = new PauseEscInputComponent(10);
        mockEntity = mock(Entity.class);
        mockEvents = mock(EventHandler.class);

        when(mockEntity.getEvents()).thenReturn(mockEvents);
        component.setEntity(mockEntity);
    }


    @Test
    void testPressNonEscapeKeyDoesNothing() {
        boolean result = component.keyPressed(Input.Keys.SPACE);

        verify(mockEntity.getEvents(), never()).trigger(anyString());
        assertFalse(result);
    }

    @Test
    void testKeyReleasedAlwaysReturnsFalse() {
        assertFalse(component.keyReleased(Input.Keys.ESCAPE));
        assertFalse(component.keyReleased(Input.Keys.SPACE));
    }


    }

