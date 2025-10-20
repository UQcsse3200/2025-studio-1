package com.csse3200.game.ui.terminal.commands.util;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommandPlayersTest {

    @Test
    void resolve_prefers_global_player_over_keyboardControlled() {
        Entity global = mock(Entity.class);
        EntityService es = mock(EntityService.class);

        // have a keyboard-controlled entity present, but global should win
        Entity keyboard = mock(Entity.class);
        when(keyboard.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));
        Array<Entity> list = new Array<>();
        list.add(keyboard);
        when(es.getEntities()).thenReturn(list);

        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            svc.when(ServiceLocator::getPlayer).thenReturn(global);

            Entity out = CommandPlayers.resolve(es);
            assertSame(global, out);
            svc.verify(ServiceLocator::getPlayer);
        }
    }

    @Test
    void resolve_fallsBack_to_first_keyboardControlled_when_no_global_player() {
        EntityService es = mock(EntityService.class);

        Entity e1 = mock(Entity.class);
        when(e1.getComponent(KeyboardPlayerInputComponent.class)).thenReturn(null);
        Entity e2 = mock(Entity.class);
        when(e2.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));
        Entity e3 = mock(Entity.class);
        when(e3.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));

        Array<Entity> list = new Array<>();
        list.addAll(e1, e2, e3);
        when(es.getEntities()).thenReturn(list);

        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            svc.when(ServiceLocator::getPlayer).thenReturn(null);

            Entity out = CommandPlayers.resolve(es);
            assertSame(e2, out); // first match
        }
    }

    @Test
    void resolve_returnsNull_when_no_global_and_no_service_or_no_match() {
        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            svc.when(ServiceLocator::getPlayer).thenReturn(null);

            // null EntityService
            assertNull(CommandPlayers.resolve(null));

            // empty EntityService
            EntityService es = mock(EntityService.class);
            when(es.getEntities()).thenReturn(new Array<>());
            assertNull(CommandPlayers.resolve(es));

            // no keyboard-controlled in list
            Entity e = mock(Entity.class);
            when(e.getComponent(KeyboardPlayerInputComponent.class)).thenReturn(null);
            Array<Entity> list = new Array<>();
            list.add(e);
            when(es.getEntities()).thenReturn(list);
            assertNull(CommandPlayers.resolve(es));
        }
    }

    @Test
    void findKeyboardControlled_handles_null_empty_and_noMatch() {
        assertNull(CommandPlayers.findKeyboardControlled(null));

        Array<Entity> empty = new Array<>();
        assertNull(CommandPlayers.findKeyboardControlled(empty));

        Entity e = mock(Entity.class);
        when(e.getComponent(KeyboardPlayerInputComponent.class)).thenReturn(null);
        Array<Entity> list = new Array<>();
        list.add(e);
        assertNull(CommandPlayers.findKeyboardControlled(list));
    }

    @Test
    void findKeyboardControlled_returns_first_match() {
        Entity e1 = mock(Entity.class);
        when(e1.getComponent(KeyboardPlayerInputComponent.class)).thenReturn(null);
        Entity e2 = mock(Entity.class);
        when(e2.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));
        Entity e3 = mock(Entity.class);
        when(e3.getComponent(KeyboardPlayerInputComponent.class))
                .thenReturn(mock(KeyboardPlayerInputComponent.class));

        Array<Entity> list = new Array<>();
        list.addAll(e1, e2, e3);

        Entity out = CommandPlayers.findKeyboardControlled(list);
        assertSame(e2, out); // first with KeyboardPlayerInputComponent
    }
}
