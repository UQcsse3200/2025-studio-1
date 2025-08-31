package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * Projectile straight-line movement + lifetime control.
 */
public class EnemyProjectileMovementComponent extends Component {
    private final Vector2 velocity;
    private float life;

    public EnemyProjectileMovementComponent(Vector2 velocity, float lifeSeconds) {
        this.velocity = new Vector2(velocity);
        this.life = lifeSeconds;
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        if (entity == null) return;

        // Apply movement
        entity.setPosition(entity.getPosition().cpy().add(velocity.cpy().scl(dt)));

        // Reduce lifetime
        life -= dt;
        if (life <= 0f) {
            com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                if (entity != null) entity.dispose();
            });
        }
    }
}