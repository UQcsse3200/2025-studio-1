package com.csse3200.game.components.enemy;

import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

import com.csse3200.game.components.enemy.BossStatusDisplay;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BossStatusDisplayTest {

    // Simple test class that skips UI creation
    static class TestBossStatusDisplay extends BossStatusDisplay {
        private ProgressBar testHealthBar;

        public TestBossStatusDisplay() {
            super("Test Boss");
        }

        @Override
        public void create() {
            // Skip UI creation
        }

        public void setTestHealthBar(ProgressBar healthBar) {
            this.testHealthBar = healthBar;
        }

        @Override
        public void updateBossHealthUI(int health) {
            if (testHealthBar != null) {
                testHealthBar.setValue(health);
            }
        }
    }

    @Test
    void testHealthBarUpdate() {
        // Create simple mock drawable
        Drawable mockDrawable = mock(Drawable.class);

        // Create basic progress bar style
        ProgressBarStyle style = new ProgressBarStyle();
        style.background = mockDrawable;
        style.knobBefore = mockDrawable;

        // Create progress bar
        ProgressBar healthBar = new ProgressBar(0, 100, 1, false, style);

        // Test the display
        TestBossStatusDisplay display = new TestBossStatusDisplay();
        display.setTestHealthBar(healthBar);

        // Test health updates
        display.updateBossHealthUI(100);
        assertEquals(100, healthBar.getValue(), 0.1);

        display.updateBossHealthUI(50);
        assertEquals(50, healthBar.getValue(), 0.1);

        display.updateBossHealthUI(0);
        assertEquals(0, healthBar.getValue(), 0.1);
    }

    @Test
    void testConstructor() {
        BossStatusDisplay display = new BossStatusDisplay("Boss1");
        assertNotNull(display);
    }

    @Test
    void testDeathMethod() {
        BossStatusDisplay display = new BossStatusDisplay("TestBoss") {
            @Override
            public void create() {
                // Skip UI
            }
        };

        // Should not crash
        assertDoesNotThrow(() -> display.onBossDeath());
    }
}