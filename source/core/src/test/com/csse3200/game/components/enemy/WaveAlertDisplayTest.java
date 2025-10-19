package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WaveAlertDisplayTest {
    private WaveAlertDisplay display;

    @BeforeEach
    void setup() {
        // Mock Stage to avoid LibGDX native calls
        Stage stage = mock(Stage.class);

        // Mock RenderService to return the Stage
        RenderService mockRenderService = mock(RenderService.class);
        when(mockRenderService.getStage()).thenReturn(stage);
        // Register the mocked RenderService in ServiceLocator
        ServiceLocator.registerRenderService(mockRenderService);

        Gdx.gl = mock(GL20.class);
        Gdx.gl20 = mock(GL20.class);

        Gdx.graphics = mock(Graphics.class);
        when(Gdx.graphics.getHeight()).thenReturn(720);
        when(Gdx.graphics.getWidth()).thenReturn(1280);

        display = new WaveAlertDisplay();
    }

    @AfterEach
    void dispose() {
        // Clear services registered in ServiceLocator so next test starts fresh
        ServiceLocator.clear();
    }

    @Test
    @DisplayName("Check initial labels' texts at class creation")
    void checkLabelTexts() {
        // Check number of enemies left
        Assertions.assertNotNull(display.getAlertLabel1());
        String expectedEnemyNum = "> SYSTEM ALERT:";
        Assertions.assertEquals(expectedEnemyNum, display.getAlertLabel1().getText().toString());

        // Check initial waves spawned
        Assertions.assertNotNull(display.getAlertLabel2());
        String expectedWavesSpawned = "ENEMY WAVE DETECTED..";
        Assertions.assertEquals(expectedWavesSpawned, display.getAlertLabel2().getText().toString());
    }

    @Test
    @DisplayName("Check table visibility")
    void checkTableVisibility() {
        // initially invisible after calling addActors()
        assertFalse(display.getTable().isVisible());

        // make visible and assert
        display.display();
        assertTrue(display.getTable().isVisible());
    }
}
