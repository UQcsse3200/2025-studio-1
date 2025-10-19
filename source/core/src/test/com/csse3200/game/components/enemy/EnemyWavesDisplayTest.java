package com.csse3200.game.components.enemy;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class EnemyWavesDisplayTest {
    private GameArea area;
    private Entity player;
    private EnemyWaves wavesManager;
    private EnemyWavesDisplay display;

    @BeforeEach
    void setup() {
        // Mock Stage to avoid LibGDX native calls
        Stage stage = mock(Stage.class);

        // Mock RenderService to return the Stage
        RenderService mockRenderService = mock(RenderService.class);
        when(mockRenderService.getStage()).thenReturn(stage);
        // Register the mocked RenderService in ServiceLocator
        ServiceLocator.registerRenderService(mockRenderService);

        area = mock(GameArea.class);
        player = mock(Entity.class);
        wavesManager = new EnemyWaves(area, player);
        display = new EnemyWavesDisplay(wavesManager);
    }

    @AfterEach
    void dispose() {
        // Clear services registered in ServiceLocator so next test starts fresh
        ServiceLocator.clear();
    }

    @Test
    @DisplayName("Check initial labels at class creation")
    void checkLabelInitial() {
        // Check number of enemies left
        Assertions.assertNotNull(display.getEnemyNumLabel());
        String expectedEnemyNum = "Enemies left: 0 / 0";
        Assertions.assertEquals(expectedEnemyNum, display.getEnemyNumLabel().getText().toString());

        // Check initial waves spawned
        Assertions.assertNotNull(display.getWaveNumberLabel());
        String expectedWavesSpawned = "Waves spawned: 0";
        Assertions.assertEquals(expectedWavesSpawned, display.getWaveNumberLabel().getText().toString());

        // Check initial max waves when no max waves specified
        Assertions.assertNotNull(display.getMaxWavesLabel());
        String expectedMaxWaves = "Max waves: 1";
        Assertions.assertEquals(expectedMaxWaves, display.getMaxWavesLabel().getText().toString());

        // Check initial delay time
        Assertions.assertNotNull(display.getWaveDelayLabel());
        String expectedDelayTime = "Wave delay: 5s";
        Assertions.assertEquals(expectedDelayTime, display.getWaveDelayLabel().getText().toString());
    }

    @Test
    @DisplayName("Check Labels after some setters and event triggers")
    void checkLabelAfterSetter() {
        // Check enemies left label
        display.setInitialEnemyNum(5);
        wavesManager.setEnemyLeft(2);
        wavesManager.getEvents().trigger("numEnemyLeftChanged");

        Assertions.assertNotNull(display.getEnemyNumLabel());
        String expectedEnemyNum = "Enemies left: 2 / 5";
        Assertions.assertEquals(expectedEnemyNum, display.getEnemyNumLabel().getText().toString());

        // Check updated max waves when no max waves specified
        wavesManager.setMaxWaves(5);

        Assertions.assertNotNull(display.getMaxWavesLabel());
        String expectedMaxWaves = "Max waves: 5";
        Assertions.assertEquals(expectedMaxWaves, display.getMaxWavesLabel().getText().toString());

        // Check updated waves spawned
        wavesManager.setWaveNumber(3);

        Assertions.assertNotNull(display.getWaveNumberLabel());
        String expectedWavesSpawned = "Waves spawned: 3";
        Assertions.assertEquals(expectedWavesSpawned, display.getWaveNumberLabel().getText().toString());
    }

    @Test
    @DisplayName("Test table visibility")
    void checkTableVisibility() {
        // initially invisible after calling addActors()
        assertFalse(display.getTable().isVisible());

        // make visible and assert
        wavesManager.getEvents().trigger("spawnWave");
        assertTrue(display.getTable().isVisible());
    }

}
