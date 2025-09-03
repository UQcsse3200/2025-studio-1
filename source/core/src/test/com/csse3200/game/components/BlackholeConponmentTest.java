package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.BlackholeAttackComponent;
import com.csse3200.game.components.enemy.BlackholeComponent;
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
class BlackholeConponmentTest {

    static class TestGameTime extends GameTime {
        private float dt = 0f;
        public void tick(float dt) { this.dt = dt; }
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
    void doesNotSpawnWhenOutOfRange() {
        Entity boss = new Entity();
        boss.setPosition(new Vector2(0f, 0f));
        Entity player = new Entity();
        player.setPosition(new Vector2(100f, 0f));  // 远离射程
        float range = 10f;
        float cooldown = 1.0f;
        BlackholeComponent comp = new BlackholeComponent(player, cooldown, range);
        boss.addComponent(comp);
        boss.create();
        for (int i = 0; i < 10; i++) {
            time.tick(0.16f);
            comp.update();
        }
        assertEquals(0, entities.registered, "Out of range");
    }
    @Test
    void doesNotPullTargetWhenOutsideRadius() {
        Entity player = new Entity();
        player.setPosition(new Vector2(10f, 0f));
        Entity hole = new Entity();
        hole.setPosition(new Vector2(0f, 0f));
        float radius = 2f;
        float lifeTime = 5f;
        BlackholeAttackComponent attack = new BlackholeAttackComponent(player, radius, lifeTime);
        hole.addComponent(attack);
        hole.create();
        Vector2 before = player.getPosition().cpy();
        time.tick(0.1f);
        attack.update();
        assertEquals(before.x, player.getPosition().x, 1e-5);
        assertEquals(before.y, player.getPosition().y, 1e-5);
    }
}

