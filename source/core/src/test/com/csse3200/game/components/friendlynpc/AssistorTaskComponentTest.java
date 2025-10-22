package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.events.listeners.EventListener0;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssistorTaskComponentTest {

    private static Application originalApp;

    private Entity npc;
    private Entity player;
    private Entity partner;
    private EntityService entityService;
    private EventHandler events;

    @BeforeAll
    static void stashGdxApp() {
        originalApp = Gdx.app;
    }

    @AfterAll
    static void restoreGdxApp() {
        Gdx.app = originalApp;
    }

    @BeforeEach
    void setup() {
        npc = mock(Entity.class);
        player = mock(Entity.class);
        partner = mock(Entity.class);
        entityService = mock(EntityService.class);
        events = mock(EventHandler.class);

        when(npc.getEvents()).thenReturn(events);
        when(npc.getPosition()).thenReturn(new Vector2(3f, 4f));
    }

    @Test
    void onDialogueEnd_postsRunnable_thenRunnableCreatesAndRegistersPartner() {
        try (MockedStatic<FriendlyNPCFactory> factoryMock = mockStatic(FriendlyNPCFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {
            factoryMock.when(() -> FriendlyNPCFactory.createPartner(player)).thenReturn(partner);
            slMock.when(ServiceLocator::getEntityService).thenReturn(entityService);
            Application mockApp = mock(Application.class);
            Gdx.app = mockApp;
            AssistorTaskComponent c = new AssistorTaskComponent(player);
            c.setEntity(npc);
            c.create();
            ArgumentCaptor<String> nameCap = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<EventListener0> listenerCap = ArgumentCaptor.forClass(EventListener0.class);
            verify(events, times(1)).addListener(nameCap.capture(), listenerCap.capture());
            assertEquals("npcDialogueEnd", nameCap.getValue());
            EventListener0 listener = listenerCap.getValue();
            assertNotNull(listener);
            listener.handle();
            ArgumentCaptor<Runnable> runCap = ArgumentCaptor.forClass(Runnable.class);
            verify(mockApp, times(1)).postRunnable(runCap.capture());
            Runnable scheduled = runCap.getValue();
            assertNotNull(scheduled, "Expected a Runnable to be posted");
            scheduled.run();
            factoryMock.verify(() -> FriendlyNPCFactory.createPartner(player), times(1));
            verify(partner, times(1)).setPosition(3f, 4f);
            verify(entityService, times(1)).register(partner);
        }
    }
}




