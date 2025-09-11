package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PlayerStatsDisplayTest {

    // Class to stop addActors();
    static class TestPlayerStatsDisplay extends PlayerStatsDisplay {
        @Override
        public void create() {
            // Skip addActors();
        }
    }

    @Test
    void testUpdateHealthUI() {
        Drawable dummyDrawable = mock(Drawable.class);

        ProgressBarStyle style = new ProgressBarStyle();
        style.background = dummyDrawable;
        style.knob = dummyDrawable;
        ProgressBar bar = new ProgressBar(0, 100, 1, false, style);

        PlayerStatsDisplay display = new TestPlayerStatsDisplay();
        display.setHealthBar(bar);

        display.updatePlayerHealthUI(50);
        assertEquals(50, bar.getValue(), 0.01);

        display.updatePlayerHealthUI(90);
        assertEquals(90, bar.getValue(), 0.01);
    }

    @Test
    void testUpdateProcessorUI() {
        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        Label label = new Label("", style);

        PlayerStatsDisplay display = new TestPlayerStatsDisplay();
        display.setProcessorLabel(label);

        display.updatePlayerProcessorUI(5);
        assertEquals("Processor: 5", label.getText().toString());

        display.updatePlayerProcessorUI(123);
        assertEquals("Processor: 123", label.getText().toString());
    }
}
