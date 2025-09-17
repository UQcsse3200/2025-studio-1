package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EnemyHealthDisplayTest {
    @BeforeEach
    void setup() {
        // Mock Stage to avoid LibGDX native calls
        Stage mockStage = mock(Stage.class);
        // Mock RenderService to return the Stage
        RenderService mockRenderService = mock(RenderService.class);
        when(mockRenderService.getStage()).thenReturn(mockStage);
        // Register the mocked RenderService in ServiceLocator
        ServiceLocator.registerRenderService(mockRenderService);
    }

    @Test
    void testHealthDisplayLogicRuns() {
        // Create an enemy with 10 health
        Entity enemy = new Entity().addComponent(new CombatStatsComponent(10));
        // Create EnemyHealthDisplay and override create() to use a mocked ProgressBar
        EnemyHealthDisplay display = new EnemyHealthDisplay(0.5f) {
            @Override
            public void create() {
                maxHealth = entity.getComponent(CombatStatsComponent.class).getMaxHealth();
                currentHealth = maxHealth;
                // Use a mocked ProgressBar
                healthBar = mock(ProgressBar.class);
                // Add actor to mocked stage
                stage = ServiceLocator.getRenderService().getStage();
                stage.addActor(healthBar);
                // Listen to health updates
                entity.getEvents().addListener("updateHealth", this::updateEnemyHealthUI);
            }
        };

        enemy.addComponent(display);
        display.create();
        // Ensure maxHealth is correct
        assertEquals(10, display.getMaxHealth());
        assertEquals(10, display.getCurrentHealth());
        // Simulate taking damage
        enemy.getComponent(CombatStatsComponent.class).takeDamage(4);
        enemy.getEvents().trigger("updateHealth", 6);
        // Check that currentHealth updated correctly
        assertEquals(6, display.getCurrentHealth());
    }

    private EnemyHealthDisplay createTestDisplay() {
        return new EnemyHealthDisplay() {
            @Override
            public void create() {
                maxHealth = entity.getComponent(CombatStatsComponent.class).getMaxHealth();
                currentHealth = maxHealth;
                healthBar = mock(ProgressBar.class);
                stage = ServiceLocator.getRenderService().getStage();
                stage.addActor(healthBar);
            }
        };
    }

    @Test
    void testUpdateRunsWithoutCrashing() {
        Entity enemy = new Entity().addComponent(new CombatStatsComponent(10));
        EnemyHealthDisplay display = createTestDisplay();
        enemy.addComponent(display);
        display.create();
        // Call update() and assert it does not throw when enemy change position
        enemy.setPosition(1, 2);
        assertDoesNotThrow(display::update);
    }

    @Test
    public void testUpdateWhenEntityNull() {
        EnemyHealthDisplay display = createTestDisplay();
        display.update();  // will print log message and return without error
    }

    @Test
    public void testDisposeRemovesHealthBar() throws Exception {
        Entity enemy = new Entity().addComponent(new CombatStatsComponent(10));
        EnemyHealthDisplay display = createTestDisplay();
        enemy.addComponent(display);
        display.create();

        ProgressBar mockHealthBar = display.healthBar;
        display.dispose();
        verify(mockHealthBar).remove(); // ensure remove() called
    }
}
