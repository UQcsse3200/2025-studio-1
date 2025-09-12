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
        EnemyHealthDisplay display = new EnemyHealthDisplay() {
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

        // Initialize component
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

    @Test
    void testUpdateRunsWithoutCrashing() {
        Entity enemy = new Entity().addComponent(new CombatStatsComponent(10));

        EnemyHealthDisplay display = new EnemyHealthDisplay() {
            @Override
            public void create() {
                maxHealth = entity.getComponent(CombatStatsComponent.class).getMaxHealth();
                currentHealth = maxHealth;
                healthBar = mock(ProgressBar.class);
                stage = ServiceLocator.getRenderService().getStage();
                stage.addActor(healthBar);
            }
        };

        enemy.addComponent(display);
        display.create();

        // Set a position for the entity
        enemy.setPosition(1, 2);

        // Call update() and assert it does not throw
        assertDoesNotThrow(display::update);
    }
}
