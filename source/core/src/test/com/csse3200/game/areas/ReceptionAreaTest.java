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

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class ReceptionAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private ReceptionGameArea receptionGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        receptionGameArea = spy(new ReceptionGameArea(terrainFactory, cameraComponent));

        // Don’t let real spawns run during tests
        doNothing().when(receptionGameArea).spawnEntity(any(Entity.class));
        doNothing()
                .when(receptionGameArea)
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

            // Use an explicit cell so we assert it’s forwarded correctly
            GridPoint2 cell = new GridPoint2(2, 3);
            PlayerSpawnSpec spec = PlayerSpawnSpec.of("reception", cell); // defaults: center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(receptionGameArea, spec);

            // Factory was used
            pf.verify(PlayerFactory::createPlayer);

            // Spawned at the given cell with default flags
            verify(receptionGameArea).spawnEntityAt(eq(mockPlayer), eq(cell), eq(true), eq(true));

            // Method should return the player
            assert ret == mockPlayer;
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(receptionGameArea).clearAndLoad(any());

        var backToFloor = ReceptionGameArea.class.getDeclaredMethod("loadBackToFloor5");
        backToFloor.setAccessible(true);
        backToFloor.invoke(receptionGameArea);
        verify(receptionGameArea).clearAndLoad(argThat(supplier -> supplier.get() instanceof MainHall));

        var loadForest = ReceptionGameArea.class.getDeclaredMethod("loadForest");
        loadForest.setAccessible(true);
        loadForest.invoke(receptionGameArea);
        verify(receptionGameArea).clearAndLoad(argThat(supplier -> supplier.get() instanceof ForestGameArea));
    }
}
