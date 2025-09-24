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
class MainHallAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private MainHall mainHall;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        mainHall = spy(new MainHall(terrainFactory, cameraComponent));

        // Don’t execute real spawns during test
        doNothing().when(mainHall).spawnEntity(any(Entity.class));
        doNothing()
                .when(mainHall)
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

            // Choose an explicit cell so we can assert it’s forwarded correctly
            GridPoint2 cell = new GridPoint2(4, 5);
            PlayerSpawnSpec spec = PlayerSpawnSpec.of("main-hall", cell); // defaults: center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(mainHall, spec);

            // Factory was invoked
            pf.verify(PlayerFactory::createPlayer);

            // Player spawned at the given cell with default flags
            verify(mainHall).spawnEntityAt(eq(mockPlayer), eq(cell), eq(true), eq(true));

            // spawnPlayer(...) should return the player entity
            assert ret == mockPlayer;
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(mainHall).clearAndLoad(any());

        var m1 = MainHall.class.getDeclaredMethod("loadBackToFloor2");
        m1.setAccessible(true);
        m1.invoke(mainHall);
        verify(mainHall).clearAndLoad(argThat(supplier -> supplier.get() instanceof ReceptionGameArea));

        var m2 = MainHall.class.getDeclaredMethod("loadSecurity");
        m2.setAccessible(true);
        m2.invoke(mainHall);
        verify(mainHall).clearAndLoad(argThat(supplier -> supplier.get() instanceof SecurityGameArea));
    }
}
