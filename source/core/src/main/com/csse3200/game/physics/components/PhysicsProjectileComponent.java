package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Movement controller for a projectile entity
 */
public class PhysicsProjectileComponent extends Component {

    private static final Logger logger = LoggerFactory.
            getLogger(PhysicsProjectileComponent.class);
    protected final float lifetime = 5f;
    protected PhysicsComponent physicsComponent;
    protected Vector2 initialVelocity;
    protected float lived = 0f;

    /**
     * Manifests the physics for the projectile
     */
    @Override
    public void create() {

        physicsComponent = entity.getComponent(PhysicsComponent.class);

        Body body = physicsComponent.getBody();

        body.setBullet(true);
        body.setGravityScale(0f);
        body.setFixedRotation(true);
        body.setLinearDamping(0f);

        if (initialVelocity != null) {
            body.setLinearVelocity(initialVelocity);
        }
    }

    /**
     * Checks how long the projectile has been alive for, adding it to the "to remove list"
     * within entity service if it has exceeded this time.
     */

    public void update() {
        float dt = ServiceLocator.getTimeSource().getDeltaTime();
        lived += dt;

        if (lived > lifetime) {
            entity.setToRemove();
            Body body = physicsComponent.getBody();
            body.setLinearVelocity(new Vector2(0f, 0f));
        }
    }

    /**
     * Fires a bullet in a certain direction at a certain speed
     */
    public void fire(Vector2 direction, float speed) {

        initialVelocity = direction.nor().scl(speed);
        Body body = physicsComponent.getBody();
        body.setBullet(true);
        body.setLinearVelocity(initialVelocity);
    }

    /**
     * Setter function used for tests
     *
     * @param lived assigned lived time for a projectile
     */
    public void setLived(float lived) {
        this.lived = lived;
    }
}
