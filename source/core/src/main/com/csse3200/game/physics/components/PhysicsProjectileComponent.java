package com.csse3200.game.physics.components;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Movement controller for a projectile entity */
public class PhysicsProjectileComponent extends Component{

    private static final Logger logger = LoggerFactory.
            getLogger(PhysicsProjectileComponent.class);
    private PhysicsComponent physicsComponent;
    private Vector2 initialVelocity;
    private float lifetime = 5f;
    private float lived = 0f;
    private boolean paused = false;

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
        if (!ServiceLocator.getTimeSource().isPaused()) {
            float dt = ServiceLocator.getTimeSource().getDeltaTime();
            lived += dt;

            if (lived > lifetime) {
                entity.setToRemove();
                Body body = physicsComponent.getBody();
                body.setLinearVelocity(new Vector2(0f, 0f));
            }
        }
    }

    /**
     * Fires a bullet in a certain direction at a certain speed
     *
     */

    public void fire(Vector2 direction, float speed) {

        initialVelocity = direction.nor().scl(speed);
        Body body = physicsComponent.getBody();
        body.setBullet(true);
        body.setLinearVelocity(initialVelocity);
    }

    /**
     * Setter function used for tests
     * @param lived assigned lived time for a projectile
     */
    public void setLived(float lived) {
        this.lived = lived;
    }



}
