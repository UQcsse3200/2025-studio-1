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
class SecurityAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private SecurityGameArea securityGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        securityGameArea = spy(new SecurityGameArea(terrainFactory, cameraComponent));

        doNothing().when(securityGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = SecurityGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(securityGameArea); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(securityGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(securityGameArea).clearAndLoad(any());

        var method = SecurityGameArea.class.getDeclaredMethod("loadBackToFloor5");
        method.setAccessible(true);
        method.invoke(securityGameArea);

        verify(securityGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof MainHall;
        }));

        var method2 = SecurityGameArea.class.getDeclaredMethod("loadOffice");
        method2.setAccessible(true);
        method2.invoke(securityGameArea);

        verify(securityGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof OfficeGameArea;
        }));
    }
}
