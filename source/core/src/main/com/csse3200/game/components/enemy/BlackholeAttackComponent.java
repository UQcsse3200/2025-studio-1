package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

public class BlackholeAttackComponent extends Component {
    private final Entity target;
    private final float radius;
    private final float lifeTime;

    private float timer = 0f;

    public BlackholeAttackComponent(Entity target, float radius, float lifeTime) {
        this.target = target;
        this.radius = radius;
        this.lifeTime = lifeTime;
    }

    @Override
    public void update() {
        if (target == null || entity == null) return;

        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        timer += dt;
        if (timer >= lifeTime) {
            Gdx.app.postRunnable(() -> {
                if (entity != null) {
                    entity.dispose();
                }
            });
        }
        Vector2 hole = entity.getCenterPosition();
        Vector2 player = target.getCenterPosition();
        if (hole.dst2(player) <= radius * radius) {
            target.setPosition(hole);
        }
    }
}
