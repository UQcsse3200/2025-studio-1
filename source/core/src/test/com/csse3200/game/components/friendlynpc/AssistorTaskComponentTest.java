// src/test/java/com/csse3200/game/components/friendlynpc/AssistorTaskComponentTest.java
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class AssistorTaskComponentTest {

    private Entity npc;              // Entity that owns the component under test
    private Entity player;           // Player entity passed into the component
    private Entity partner;          // Partner entity returned by the factory
    private EntityService entityService;
    private EventHandler events;     // Event bus attached to the NPC entity

    @BeforeEach
    void setup() {
        npc = mock(Entity.class);
        player = mock(Entity.class);
        partner = mock(Entity.class);
        entityService = mock(EntityService.class);
        events = mock(EventHandler.class);

        // NPC returns its event handler and current position
        when(npc.getEvents()).thenReturn(events);
        when(npc.getPosition()).thenReturn(new Vector2(3f, 4f));
    }

    @Test
    void create_registersListener_and_onDialogueEnd_createsMovesAndRegistersPartner() {
        // Mock static calls for the factory and service locator
        try (MockedStatic<FriendlyNPCFactory> factoryMock = mockStatic(FriendlyNPCFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            // ServiceLocator -> return mocked EntityService
            slMock.when(ServiceLocator::getEntityService).thenReturn(entityService);

            // FriendlyNPCFactory.createPartner(player) -> return mocked partner
            factoryMock.when(() -> FriendlyNPCFactory.createPartner(player)).thenReturn(partner);

            // Create component, attach to NPC, and call create() so it registers the listener
            AssistorTaskComponent c = spy(AssistorTaskComponent.class);
            doNothing().when(c).onDialogueEnd();
            c.setEntity(npc);  // If setEntity isn't available, assign via reflection or direct field access
            c.create();

            // Capture the exact arguments passed to addListener
            ArgumentCaptor<String> eventNameCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<EventListener0> listenerCap = ArgumentCaptor.forClass(EventListener0.class);

            verify(events, times(1)).addListener(eventNameCap.capture(), listenerCap.capture());

            // Assert the component subscribes to the expected event
            assertEquals("npcDialogueEnd", eventNameCap.getValue());

            // Invoke the captured zero-arg listener (EventListener0#handle)
            EventListener0 listener = listenerCap.getValue();
            assertNotNull(listener, "Listener should be registered");
            listener.handle();

            // onDialogueEnd() should create the partner, move it to NPC position, and register it
            factoryMock.verify(() -> FriendlyNPCFactory.createPartner(player), times(1));
            verify(partner, times(1)).setPosition(3f, 4f);
            verify(entityService, times(1)).register(partner);

            // Optional: trigger a second time to prove idempotent behavior (if allowed)
            listener.handle();
            factoryMock.verify(() -> FriendlyNPCFactory.createPartner(player), times(2));
            verify(partner, times(2)).setPosition(3f, 4f);
            verify(entityService, times(2)).register(partner);
        }
    }
}


