package com.csse3200.game.components.player;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void testUpdateAmmoUI() {


        Label.LabelStyle style = new Label.LabelStyle();
        style.font = new BitmapFont();
        Label label = new Label("", style);

        PlayerStatsDisplay display = new TestPlayerStatsDisplay();
        display.setAmmoLabel(label);

        Entity player = new Entity();
        player.addComponent(display);

        player.addComponent(new AmmoStatsComponent(1000));
        player.addComponent(new InventoryComponent(5));

        display.updateAmmoUI();
        assertEquals("Ammo: 1000", label.getText().toString());


    }
}
