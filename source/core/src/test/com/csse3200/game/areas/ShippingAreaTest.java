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
class ShippingAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private ShippingGameArea shippingGameArea;

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        shippingGameArea = spy(new ShippingGameArea(terrainFactory, cameraComponent));

        // Don’t execute real spawns during test
        doNothing().when(shippingGameArea).spawnEntity(any(Entity.class));
        doNothing()
                .when(shippingGameArea)
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

            // Choose a specific cell so we can assert forwarding
            GridPoint2 cell = new GridPoint2(8, 6);
            PlayerSpawnSpec spec = PlayerSpawnSpec.of("shipping", cell); // defaults: center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(shippingGameArea, spec);

            // Factory used
            pf.verify(PlayerFactory::createPlayer);

            // Spawned at given cell with default flags
            verify(shippingGameArea).spawnEntityAt(eq(mockPlayer), eq(cell), eq(true), eq(true));

            // spawnPlayer returns the created player
            assert ret == mockPlayer;
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(shippingGameArea).clearAndLoad(any());

        var loadResearch = ShippingGameArea.class.getDeclaredMethod("loadResearch");
        loadResearch.setAccessible(true);
        loadResearch.invoke(shippingGameArea);
        verify(shippingGameArea).clearAndLoad(argThat(s -> s.get() instanceof ResearchGameArea));

        var loadStorage = ShippingGameArea.class.getDeclaredMethod("loadStorage");
        loadStorage.setAccessible(true);
        loadStorage.invoke(shippingGameArea);
        verify(shippingGameArea).clearAndLoad(argThat(s -> s.get() instanceof StorageGameArea));
    }
}
