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
class ElevatorAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private ElevatorGameArea elevatorGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        elevatorGameArea = spy(new ElevatorGameArea(terrainFactory, cameraComponent));

        doNothing().when(elevatorGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = ElevatorGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(elevatorGameArea); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(elevatorGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(elevatorGameArea).clearAndLoad(any());

        var method = ElevatorGameArea.class.getDeclaredMethod("loadOffice");
        method.setAccessible(true);
        method.invoke(elevatorGameArea);

        verify(elevatorGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof OfficeGameArea;
        }));

        var method2 = ElevatorGameArea.class.getDeclaredMethod("loadResearch");
        method2.setAccessible(true);
        method2.invoke(elevatorGameArea);

        verify(elevatorGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ResearchGameArea;
        }));
    }
}
