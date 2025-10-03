package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PickupAllCommandTests {

    private static ArrayList<String> noArgs() {
        return new ArrayList<>();
    }

    @Test
    void triggersPickupAll_whenPlayerFound() {
        EntityService es = mock(EntityService.class);
        Entity player = mock(Entity.class);

        // player has KeyboardPlayerInputComponent
        when(player.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));

        // player has events
        var events = mock(EventHandler.class);
        when(player.getEvents()).thenReturn(events);

        Array<Entity> entities = new Array<>();
        entities.add(player);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            PickupAllCommand cmd = new PickupAllCommand();
            boolean ok = cmd.action(noArgs());

            assertTrue(ok);
            verify(events).trigger("pickupAll");
        }
    }

    @Test
    void returnsFalse_whenNoPlayerPresent() {
        EntityService es = mock(EntityService.class);
        Entity notPlayer = mock(Entity.class);
        when(notPlayer.getComponent(KeyboardPlayerInputComponent.class)).thenReturn(null);

        Array<Entity> entities = new Array<>();
        entities.add(notPlayer);
        when(es.getEntities()).thenReturn(entities);

        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(es);

            PickupAllCommand cmd = new PickupAllCommand();
            var events = mock(EventHandler.class);
            when(notPlayer.getEvents()).thenReturn(events);
            assertFalse(cmd.action(noArgs()));
            verify(notPlayer, times(1)).getComponent(KeyboardPlayerInputComponent.class);
        }
    }

    @Test
    void returnsFalse_whenNoEntityService() {
        try (MockedStatic<ServiceLocator> sl = mockStatic(ServiceLocator.class)) {
            sl.when(ServiceLocator::getEntityService).thenReturn(null);

            PickupAllCommand cmd = new PickupAllCommand();
            assertFalse(cmd.action(noArgs()));
        }
    }
}