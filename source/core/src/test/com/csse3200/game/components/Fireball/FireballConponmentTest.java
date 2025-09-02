package com.csse3200.game.components.Fireball;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.FireballAttackComponment;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class FireballConponmentTest {
    static class TestGameTime extends GameTime {
        private float dt = 0f;
        void tick() { this.dt = (float) 0.1; }
        @Override public float getDeltaTime() { return dt; }
    }
    static class CountingEntityService extends EntityService {
        public int registered = 0;
        @Override
        public void register(Entity entity) {
            registered++;
        }
    }
    private TestGameTime time;
    private CountingEntityService entities;
    @BeforeEach
    void setUp() {
        time = new TestGameTime();
        ServiceLocator.registerTimeSource(time);
        entities = new CountingEntityService();
        ServiceLocator.registerEntityService(entities);
    }
    @Test
    void doesNotShootWhenTargetOutOfRange() {
        Entity shooter = new Entity();
        shooter.setPosition(new Vector2(0f, 0f));
        Entity target = new Entity();
        target.setPosition(new Vector2(100f, 0f)); // 射程外
        FireballAttackComponment comp =
                new FireballAttackComponment(target, 1f, 10f, 5f, 1);
        shooter.addComponent(comp);
        shooter.create();
        time.tick();
        comp.update();
        assertEquals(0, entities.registered, "out of range");
    }
}
