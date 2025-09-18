package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ReceptionAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private Reception reception;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        reception = spy(new Reception(terrainFactory, cameraComponent));

        doNothing().when(reception)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = Reception.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(reception); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(reception).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(reception).clearAndLoad(any());

        var method = Reception.class.getDeclaredMethod("loadBackToFloor5");
        method.setAccessible(true);
        method.invoke(reception);

        verify(reception).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof MainHall;
        }));

        var method2 = Reception.class.getDeclaredMethod("loadForest");
        method2.setAccessible(true);
        method2.invoke(reception);

        verify(reception).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ForestGameArea;
        }));
    }
}
