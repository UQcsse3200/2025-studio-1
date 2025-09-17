package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.ai.movement.MovementController;
import com.csse3200.game.components.Component;
import com.csse3200.game.utils.math.Vector2Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Movement controller for a physics-based entity.
 */
public class PhysicsMovementComponent extends Component implements MovementController {
    private static final Logger logger = LoggerFactory.getLogger(PhysicsMovementComponent.class);
    private static final Vector2 maxSpeed = Vector2Utils.ONE;

    private Vector2 speed;
    private PhysicsComponent physicsComponent;
    private Vector2 targetPosition;
    private boolean movementEnabled = true;
    private volatile boolean disposed = false;

    public PhysicsMovementComponent() {
        speed = maxSpeed;
    }

    public PhysicsMovementComponent(Vector2 speed) {
        this.speed = speed;
    }

    @Override
    public void create() {
        physicsComponent = entity.getComponent(PhysicsComponent.class);
    }

    @Override
    public void update() {
        if (disposed) {
            return;
        }
        if (movementEnabled && targetPosition != null) {
            if (physicsComponent == null) {
                return; // Not yet initialised or already disposed
            }
            Body body = physicsComponent.getBody();
            if (body == null) {
                return; // Body destroyed or not created yet
            }
            updateDirection(body);
        }
    }

    /**
     * Enable/disable movement for the controller. Disabling will immediately set velocity to 0.
     *
     * @param movementEnabled true to enable movement, false otherwise
     */
    @Override
    public void setMoving(boolean movementEnabled) {
        if (disposed) {
            return;
        }
        this.movementEnabled = movementEnabled;
        if (!movementEnabled) {
            if (physicsComponent == null) {
                return; // Nothing to stop yet
            }
            Body body = physicsComponent.getBody();
            if (body == null) {
                return; // Already disposed
            }
            setToVelocity(body, Vector2.Zero);
        }
    }

    @Override
    public boolean getMoving() {
        return movementEnabled;
    }

    /**
     * @return Target position in the world
     */
    @Override
    public Vector2 getTarget() {
        return targetPosition;
    }

    /**
     * Set a target to move towards. The entity will be steered towards it in a straight line, not
     * using pathfinding or avoiding other entities.
     *
     * @param target target position
     */
    @Override
    public void setTarget(Vector2 target) {
        logger.trace("Setting target to {}", target);
        this.targetPosition = target;
    }

    public void setSpeed(Vector2 speed) {
        this.speed = speed;
    }

    private void updateDirection(Body body) {
        Vector2 desiredVelocity = getDirection().scl(speed);
        setToVelocity(body, desiredVelocity);
    }

    private void setToVelocity(Body body, Vector2 desiredVelocity) {
        if (disposed) {
            return;
        }
        if (body == null) {
            // Attempt to re-fetch in case of race where original body was cleared after call site null-check
            if (physicsComponent != null) {
                body = physicsComponent.getBody();
            }
            if (body == null) {
                logger.debug("Skipping velocity update: body is null (entity={})", entity);
                return; // Safety guard
            }
        }
        try {
            Vector2 velocity = body.getLinearVelocity();
            Vector2 impulse = desiredVelocity.cpy().sub(velocity).scl(body.getMass());
            body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
        } catch (NullPointerException e) {
            // Final defensive catch (should not happen after guards)
            logger.warn("Body became null during velocity update (entity={})", entity);
        }
    }

    private Vector2 getDirection() {
        // Move towards targetPosition based on our current position
        return targetPosition.cpy().sub(entity.getPosition()).nor();
    }

    /**
     * Makes the entity jump by applying a vertical impulse.
     *
     * @param force The strength of the jump (vertical impulse).
     */
    public void jump(float force) {
        if (disposed) {
            return;
        }
        if (physicsComponent != null) {
            Body body = physicsComponent.getBody();
            if (body != null) {
                body.applyLinearImpulse(new Vector2(0, force), body.getWorldCenter(), true);
            }
        }
    }

    @Override
    public void dispose() {
        disposed = true;
        movementEnabled = false;
        targetPosition = null;
        physicsComponent = null; // release reference; any in-flight calls will early-exit
    }
}
