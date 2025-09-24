package com.csse3200.game.areas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.extensions.GameExtension;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

@ExtendWith(GameExtension.class)
class ResearchAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private ResearchGameArea researchGameArea;

    @BeforeEach
    void setUp() {
        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);
        researchGameArea = spy(new ResearchGameArea(terrainFactory, cameraComponent));

        // Prevent side effects from real spawning
        doNothing()
                .when(researchGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    private static Method spawnPlayerMethod() throws Exception {
        Method m = GameArea.class.getDeclaredMethod("spawnPlayer", PlayerSpawnSpec.class);
        m.setAccessible(true);
        return m;
    }

    @Test
    void testSpawnPlayer_usesSpecSupplierAndFlags() throws Exception {
        Entity mockPlayer = mock(Entity.class);

        PlayerSpawnSpec spec =
                PlayerSpawnSpec.of("research", new GridPoint2(7, 8))
                        .withFactory(() -> mockPlayer)   // override supplier
                        .withCenter(false)               // custom flags
                        .withCollidable(false);

        Object ret = spawnPlayerMethod().invoke(researchGameArea, spec);

        // Uses the supplier-provided player and propagates flags and cell
        verify(researchGameArea)
                .spawnEntityAt(eq(mockPlayer), eq(new GridPoint2(7, 8)), eq(false), eq(false));

        assert ret == mockPlayer;
    }

    @Test
    void testSpawnPlayer_usesDefaultPlayerFactory() throws Exception {
        Entity mockPlayer = mock(Entity.class);
        try (MockedStatic<PlayerFactory> pf = mockStatic(PlayerFactory.class)) {
            pf.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            PlayerSpawnSpec spec = PlayerSpawnSpec.of("research", new GridPoint2(3, 4)); // defaults: center=true, collidable=true

            Object ret = spawnPlayerMethod().invoke(researchGameArea, spec);

            // Default factory must be invoked
            pf.verify(PlayerFactory::createPlayer);

            // Spawned at the spec cell with default flags
            verify(researchGameArea)
                    .spawnEntityAt(eq(mockPlayer), eq(new GridPoint2(3, 4)), eq(true), eq(true));

            assert ret == mockPlayer;
        }
    }

    @Test
    void testCreate_callsSpawnPlayerWithDefaultSpec() {
        Entity mockPlayer = mock(Entity.class);

        // Return a player when create() calls spawnPlayer(...)
        doReturn(mockPlayer).when(researchGameArea).spawnPlayer(any(PlayerSpawnSpec.class));

        // *** CRITICAL: bypass entity-building helpers that touch textures/assets ***
        // ResearchGameArea helpers:
        doNothing().when(researchGameArea).spawnBordersAndDoors();
        doNothing().when(researchGameArea).spawnPlatforms();
        doNothing().when(researchGameArea).spawnResearchProps();
        doNothing().when(researchGameArea).spawnEnemies();

        // GameArea helper used by create() (left/right door cells)
        doNothing().when(researchGameArea)
                .spawnObjectDoors(any(GridPoint2.class), any(GridPoint2.class));

        // Stub static layout calls + ItemSpawner construction to avoid assets
        try (MockedStatic<GenericLayout> gl = mockStatic(GenericLayout.class);
             MockedConstruction<ItemSpawner> itemSpawnerCtor =
                     mockConstruction(ItemSpawner.class, (mock, ctx) -> {
                         doNothing().when(mock).spawnItems(any());
                     })) {

            // Act
            researchGameArea.create();

            // Assert default spec: (10,10), center=true, collidable=true
            verify(researchGameArea)
                    .spawnPlayer(argThat(spec -> {
                        GridPoint2 gp = spec.cell();
                        return gp != null
                                && gp.x == 10 && gp.y == 10
                                && spec.centerOnTile()
                                && spec.collidable();
                    }));

            // Sanity: the heavy helpers were indeed bypassed
            verify(researchGameArea).spawnBordersAndDoors();
            verify(researchGameArea).spawnPlatforms();
            verify(researchGameArea).spawnResearchProps();
            verify(researchGameArea).spawnEnemies();
            verify(researchGameArea)
                    .spawnObjectDoors(any(GridPoint2.class), any(GridPoint2.class));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(researchGameArea).clearAndLoad(any());

        var loadElevator = ResearchGameArea.class.getDeclaredMethod("loadElevator");
        loadElevator.setAccessible(true);
        loadElevator.invoke(researchGameArea);

        verify(researchGameArea)
                .clearAndLoad(argThat(supplier -> supplier.get() instanceof ElevatorGameArea));

        var loadShipping = ResearchGameArea.class.getDeclaredMethod("loadShipping");
        loadShipping.setAccessible(true);
        loadShipping.invoke(researchGameArea);

        verify(researchGameArea)
                .clearAndLoad(argThat(supplier -> supplier.get() instanceof ShippingGameArea));
    }
}
