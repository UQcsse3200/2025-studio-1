package com.csse3200.game.areas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.ArrayList;

@ExtendWith(GameExtension.class)
class StorageAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private StorageGameArea storageGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        storageGameArea = spy(new StorageGameArea(terrainFactory, cameraComponent));

        doNothing().when(storageGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = StorageGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(storageGameArea); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(storageGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(storageGameArea).clearAndLoad(any());

        var method = StorageGameArea.class.getDeclaredMethod("loadServer");
        method.setAccessible(true);
        method.invoke(storageGameArea);

        verify(storageGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ServerGameArea;
        }));

        var method2 = StorageGameArea.class.getDeclaredMethod("loadShipping");
        method2.setAccessible(true);
        method2.invoke(storageGameArea);

        verify(storageGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ShippingGameArea;
        }));
    }
}
