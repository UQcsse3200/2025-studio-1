package com.csse3200.game.components.enemy;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BossChargeSkillComponentExtraTest {

    /**
     * Controllable time source for deterministic updates.
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
        // If you have a ServiceLocator.clear(), you can call it here.
        // ServiceLocator.clear();
    }

    @Test
    void update_doesNothing_whenAttackDisabled() {
        // Arrange: boss and target on the same spot
        Entity target = new Entity(); target.setPosition(0f, 0f);
        Entity boss   = new Entity(); boss.setPosition(0f, 0f);

        // Short timings (not really used since attack=false)
        BossChargeSkillComponent comp = new BossChargeSkillComponent(
                target,
                5f,      // triggerRange
                0.05f,   // dwellTime
                0.05f,   // prepareTime
                5f,      // chargeSpeed
                0.1f,    // chargeDuration
                0.05f,   // cooldown
                -1f, 1f, // patrolLeftX, patrolRightX
                0f,      // patrolY
                1f       // patrolSpeed
        );
        boss.addComponent(comp);
        boss.create();

        // Disable attack path (early return branch)
        comp.setAttack(false);

        // Capture position before update
        float x0 = boss.getPosition().x;
        float y0 = boss.getPosition().y;

        // Act
        step(comp, 0.2f, 0.02f);

        // Assert: position unchanged, i.e., no state/behavior executed
        assertEquals(x0, boss.getPosition().x, 1e-5, "Position X should not change when attack is disabled");
        assertEquals(y0, boss.getPosition().y, 1e-5, "Position Y should not change when attack is disabled");
    }

    @Test
    void prep_isCancelled_whenCrashBecomesFalse() {
        // Arrange: put target in range so PREP can be reached
        Entity target = new Entity(); target.setPosition(0f, 0f);
        Entity boss   = new Entity(); boss.setPosition(0f, 0f);

        var events = new ArrayList<String>();
        boss.getEvents().addListener("boss2:patrol", () -> events.add("patrol"));
        boss.getEvents().addListener("boss2:prep",   () -> events.add("prep"));

        BossChargeSkillComponent comp = new BossChargeSkillComponent(
                target,
                100f,    // triggerRange (big so always in range)
                0.05f,   // dwellTime (quick to reach PREP)
                0.1f,    // prepareTime
                5f,      // chargeSpeed
                0.1f,    // chargeDuration
                0.05f,   // cooldown
                -1f, 1f, // patrolLeftX, patrolRightX
                0f,      // patrolY
                1f       // patrolSpeed
        );
        boss.addComponent(comp);
        boss.create();

        // Enable attack path and allow entering PREP
        comp.setAttack(true);
        comp.setCrash(true);

        // Wait long enough to enter PREP
        step(comp, 0.08f, 0.02f);
        assertTrue(events.contains("prep"), "Should enter PREP and fire boss2:prep");

        // Cancel crash while in/after PREP; component should revert to PATROL
        comp.setCrash(false);

        // Give time for the component to handle the cancel and switch back to PATROL
        int before = events.size();
        step(comp, 0.2f, 0.02f);

        assertTrue(
                events.subList(before, events.size()).contains("patrol"),
                "Cancelling crash should send boss back to PATROL"
        );
    }

    /** Helper: advance time and repeatedly call update() */
    private void step(BossChargeSkillComponent comp, float total, float dt) {
        float t = 0f;
        while (t < total) {
            time.setDelta(dt);
            comp.update();
            t += dt;
        }
    }
}

