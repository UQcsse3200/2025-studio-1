package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.PromptFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class CasinoGameAreaTest {

    private TerrainFactory terrainFactory;
    private CameraComponent cameraComponent;
    private CasinoGameArea casino;

    @BeforeAll
    static void setUpStaticMocks() {
        MockedStatic<PromptFactory> pf = mockStatic(PromptFactory.class);
        pf.when(PromptFactory::createPrompt).thenAnswer(invocation -> null);
    }

    @BeforeEach
    void setUp() {
        ServiceLocator.registerEntityService(new EntityService());
        ResourceService rs = mock(ResourceService.class);
        ServiceLocator.registerResourceService(rs);

        terrainFactory = mock(TerrainFactory.class);
        cameraComponent = mock(CameraComponent.class);


        casino = spy(new CasinoGameArea(terrainFactory, cameraComponent));

        doNothing().when(casino)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());

    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = CasinoGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            Entity result = (Entity) method.invoke(casino);

            playerFactoryMock.verify(PlayerFactory::createPlayer);
            verify(casino).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
            assertSame(mockPlayer, result);
        }
    }

    @Test
    void testTraversalToForestArea() throws Exception {
        doNothing().when(casino).clearAndLoad(any());
        var method = CasinoGameArea.class.getDeclaredMethod("loadSpawnFromCasino");
        method.setAccessible(true);
        method.invoke(casino);

        verify(casino).clearAndLoad(argThat(supplier -> supplier.get() instanceof ForestGameArea));
        ResourceService rs = ServiceLocator.getResourceService();
        verify(rs, atLeastOnce()).unloadAssets(any(String[].class));
    }


}