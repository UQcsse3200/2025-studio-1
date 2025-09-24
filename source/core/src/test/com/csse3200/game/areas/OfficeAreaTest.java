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
class OfficeAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private OfficeGameArea officeGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        officeGameArea = spy(new OfficeGameArea(terrainFactory, cameraComponent));

        doNothing().when(officeGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = OfficeGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(officeGameArea); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(officeGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(officeGameArea).clearAndLoad(any());

        var method = OfficeGameArea.class.getDeclaredMethod("loadSecurity");
        method.setAccessible(true);
        method.invoke(officeGameArea);

        verify(officeGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof SecurityGameArea;
        }));

        var method2 = OfficeGameArea.class.getDeclaredMethod("loadElevator");
        method2.setAccessible(true);
        method2.invoke(officeGameArea);

        verify(officeGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ElevatorGameArea;
        }));
    }
}
