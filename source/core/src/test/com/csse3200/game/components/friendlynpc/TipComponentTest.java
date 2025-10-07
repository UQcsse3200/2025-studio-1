package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.EntityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

public class TipComponentTest {
    private Entity npc;
    private Entity player;
    private Entity tipEntity;
    private EntityService entityService;

    private final Vector2 npcPos = new Vector2(2f, 3f);
    private final Vector2 playerPos = new Vector2(2f, 3.5f); // 初始距离 0.5

    @BeforeEach
    void setup() {
        npc = mock(Entity.class);
        player = mock(Entity.class);
        tipEntity = mock(Entity.class);
        entityService = mock(EntityService.class);

        when(npc.getPosition()).thenReturn(npcPos);
        when(player.getPosition()).thenReturn(playerPos);
    }

    @Test
    void createsTipRegistersAndPositions_whenWithinDistance_andNoExistingTip() {
        try (MockedStatic<FriendlyNPCFactory> factoryMock = mockStatic(FriendlyNPCFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            factoryMock.when(FriendlyNPCFactory::createTip).thenReturn(tipEntity);
            slMock.when(ServiceLocator::getEntityService).thenReturn(entityService);

            TipComponent c = new TipComponent(npc, player, /*triggerDist=*/1f);

            c.update();

            factoryMock.verify(() -> FriendlyNPCFactory.createTip(), times(1));
            verify(entityService, times(1)).register(tipEntity);
            verify(tipEntity, times(1)).setPosition(2f, 4f); // y = 3 + 1

            c.update();
            factoryMock.verify(() -> FriendlyNPCFactory.createTip(), times(1));
            verify(entityService, times(1)).register(tipEntity);
        }
    }

    @Test
    void doesNothing_whenOutsideDistance_andNoExistingTip() {
        playerPos.set(10f, 10f);

        try (MockedStatic<FriendlyNPCFactory> factoryMock = mockStatic(FriendlyNPCFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            slMock.when(ServiceLocator::getEntityService).thenReturn(entityService);

            TipComponent c = new TipComponent(npc, player, 1f);
            c.update();

            factoryMock.verifyNoInteractions();
            verify(entityService, never()).register(any());
        }
    }

    @Test
    void unregistersAndDisposes_whenMovedOutsideAfterCreated() {
        try (MockedStatic<FriendlyNPCFactory> factoryMock = mockStatic(FriendlyNPCFactory.class);
             MockedStatic<ServiceLocator> slMock = mockStatic(ServiceLocator.class)) {

            factoryMock.when(FriendlyNPCFactory::createTip).thenReturn(tipEntity);
            slMock.when(ServiceLocator::getEntityService).thenReturn(entityService);

            TipComponent c = new TipComponent(npc, player, 1f);

            c.update();
            factoryMock.verify(() -> FriendlyNPCFactory.createTip(), times(1));
            verify(entityService, times(1)).register(tipEntity);

            playerPos.set(10f, 10f);
            c.update();

            verify(entityService, times(1)).unregister(tipEntity);
            verify(tipEntity, times(1)).dispose();

            c.update();
            verify(entityService, times(1)).unregister(tipEntity);
            verify(tipEntity, times(1)).dispose();
        }
    }
}
