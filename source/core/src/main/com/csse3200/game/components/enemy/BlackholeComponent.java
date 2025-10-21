package com.csse3200.game.components.enemy;

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
    private boolean attack = true;

    public BlackholeComponent(Entity target, float cooldown, float range) {
        this.target = target;
        this.cooldown = cooldown;
        this.range = range;
    }

    @Override
    public void update() {
        if (!attack) {
            return;
        }
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
        float offsetX = 1f;
        Vector2 spawnPos = new Vector2(playerPos.x + offsetX, playerPos.y - offsetY);
        Entity blackhole = BossFactory.createBlackhole(spawnPos, target);
        ServiceLocator.getEntityService().register(blackhole);
    }

    public void setAttack(boolean attack) {
        this.attack = attack;
    }
}