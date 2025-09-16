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
class ServerAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private ServerGameArea serverGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        serverGameArea = spy(new ServerGameArea(terrainFactory, cameraComponent));

        doNothing().when(serverGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class);
        Entity mockPlayer = mock(Entity.class);
        playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

        var method = ServerGameArea.class.getDeclaredMethod("spawnPlayer");
        method.setAccessible(true);
        Entity result = (Entity) method.invoke(serverGameArea);

        // Verify PlayerFactory used and player spawned
        playerFactoryMock.verify(PlayerFactory::createPlayer);
        verify(serverGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        Assertions.assertEquals(mockPlayer, result);
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(serverGameArea).clearAndLoad(any());

        var method = ServerGameArea.class.getDeclaredMethod("loadTunnel");
        method.setAccessible(true);
        method.invoke(serverGameArea);

        verify(serverGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof TunnelGameArea;
        }));

        var method2 = ServerGameArea.class.getDeclaredMethod("loadStorage");
        method2.setAccessible(true);
        method2.invoke(serverGameArea);

        verify(serverGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof StorageGameArea;
        }));

    }
}
