package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BossChargeSkillComponentTest {
    /**
     * A simple controllable time source for tests.
     * Most course templates allow customizing delta time via overriding getDeltaTime().
     */
    static class TestTime extends GameTime {
        private float dt = 0f;
        void setDelta(float dt) { this.dt = dt; }
        @Override public float getDeltaTime() { return dt; }
    }

    private TestTime time;

    @BeforeEach
    void setup() {
        time = new TestTime();
        ServiceLocator.registerTimeSource(time);
    }

    @AfterEach
    void teardown() {
        // If your ServiceLocator supports clearing, you can call it here.
        // ServiceLocator.clear();
    }

    @Test
    void create_shouldSetPatrolY_andFirePatrolEvent() {
        // Arrange: a boss entity and a target (target position does not matter here)
        Entity target = new Entity();
        target.setPosition(10f, 5f);

        float patrolY = 7f;

        Entity boss = new Entity();
        List<String> events = new ArrayList<>();
        // Listen for the "enter patrol" event
        boss.getEvents().addListener("boss2:patrol", () -> events.add("patrol"));

        BossChargeSkillComponent comp = new BossChargeSkillComponent(
                target,
                5f,     // triggerRange
                0.2f,   // dwellTime
                0.1f,   // prepareTime
                4f,     // chargeSpeed
                0.2f,   // chargeDuration
                0.15f,  // cooldown
                0f, 10f,// patrolLeftX, patrolRightX
                patrolY,
                1.0f    // patrolSpeed
        );
        boss.addComponent(comp);

        // Component create() is invoked by entity.create()
        boss.create();

        // Assert: Y is forced to patrol line and the patrol event was fired
        assertEquals(patrolY, boss.getPosition().y, 1e-5, "create() should set Y to patrolY");
        assertTrue(events.contains("patrol"), "create() should trigger boss2:patrol");
    }

    @Test
    void fullFlow_patrolToPrepToChargingToReturnToCooldownToPatrol() {
        // Put a target within range
        Entity target = new Entity();
        target.setPosition(2f, 5f);

        float patrolY = 5f;

        Entity boss = new Entity();
        boss.setPosition(0f, 0f); // create() will snap Y to patrolY

        List<String> events = new ArrayList<>();
        boss.getEvents().addListener("boss2:patrol",   () -> events.add("patrol"));
        boss.getEvents().addListener("boss2:prep",     () -> events.add("prep"));
        boss.getEvents().addListener("boss2:charge",   () -> events.add("charge"));
        boss.getEvents().addListener("boss2:return",   () -> events.add("return"));
        boss.getEvents().addListener("boss2:cooldown", () -> events.add("cooldown"));

        // Use short timings so the state machine advances quickly during the test
        float triggerRange   = 100f;
        float dwellTime      = 0.1f;
        float prepareTime    = 0.05f;
        float chargeSpeed    = 6f;
        float chargeDuration = 0.1f;
        float cooldown       = 0.05f;

        BossChargeSkillComponent comp = new BossChargeSkillComponent(
                target,
                triggerRange, dwellTime, prepareTime,
                chargeSpeed, chargeDuration, cooldown,
                -10f, 10f, patrolY, 1.0f
        );
        boss.addComponent(comp);
        boss.create();

        // The component progresses to attack/charge only when crash == true
        comp.setCrash(true);
        comp.setAttack(true);

        // Stay within dwell time on the patrol line to enter PREP
        advance(comp, 0.12f, 0.02f); // total = 0.12s, step = 0.02s
        assertTrue(events.contains("prep"), "Should enter PREP and trigger boss2:prep");

        // After prepare time, it should enter CHARGING
        int beforeChargeIndex = events.size();
        advance(comp, 0.06f, 0.02f); // exceed prepareTime
        assertTrue(events.subList(beforeChargeIndex, events.size()).contains("charge"),
                "Should trigger boss2:charge");

        // During CHARGING the boss position should change
        Vector2 posBefore = new Vector2(boss.getPosition());
        advance(comp, 0.12f, 0.02f); // exceed chargeDuration; should transition to RETURN
        Vector2 posAfter = new Vector2(boss.getPosition());
        assertNotEquals(posBefore, posAfter, "CHARGING should change boss position");
        assertTrue(events.contains("return"), "When charging ends it should trigger boss2:return");

        // After RETURN it should enter COOLDOWN, then back to PATROL
        int beforeCooldown = events.size();
        // RETURN moves toward anchorX, patrolY; give time to reach and enter cooldown
        advance(comp, 0.5f, 0.02f);
        assertTrue(events.subList(beforeCooldown, events.size()).contains("cooldown"),
                "After returning it should trigger boss2:cooldown");

        // After cooldown it should go back to patrol
        int beforePatrol2 = events.size();
        advance(comp, 0.2f, 0.02f);
        assertTrue(events.subList(beforePatrol2, events.size()).contains("patrol"),
                "After cooldown it should trigger boss2:patrol again");
    }

    /** Advance the state machine by repeatedly calling update() with a fixed step. */
    private void advance(BossChargeSkillComponent comp, float total, float step) {
        float t = 0f;
        while (t < total) {
            time.setDelta(step);
            comp.update();
            t += step;
        }
    }
}
