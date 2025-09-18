package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TunnelAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private TunnelGameArea tunnelGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        tunnelGameArea = spy(new TunnelGameArea(terrainFactory, cameraComponent));

        doNothing().when(tunnelGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = TunnelGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            Entity result = (Entity) method.invoke(tunnelGameArea);

            // Verify PlayerFactory used and player spawned
            playerFactoryMock.verify(PlayerFactory::createPlayer);
            verify(tunnelGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
            Assertions.assertEquals(mockPlayer, result);
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(tunnelGameArea).clearAndLoad(any());

        var method = TunnelGameArea.class.getDeclaredMethod("loadServer");
        method.setAccessible(true);
        method.invoke(tunnelGameArea);

        verify(tunnelGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ServerGameArea;
        }));
    }
}
