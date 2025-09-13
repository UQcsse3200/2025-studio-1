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
    private boolean disposed = false;

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

        // 到寿命时销毁
        if (!disposed && timer >= lifeTime) {
            disposed = true;
            Gdx.app.postRunnable(() -> {
                if (entity != null) {
                    entity.dispose();
                }
            });
            return;
        }
        Vector2 holeCenter = entity.getCenterPosition();
        Vector2 playerCenter = target.getCenterPosition();
        if (holeCenter.dst2(playerCenter) <= radius * radius) {
            float pullFactor = 0.07f;
            Vector2 newCenter = playerCenter.cpy().lerp(holeCenter, pullFactor);
            Vector2 delta = newCenter.sub(playerCenter);
            Vector2 curPos = target.getPosition();
            target.setPosition(curPos.x + delta.x, curPos.y + delta.y);
        }
    }
}
