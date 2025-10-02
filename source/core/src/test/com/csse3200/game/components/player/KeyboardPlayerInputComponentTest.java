package com.csse3200.game.components.player;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.utils.math.Vector2Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;

public class KeyboardPlayerInputComponentTest {
    Entity player;
    EventHandler events;

    KeyboardPlayerInputComponent component;

    @BeforeEach
    public void setup() {
        component = new KeyboardPlayerInputComponent();
        player = new Entity().addComponent(component);
    }

    @Test
    public void testTriggersWalkEvent() {
        verify(events).trigger("walk", Vector2Utils.LEFT);
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
