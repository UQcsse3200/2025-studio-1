package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Slimmed smoke tests for Boss2HealthPhaseSwitcher.
 * These tests avoid AnimationRenderComponent to keep them deterministic and resource-free.
 * The goal is to ensure update() is safe when ARC is absent and no events are emitted.
 */
class Boss2HealthPhaseSwitcherTest {

    @Test
    void update_withNoArc_doesNotCrash() {
        // Arrange: entity with stats but without AnimationRenderComponent
        Entity boss = new Entity();
        boss.addComponent(new CombatStatsComponent(1000)); // ensure maxHealth > 0

        Boss2HealthPhaseSwitcher switcher =
                new Boss2HealthPhaseSwitcher(0.5f, 0.3f, "idle", "phase2", "angry");
        boss.addComponent(switcher);

        // Act & Assert: calling update() without ARC should be a no-op and not throw
        assertDoesNotThrow(switcher::update, "update() must be safe when ARC is null");
    }

    @Test
    void update_withStatsOnly_emitsNoPhaseEvents() {
        // Arrange: entity with stats and phase event listeners, still no ARC
        Entity boss = new Entity();
        CombatStatsComponent stats = new CombatStatsComponent(1000);
        boss.addComponent(stats);

        Boss2HealthPhaseSwitcher switcher =
                new Boss2HealthPhaseSwitcher(0.5f, 0.3f, "idle", "phase2", "angry");
        boss.addComponent(switcher);

        List<String> events = new ArrayList<>();
        boss.getEvents().addListener("boss2:phase2", () -> events.add("phase2"));
        boss.getEvents().addListener("boss2:angry",  () -> events.add("angry"));

        // Drop HP well below thresholds; without ARC, switcher should do nothing and emit nothing
        stats.setHealth(200);

        // Act
        assertDoesNotThrow(switcher::update);
        // Assert
        assertTrue(events.isEmpty(), "Without ARC no phase events should be emitted");
    }
}


