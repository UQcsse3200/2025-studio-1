package com.csse3200.game.areas;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

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

        // Don’t execute real spawns during test
        doNothing().when(serverGameArea).spawnEntity(any(Entity.class));
        doNothing()
                .when(serverGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    private static Method spawnPlayerMethod() throws Exception {
        Method m = GameArea.class.getDeclaredMethod("spawnPlayer", PlayerSpawnSpec.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testSpawnPlayer_usesPlayerFactory_andSpawnsAtSpecCellWithDefaultFlags() throws Exception {
        Entity mockPlayer = mock(Entity.class);

        try (MockedStatic<PlayerFactory> pf = mockStatic(PlayerFactory.class)) {
            pf.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            // Explicit cell so we can assert forwarding
            GridPoint2 cell = new GridPoint2(7, 9);
            PlayerSpawnSpec spec = PlayerSpawnSpec.of("server-room", cell); // defaults center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(serverGameArea, spec);

            // Factory invoked
            pf.verify(PlayerFactory::createPlayer);

            // Spawned at given cell with default flags
            verify(serverGameArea).spawnEntityAt(eq(mockPlayer), eq(cell), eq(true), eq(true));

            // Method returns the player
            assert ret == mockPlayer;
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(serverGameArea).clearAndLoad(any());

        var m1 = ServerGameArea.class.getDeclaredMethod("loadTunnel");
        m1.setAccessible(true);
        m1.invoke(serverGameArea);
        verify(serverGameArea).clearAndLoad(argThat(s -> s.get() instanceof TunnelGameArea));

        var m2 = ServerGameArea.class.getDeclaredMethod("loadStorage");
        m2.setAccessible(true);
        m2.invoke(serverGameArea);
        verify(serverGameArea).clearAndLoad(argThat(s -> s.get() instanceof StorageGameArea));
    }
}
