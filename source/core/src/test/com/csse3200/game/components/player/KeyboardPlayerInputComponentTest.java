package com.csse3200.game.components.player;

import com.badlogic.gdx.Input;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.utils.math.Vector2Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeyboardPlayerInputComponentTest {
    Entity player;
    EventHandler events;

    KeyboardPlayerInputComponent component;

    @BeforeEach
    public void setup() {
        component = new KeyboardPlayerInputComponent();
        player = new Entity().addComponent(component);

        when(player.getEvents()).thenReturn(events);
        component.setEntity(player);
    }

    @Test
    public void testTriggersWalkEvent() {
        component.keyPressed(Input.Keys.A);
        verify(events).trigger("walk", Vector2Utils.LEFT);

        component.keyPressed(Input.Keys.D);
        verify(events).trigger("walk", Vector2Utils.RIGHT);
    }

    @Test
    public void checkSlotTest() {
    }

    @Test
    public void equipCurrentItemTest() {
    }

    @Test
    public void unequipCurrentItemTest() {
    }

}
