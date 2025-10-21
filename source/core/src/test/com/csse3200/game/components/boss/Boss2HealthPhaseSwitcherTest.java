package com.csse3200.game.components.boss;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.rendering.AnimationRenderComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Boss2HealthPhaseSwitcher}.
 *
 * Covers:
 *  - Initial animation selection in create()
 *  - Stage switching by HP ratio (idle / phase2 / angry)
 *  - Safe no-op when an animation name is not in the atlas
 *  - One-shot events on first entry into phase2/angry
 *  - No duplicate events on repeated updates
 *  - Lazy component re-fetch in update() when stats/arc are initially null
 */
class Boss2HealthPhaseSwitcherTest {

    private static final String IDLE = "idle";
    private static final String P2 = "phase2";
    private static final String ANGRY = "angry";

    /** Attach the component to a mocked Entity without needing the full ECS. */
    private static void attachToEntity(Object component, Entity entity) {
        try {
            Method m = component.getClass().getSuperclass().getDeclaredMethod("setEntity", Entity.class);
            m.setAccessible(true);
            m.invoke(component, entity);
        } catch (Exception ignored1) {
            try {
                Field f = component.getClass().getSuperclass().getDeclaredField("entity");
                f.setAccessible(true);
                f.set(component, entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach component to entity by reflection.", e);
            }
        }
    }

    private Entity entity;
    private CombatStatsComponent stats;
    private AnimationRenderComponent arc;
    private EventHandler events;

    @BeforeEach
    void setup() {
        entity = mock(Entity.class);
        stats = mock(CombatStatsComponent.class);
        arc = mock(AnimationRenderComponent.class);
        events = mock(EventHandler.class);

        when(entity.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(arc);
        when(entity.getEvents()).thenReturn(events);
    }

    @Test
    void create_playsAngry_whenHpBelowAngry_andTriggersEventOnce() {
        // thresholds: phase2=0.5, angry=0.3 ; hp/max = 25/100 = 0.25 -> angry
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(25);
        when(arc.hasAnimation(IDLE)).thenReturn(true);
        when(arc.hasAnimation(P2)).thenReturn(true);
        when(arc.hasAnimation(ANGRY)).thenReturn(true);

        Boss2HealthPhaseSwitcher sw = new Boss2HealthPhaseSwitcher(0.5f, 0.3f, IDLE, P2, ANGRY);
        attachToEntity(sw, entity);

        // create() should immediately evaluate and play once
        assertDoesNotThrow(sw::create);

        verify(arc).startAnimation(ANGRY);
        verify(events, times(1)).trigger("boss2:angry");

        // Recreate a frame: update() should NOT re-trigger angry if still angry
        sw.update();
        verify(events, times(1)).trigger("boss2:angry"); // still exactly once
    }

    @Test
    void create_fallsBackToPhase2_ifAngryMissing_butRatioIsAngry() {
        // hp/max = 25/100 = 0.25 (angry by ratio), but angry animation missing -> fall back to phase2
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(25);
        when(arc.hasAnimation(ANGRY)).thenReturn(false);
        when(arc.hasAnimation(P2)).thenReturn(true);
        when(arc.hasAnimation(IDLE)).thenReturn(true);

        Boss2HealthPhaseSwitcher sw = new Boss2HealthPhaseSwitcher(0.5f, 0.3f, IDLE, P2, ANGRY);
        attachToEntity(sw, entity);

        sw.create();

        verify(arc, never()).startAnimation(ANGRY);
        verify(arc).startAnimation(P2);
        verify(events, times(1)).trigger("boss2:phase2");
        verify(events, never()).trigger("boss2:angry");
    }

    @Test
    void idle_selected_whenAbovePhase2_or_whenOnlyIdleExists() {
        // hp/max = 80/100 = 0.8 -> idle
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(80);

        // Simulate idle missing from atlas (safe skip)
        when(arc.hasAnimation(IDLE)).thenReturn(false);
        when(arc.hasAnimation(P2)).thenReturn(true);
        when(arc.hasAnimation(ANGRY)).thenReturn(true);

        Boss2HealthPhaseSwitcher sw = new Boss2HealthPhaseSwitcher(0.5f, 0.3f, IDLE, P2, ANGRY);
        attachToEntity(sw, entity);

        sw.create();

        // Since idle animation is not in atlas, no startAnimation should be called
        verify(arc, never()).startAnimation(anyString());
        verify(events, never()).trigger("boss2:phase2");
        verify(events, never()).trigger("boss2:angry");
    }

    @Test
    void transitions_phase2_then_angry_and_eventsAreOneShot() {
        // Start above phase2: idle (hp 60/100 = 0.6)
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(60);
        when(arc.hasAnimation(IDLE)).thenReturn(true);
        when(arc.hasAnimation(P2)).thenReturn(true);
        when(arc.hasAnimation(ANGRY)).thenReturn(true);

        Boss2HealthPhaseSwitcher sw = new Boss2HealthPhaseSwitcher(0.5f, 0.3f, IDLE, P2, ANGRY);
        attachToEntity(sw, entity);
        sw.create();

        // Drop to phase2 (hp 40 -> 0.4)
        when(stats.getHealth()).thenReturn(40);
        sw.update();
        verify(arc).startAnimation(P2);
        verify(events, times(1)).trigger("boss2:phase2");

        // Stay in phase2 (hp 35 -> still 0.35, no duplicate event)
        when(stats.getHealth()).thenReturn(35);
        sw.update();
        verify(events, times(1)).trigger("boss2:phase2");

        // Drop to angry (hp 20 -> 0.2)
        when(stats.getHealth()).thenReturn(20);
        sw.update();
        verify(arc).startAnimation(ANGRY);
        verify(events, times(1)).trigger("boss2:angry");
    }

    @Test
    void update_lazyFetchesComponents_whenInitiallyNull() throws Exception {
        // Build a fresh switcher but make entity initially return null components in update()
        Boss2HealthPhaseSwitcher sw = new Boss2HealthPhaseSwitcher(0.5f, 0.3f, IDLE, P2, ANGRY);
        attachToEntity(sw, entity);

        // Ensure create() has valid components to avoid NPE inside playForCurrentHp()
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(100);
        when(arc.hasAnimation(IDLE)).thenReturn(true);
        sw.create(); // sets internal stats/arc and evaluates once (idle)

        // Now simulate that on the next update(), internal fields are null and must be re-fetched
        // (We can't access the private fields directly, but we can force getComponent() behavior.)
        // First update: return null to hit the early-return branch
        when(entity.getComponent(CombatStatsComponent.class)).thenReturn(null);
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(null);

        sw.update(); // should return early (no NPE)

        // Second update: components become available; drop hp to phase2 to verify normal path resumes
        when(entity.getComponent(CombatStatsComponent.class)).thenReturn(stats);
        when(entity.getComponent(AnimationRenderComponent.class)).thenReturn(arc);
        when(stats.getHealth()).thenReturn(45); // ratio 0.45 -> phase2
        when(arc.hasAnimation(P2)).thenReturn(true);

        sw.update();

        verify(arc).startAnimation(P2);
        verify(events, times(1)).trigger("boss2:phase2");
    }
}



