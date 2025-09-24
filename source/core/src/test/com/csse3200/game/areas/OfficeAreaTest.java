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

        // Don’t execute real spawns during test
        doNothing().when(officeGameArea).spawnEntity(any(Entity.class));
        doNothing()
                .when(officeGameArea)
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

            // Choose a specific cell so we can assert it’s forwarded correctly
            GridPoint2 cell = new GridPoint2(5, 6);
            PlayerSpawnSpec spec = PlayerSpawnSpec.of("office", cell); // defaults: center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(officeGameArea, spec);

            // Factory used
            pf.verify(PlayerFactory::createPlayer);

            // Spawned at the given cell with default flags
            verify(officeGameArea).spawnEntityAt(eq(mockPlayer), eq(cell), eq(true), eq(true));

            // Method should return the player entity
            assert ret == mockPlayer;
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(officeGameArea).clearAndLoad(any());

        var m1 = OfficeGameArea.class.getDeclaredMethod("loadSecurity");
        m1.setAccessible(true);
        m1.invoke(officeGameArea);
        verify(officeGameArea).clearAndLoad(argThat(supplier -> supplier.get() instanceof SecurityGameArea));

        var m2 = OfficeGameArea.class.getDeclaredMethod("loadElevator");
        m2.setAccessible(true);
        m2.invoke(officeGameArea);
        verify(officeGameArea).clearAndLoad(argThat(supplier -> supplier.get() instanceof ElevatorGameArea));
    }
}
