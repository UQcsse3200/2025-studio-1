package com.csse3200.game.components.boss;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.*;

/**
 * Tests cover behavier of Boss3HealthPhaseSwitcher.
 */
public class Boss3HealthPhaseSwitcherTest {

    private MockedStatic<ServiceLocator> serviceLocatorStatic;

    @BeforeEach
    void setup() {
        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        ResourceService mockRs = mock(ResourceService.class);
        when(ServiceLocator.getResourceService()).thenReturn(mockRs);
        when(mockRs.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(new TextureAtlas());
    }

    @AfterEach
    void tearDown() {
        if (serviceLocatorStatic != null) {
            serviceLocatorStatic.close();
        }
    }

    private Boss3HealthPhaseSwitcher buildSwitcher() {
        return new Boss3HealthPhaseSwitcher("images/Boss3_Attacks.atlas", 0.1f)
                .addPhase(0.50f, "crack50")
                .addPhase(0.40f, "crack40")
                .addPhase(0.25f, "crack25");
    }

    @Test
    void create_triggersCorrectPhaseWhenSpawningBelow50() {
        // Entity with scale set to avoid any surprises
        Entity boss = new Entity();
        boss.setScale(new Vector2(2f, 2f));

        // Mock combat stats at 45/100 -> ratio = 0.45 -> should trigger "crack50"
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(45);
        boss.addComponent(stats);

        // Pre-attach a mocked AnimationRenderComponent so the switcher will use it
        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        // Make it "contain" the animation name we expect to be started
        when(arc.hasAnimation("crack50")).thenReturn(true);
        boss.addComponent(arc);

        // a TextureRenderComponent may exist, since ARC is present,
        // the switcher won't create a new ARC or disable TRC in this path.
        TextureRenderComponent trc = mock(TextureRenderComponent.class);
        boss.addComponent(trc);

        Boss3HealthPhaseSwitcher switcher = buildSwitcher();
        boss.addComponent(switcher);

        // Act
        switcher.create();

        // Assert: correct animation started once
        verify(arc, times(1)).startAnimation("crack50");
        // ensure that we didn't accidentally start others
        verify(arc, never()).startAnimation("crack40");
        verify(arc, never()).startAnimation("crack25");
    }

    @Test
    void createAndUpdate_doNothingWhenNoCombatStats() {
        Entity boss = new Entity();
        boss.setScale(new Vector2(2f, 2f));

        // pre-attach mocked ARC just to observe calls
        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        boss.addComponent(arc);

        Boss3HealthPhaseSwitcher switcher = buildSwitcher();
        boss.addComponent(switcher);

        // Act
        switcher.create();
        switcher.update();

        // Assert: no animation started since no CombatStatsComponent => ratio assumed 1.0
        verify(arc, never()).startAnimation(anyString());
    }

    @Test
    void update_triggersPhasesAsHealthDropsAcrossThresholds() {
        Entity boss = new Entity();
        boss.setScale(new Vector2(2f, 2f));

        // Health sequence:
        // create(): 100/100 -> no trigger
        // 1st update: 39/100 -> 0.39 <= 0.50 -> triggers "crack50"
        // 2nd update: 24/100 -> 0.24 <= 0.40 -> triggers "crack40" (breaks before 0.25)
        // 3rd update: 24/100 -> still 0.24 -> now advances to "crack25"
        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getMaxHealth()).thenReturn(100);
        when(stats.getHealth()).thenReturn(100, 39, 24, 24);
        boss.addComponent(stats);

        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        when(arc.hasAnimation("crack50")).thenReturn(true);
        when(arc.hasAnimation("crack40")).thenReturn(true);
        when(arc.hasAnimation("crack25")).thenReturn(true);
        boss.addComponent(arc);

        Boss3HealthPhaseSwitcher switcher = new Boss3HealthPhaseSwitcher("images/Boss3_Attacks.atlas", 0.1f)
                .addPhase(0.50f, "crack50")
                .addPhase(0.40f, "crack40")
                .addPhase(0.25f, "crack25");
        boss.addComponent(switcher);

        switcher.create();   // ratio = 1.0 -> no trigger
        switcher.update();   // -> "crack50"
        switcher.update();   // -> "crack40"
        switcher.update();   // -> "crack25"

        verify(arc, times(1)).startAnimation("crack50");
        verify(arc, times(1)).startAnimation("crack40");
        verify(arc, times(1)).startAnimation("crack25");
    }

    @Test
    void update_doesNotRetriggerSamePhaseWhenStayingWithinThreshold() {
        Entity boss = new Entity();
        boss.setScale(new Vector2(2f, 2f));

        CombatStatsComponent stats = mock(CombatStatsComponent.class);
        when(stats.getMaxHealth()).thenReturn(100);
        // Sequence: 100 -> 48 -> 47 -> 46 (all stay <= 0.5 after first drop)
        when(stats.getHealth()).thenReturn(100, 48, 47, 46);
        boss.addComponent(stats);

        AnimationRenderComponent arc = mock(AnimationRenderComponent.class);
        when(arc.hasAnimation("crack50")).thenReturn(true);
        boss.addComponent(arc);

        Boss3HealthPhaseSwitcher switcher = buildSwitcher();
        boss.addComponent(switcher);

        // Act
        switcher.create();  // 100 -> no trigger
        switcher.update();  // 48 -> trigger "crack50" once
        switcher.update();  // 47 -> same phase, should NOT retrigger
        switcher.update();  // 46 -> same phase, should NOT retrigger

        // Assert
        verify(arc, times(1)).startAnimation("crack50");
        verify(arc, never()).startAnimation("crack40");
        verify(arc, never()).startAnimation("crack25");
    }
}
