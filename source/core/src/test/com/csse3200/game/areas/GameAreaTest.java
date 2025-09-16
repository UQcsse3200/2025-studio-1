package com.csse3200.game.areas;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doCallRealMethod;

import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

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
}
