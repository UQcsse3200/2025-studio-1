package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.services.ServiceLocator;

public class BlackholeComponent extends Component {
    private final Entity target;
    private final float cooldown;
    private final float range;

    private float timer = 0f;

    public BlackholeComponent(Entity target, float cooldown, float range) {
        this.target = target;
        this.cooldown = cooldown;
        this.range = range;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer -= dt;
        if (timer > 0) return;

        Vector2 from = entity.getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (from.dst2(to) <= range * range) {
            spawnBlackholeNearPlayer(to);
            timer = cooldown;
        }
    }

    private void spawnBlackholeNearPlayer(Vector2 playerPos) {
        float offsetY = 2.5f;
        int dir = MathUtils.randomBoolean() ? 1 : -1;
        float offsetX = dir * MathUtils.random(1f, 3f);
        Vector2 spawnPos = new Vector2(playerPos.x + offsetX, playerPos.y - offsetY);
        Entity blackhole = BossFactory.createBlackhole(spawnPos, target);
        ServiceLocator.getEntityService().register(blackhole);
    }
}
