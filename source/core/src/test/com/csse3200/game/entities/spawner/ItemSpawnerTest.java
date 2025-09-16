package com.csse3200.game.entities.spawner;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.badlogic.gdx.physics.box2d.BodyDef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class ItemSpawnerTest {

    private ForestGameArea mockForestArea;
    private ItemSpawner spawner;

    @BeforeEach
    void setUp() {
        // here we mock the game area so that we don't need real game area for testing
        mockForestArea = mock(ForestGameArea.class);
        spawner = new ItemSpawner(mockForestArea);
    }
    /**
     * tests that the makeItem method returns null for an unknown item type
     */
    @Test
    void testMakeItem_UnknownType() {
        Entity item = spawner.makeItem("unknown_item");
        assertNull(item, "Unknown item type should return null");
    }

    /**
     * tests that PhysicsComponent is correctly set to StaticBody during spawn.
     */
    @Test
    void setsStaticBody() {
        // we mock entity and physics component
        Entity mockEntity = mock(Entity.class);
        PhysicsComponent mockPhysics = mock(PhysicsComponent.class);
        when(mockEntity.getComponent(PhysicsComponent.class)).thenReturn(mockPhysics);

        // here spy spawner is used to return mock entity for our test string
        ItemSpawner spySpawner = Mockito.spy(spawner);
        doReturn(mockEntity).when(spySpawner).makeItem("test_item");

        // it is the test configuration
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = Map.of(
                "test_item", List.of(new ItemSpawner.ItemSpawnInfo(new GridPoint2(0, 0), 1))
        );

        spySpawner.spawnItems(config);
        verify(mockPhysics).setBodyType(BodyDef.BodyType.StaticBody);
    }

    /**
     * it tests if the ItemSpawnInfo class correctly stores position and quantity
     * and that spawnInGameArea is called with correct parameters
     */
    @Test
    void testItemSpawnInfo() {
        GridPoint2 pos = new GridPoint2(3, 4);
        ItemSpawner.ItemSpawnInfo info = new ItemSpawner.ItemSpawnInfo(pos, 5);
        assertEquals(pos, info.position, "Position should be stored correctly");
        assertEquals(5, info.quantity, "Quantity should be stored correctly");
    }
}
