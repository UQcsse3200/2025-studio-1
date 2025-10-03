package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.services.ServiceLocator;

public class FireballAttackComponent extends Component {
    private final Entity target;
    private final float cooldown;
    private final float range;
    private final float speed;
    private final int damage;

    private float timer = 0f;
    private boolean attack = true;

    public FireballAttackComponent(Entity target, float cooldown, float range,
                                   float speed, int damage) {
        this.target = target;
        this.cooldown = cooldown;
        this.range = range;
        this.speed = speed;
        this.damage = damage;
    }

    @Override
    public void update() {
        if (!attack) {
            return;
        }
        timer -= ServiceLocator.getTimeSource().getDeltaTime();
        if (timer > 0) {
            return;
        }
        Vector2 from = entity.getCenterPosition();
        Vector2 to = target.getCenterPosition();
        if (from.dst2(to) <= range * range) {
            shoot(from, to);
            timer = cooldown;
        }
    }

    private void shoot(Vector2 from, Vector2 to) {
        Vector2 velocity = new Vector2(to).sub(from).nor().scl(speed);
        Entity fireball = BossFactory.createFireball(from, velocity);
        com.csse3200.game.areas.GameArea area = ServiceLocator.getGameArea();
        if (area != null) {
            area.spawnEntity(fireball);
        } else {
            ServiceLocator.getEntityService().register(fireball);
        }
    }

    public void setAttack(boolean attack) {
        this.attack = attack;
    }
}