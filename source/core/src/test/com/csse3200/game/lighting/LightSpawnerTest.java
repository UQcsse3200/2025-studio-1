package com.csse3200.game.lighting;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LightSpawnerTest {

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    @Test
    void spawnsLights() {
        GameArea mockArea = mock(GameArea.class);
        List<GridPoint2> positions = List.of(
                new GridPoint2(1,1),
                new GridPoint2(2,2)
        );
        Color color = new Color(1f,0f,0f,1f);

        LightSpawner.spawnCeilingCones(mockArea, positions, color);

        for (GridPoint2 pos : positions) {
            verify(mockArea, times(1)).spawnEntityPublic(any(Entity.class), eq(pos), eq(true), eq(true));
        }
    }
}
