package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainComponent;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class GameAreaSpawnTest {

    private TerrainFactory mockFactory;
    private CameraComponent mockCamera;
    private GameArea mockArea;

    @BeforeEach
    void setup() throws Exception {
        ServiceLocator.registerEntityService(new EntityService());

        // Mock dependencies
        mockFactory = mock(TerrainFactory.class);
        mockCamera = mock(CameraComponent.class);

        // Mock GameArea
        mockArea = mock(GameArea.class, withSettings()
                .useConstructor(mockFactory, mockCamera)  // calls real constructor so fields exist
                .defaultAnswer(CALLS_REAL_METHODS));      // by default, use real methods

        // Fix private areaEntities since constructor of abstract may not fully init it
        var entitiesField = GameArea.class.getDeclaredField("areaEntities");
        entitiesField.setAccessible(true);
        if (entitiesField.get(mockArea) == null) {
            entitiesField.set(mockArea, new java.util.ArrayList<>());
        }
    }

    @Test
    void spawnEntityAtCentersCorrectly() {
        // Mock terrain behaviour
        TerrainComponent terrain = mock(TerrainComponent.class);
        when(terrain.tileToWorldPosition(any(GridPoint2.class)))
                .thenAnswer(inv -> {
                    GridPoint2 gp = inv.getArgument(0);
                    return new Vector2(gp.x * 2f, gp.y * 2f);
                });
        when(terrain.getTileSize()).thenReturn(2f);

        mockArea.terrain = terrain;

        Entity e = new Entity();
        e.setPosition(0f, 0f);

        // No centering
        mockArea.spawnEntityAt(e, new GridPoint2(3, 4), false, false);

        Vector2 pos = e.getPosition();
        assertEquals(6f, pos.x, 0.001f);
        assertEquals(8f, pos.y, 0.001f);
    }

    @RepeatedTest(5)
    void roomSpawnPositionWithinRanges() {
        Vector2 p2 = mockArea.getRoomSpawnPosition("Floor2");
        assertTrue(p2.x >= 4f && p2.x <= 18f);
        assertTrue(p2.y >= 4f && p2.y <= 18f);

        Vector2 p7 = mockArea.getRoomSpawnPosition("Floor7");
        assertTrue(p7.x >= 9f && p7.x <= 28f);
        assertTrue(p7.y >= 9f && p7.y <= 28f);

        Vector2 def = mockArea.getRoomSpawnPosition("Unknown");
        assertEquals(0f, def.x, 0.0001f);
        assertEquals(0f, def.y, 0.0001f);
    }
}
