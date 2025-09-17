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

        doNothing().when(shippingGameArea)
                .spawnEntityAt(any(Entity.class), any(GridPoint2.class), anyBoolean(), anyBoolean());
    }

    @Test
    void testSpawnPlayerCallsPlayerFactory() throws Exception {
        try (MockedStatic<PlayerFactory> playerFactoryMock = mockStatic(PlayerFactory.class)) {
            Entity mockPlayer = mock(Entity.class);
            playerFactoryMock.when(PlayerFactory::createPlayer).thenReturn(mockPlayer);

            var method = ShippingGameArea.class.getDeclaredMethod("spawnPlayer");
            method.setAccessible(true);
            method.invoke(shippingGameArea); // returns null because method is void

            // Verify PlayerFactory was used
            playerFactoryMock.verify(PlayerFactory::createPlayer);

            // Verify the player was spawned
            verify(shippingGameArea).spawnEntityAt(eq(mockPlayer), any(GridPoint2.class), eq(true), eq(true));
        }
    }

    @Test
    void testTraversals() throws Exception {
        doNothing().when(shippingGameArea).clearAndLoad(any());

        var method = ShippingGameArea.class.getDeclaredMethod("loadResearch");
        method.setAccessible(true);
        method.invoke(shippingGameArea);

        verify(shippingGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof ResearchGameArea;
        }));

        var method2 = ShippingGameArea.class.getDeclaredMethod("loadStorage");
        method2.setAccessible(true);
        method2.invoke(shippingGameArea);

        verify(shippingGameArea).clearAndLoad(argThat(supplier -> {
            return supplier.get() instanceof StorageGameArea;
        }));
    }
}
