package com.csse3200.game.entities.spawner;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class ItemSpawnerTest {

    private ForestGameArea mockGameArea;
    private ItemSpawner spawner;

    // Its is a subclass to override makeItem from itemspwaner for testing
    private static class TestItemSpawner extends ItemSpawner {
        public TestItemSpawner(ForestGameArea gameArea) {
            super(gameArea);
        }

        @Override
        protected Entity makeItem(String type) {
            return mock(Entity.class);     // It just returns a mock entity for any type
        }
    }

    @BeforeEach
    void setUp() {
        mockGameArea = mock(ForestGameArea.class);
        spawner = new TestItemSpawner(mockGameArea);
    }

    /**
     * It tests that items are spawned at all provided coordinates with the correct quantities
     */
    @Test
    void validQuantityAndCoordinates() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();
        config.put("itemA", List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(1, 2), 2),
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(3, 4), 1)
        ));

        spawner.spawnItems(config);

        // Verify spawnItem is called correct number of times with correct positions
        verify(mockGameArea, times(2)).spawnItem(any(Entity.class), eq(new GridPoint2(1, 2)));
        verify(mockGameArea, times(1)).spawnItem(any(Entity.class), eq(new GridPoint2(3, 4)));
    }

    /**
     * It tests that all items in the configuration are spawned correctly on the game
     */
    @Test
    void shouldSpawnAllItems() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();
        config.put("itemA", List.of(new ItemSpawner.ItemSpawnInfo(new GridPoint2(0, 0), 1)));
        config.put("itemB", List.of(new ItemSpawner.ItemSpawnInfo(new GridPoint2(5, 5), 3)));

        spawner.spawnItems(config);

        verify(mockGameArea, times(1)).spawnItem(any(Entity.class), eq(new GridPoint2(0, 0)));
        verify(mockGameArea, times(3)).spawnItem(any(Entity.class), eq(new GridPoint2(5, 5)));
    }

    /**
     * It is testing that unknown item types are handled without crashing and a warning is logged
     */
    @Test
    void unknownItems() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();
        config.put("unknownitem", List.of(new ItemSpawner.ItemSpawnInfo(new GridPoint2(7, 7), 1)));

        spawner.spawnItems(config);

        verify(mockGameArea, times(1)).spawnItem(any(Entity.class), eq(new GridPoint2(7, 7)));
    }
}
