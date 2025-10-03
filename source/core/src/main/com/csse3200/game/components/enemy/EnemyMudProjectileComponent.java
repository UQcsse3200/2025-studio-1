package com.csse3200.game.components.enemy;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Enemy mud projectile: Moves in a straight line with a set lifetime,
 * and automatically destroys itself when the lifetime expires.
 * <p>
 * Requires: The same Entity should already contain
 * TextureRenderComponent("images/mud.png") and TouchAttackComponent(PLAYER).
 */
public class EnemyMudProjectileComponent extends Component {
    private final Vector2 velocity;
    private final float lifeSec;
    private float t = 0f;

    public EnemyMudProjectileComponent(Vector2 velocity, float lifeSec) {
        this.velocity = new Vector2(velocity);
        this.lifeSec = lifeSec;
    }

    @Override
    public void create() {
        // Defensive check (ensures components exist if not already added externally)
        if (entity.getComponent(TextureRenderComponent.class) == null) {
            entity.addComponent(new TextureRenderComponent("images/mud.png"));
            entity.getComponent(TextureRenderComponent.class).scaleEntity();
        }
        if (entity.getComponent(TouchAttackComponent.class) == null) {
            entity.addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 0.2f));
        }
    }

    @Override
    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        t += dt;
        if (t >= lifeSec) {
            entity.dispose();
            return;
        }
        entity.setPosition(entity.getPosition().add(velocity.x * dt, velocity.y * dt));
    }
}