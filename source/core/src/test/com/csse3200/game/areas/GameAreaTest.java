package com.csse3200.game.areas;

import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaTest {

    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerEntityService(new EntityService());
    }

    @Test
    void shouldSpawnAndDisposeEntities() {
        GameArea gameArea = mock(GameArea.class);
        gameArea.areaEntities = new ArrayList<>(); // manually initialize

        doCallRealMethod().when(gameArea).spawnEntity(any(Entity.class));
        doCallRealMethod().when(gameArea).dispose();

        Entity mockEntity = mock(Entity.class);

        gameArea.spawnEntity(mockEntity);
        verify(mockEntity).create();

        gameArea.dispose();
        verify(mockEntity).dispose();
    }

    @Test
    void settingInitialSpawns() throws Exception {
        ServiceLocator.registerEntityService(new EntityService());
        TerrainFactory terrainFactory = mock(TerrainFactory.class);
        CameraComponent cameraComponent = mock(CameraComponent.class);

        Field f1 = ForestGameArea.class.getDeclaredField("PLAYER_SPAWN");
        Field f2 = ResearchGameArea.class.getDeclaredField("PLAYER_SPAWN");
        Field f3 = ShippingGameArea.class.getDeclaredField("PLAYER_SPAWN");

        f1.setAccessible(true);
        f2.setAccessible(true);
        f3.setAccessible(true);

        assert (f1.get(null) != null);
        assert (f2.get(null) != null);
        assert (f3.get(null) != null);
    }
}
