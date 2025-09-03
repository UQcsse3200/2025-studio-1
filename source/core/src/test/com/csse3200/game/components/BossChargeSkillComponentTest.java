package com.csse3200.game.components; // 注意：和你的目录一致（components）

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.BossChargeSkillComponent; // 被测类在 enemy 包
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class BossChargeSkillComponentTest {

    static class TestGameTime extends GameTime {
        private float dt = 0f;
        public void tick(float dt) { this.dt = dt; }
        @Override public float getDeltaTime() { return dt; }
    }

    private TestGameTime time;

    @BeforeEach
    void setUp() {
        time = new TestGameTime();
        ServiceLocator.registerTimeSource(time);
    }

    @Test
    void triggersChargeAfterDwellAndStopsAfterDuration() {

        Entity target = new Entity();
        target.setPosition(new Vector2(5f, 0f));

        Entity boss = new Entity();
        boss.setPosition(new Vector2(0f, 0f));


        float triggerRange   = 10f;
        float dwellTime      = 1.0f;
        float prepareTime    = 0.0f;
        float chargeSpeed    = 8.0f;
        float chargeDuration = 0.6f;
        float cooldown       = 0.2f;

        BossChargeSkillComponent skill = new BossChargeSkillComponent(
                target, triggerRange, dwellTime, prepareTime, chargeSpeed, chargeDuration, cooldown);
        boss.addComponent(skill);
        boss.create();

        float dt = 0.2f;
        float t  = 0f;
        Vector2 start = boss.getPosition().cpy();

        while (t + dt < dwellTime) {
            time.tick(dt);
            skill.update();
            t += dt;
        }
        assertEquals(start.x, boss.getPosition().x, 1e-4, "\n" +
                "Should not move before dwellTime");
        assertEquals(start.y, boss.getPosition().y, 1e-4, "\n" +
                "Should not move before dwellTime");

        float runMore = chargeDuration + cooldown + dt * 3f;
        float spent = 0f;
        while (spent < runMore) {
            time.tick(dt);
            skill.update();
            spent += dt;
        }
        Vector2 afterCycle = boss.getPosition().cpy();


        assertTrue(afterCycle.x > start.x + 0.1f, "\n" +
                "Should move noticeably after triggering");


        for (int i = 0; i < 5; i++) {
            time.tick(dt);
            skill.update();
        }
        Vector2 afterStable = boss.getPosition().cpy();
        assertEquals(afterCycle.x, afterStable.x, 1e-4, "\n" +
                "After the collision, the position should be stable (x no longer changes)");
        assertEquals(afterCycle.y, afterStable.y, 1e-4, "\n" +
                "After the collision, the position should be stable (x no longer changes)");
    }

    @Test
    void doesNotTriggerIfTargetLeavesRangeEarly() {
        Entity target = new Entity();
        Entity boss = new Entity();
        boss.setPosition(new Vector2(0f, 0f));

        float triggerRange   = 6f;
        float dwellTime      = 1.0f;
        float prepareTime    = 0f;
        float chargeSpeed    = 8f;
        float chargeDuration = 0.6f;
        float cooldown       = 0f;

        BossChargeSkillComponent skill = new BossChargeSkillComponent(
                target, triggerRange, dwellTime, prepareTime, chargeSpeed, chargeDuration, cooldown);
        boss.addComponent(skill);
        boss.create();

        float dt = 0.2f;


        target.setPosition(new Vector2(3f, 0f));
        for (int i = 0; i < 4; i++) { // 0.8s < dwellTime
            time.tick(dt);
            skill.update();
        }
        Vector2 beforeLeave = boss.getPosition().cpy();

        // 离开范围，继续运行一段时间
        target.setPosition(new Vector2(100f, 0f));
        for (int i = 0; i < 10; i++) {
            time.tick(dt);
            skill.update();
        }

        Vector2 after = boss.getPosition().cpy();
        assertEquals(beforeLeave.x, after.x, 1e-4, "The collision should not be triggered after leaving the range");
        assertEquals(beforeLeave.y, after.y, 1e-4, "The collision should not be triggered after leaving the range");
    }

    @Test
    void canTriggerAgainAfterCooldown() {
        Entity target = new Entity();
        target.setPosition(new Vector2(5f, 0f));

        Entity boss = new Entity();
        boss.setPosition(new Vector2(0f, 0f));

        float triggerRange   = 10f;
        float dwellTime      = 0.4f;
        float prepareTime    = 0f;
        float chargeSpeed    = 6f;
        float chargeDuration = 0.5f;
        float cooldown       = 0.2f;

        BossChargeSkillComponent skill = new BossChargeSkillComponent(
                target, triggerRange, dwellTime, prepareTime, chargeSpeed, chargeDuration, cooldown);
        boss.addComponent(skill);
        boss.create();

        float dt = 0.1f;

        //dwell + charge + cooldown
        float run = dwellTime + chargeDuration + cooldown + dt * 3f;
        float t = 0f;
        while (t < run) {
            time.tick(dt);
            skill.update();
            t += dt;
        }
        float x1 = boss.getPosition().x;


        t = 0f;
        while (t < run) {
            time.tick(dt);
            skill.update();
            t += dt;
        }
        float x2 = boss.getPosition().x;

        assertTrue(x2 > x1 + 0.1f, "After the cooldown is over, trigger it again and move further");
    }
}
