package com.csse3200.game.entities.configs.projectiles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ActiveProjectileTypes;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * An extension of current projectile functionality based in Entity, so that it's trajectory and flight path are
 * affected during runtime. Includes things like following a specific target, and being affected by gravity in a 2D
 * platformer setting.
 */
public class ActiveProjectile extends Entity {
    private PhysicsComponent physicsComponent;
    private TextureRenderWithRotationComponent renderComponent;
    private GameTime gameTime;
    private float gravityStrength;
    private Entity target;
    private ActiveProjectileTypes activeProjectileType;
    private float speed;

    /**
     * Sets the gravity strength of the projectile, used if set to ARC projectile type.
     * @param gravityStrength The strength of the gravity affecting the projectile
     */
    public void setGravityStrength(float gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    /**
     * Sets the target that the projectile will continuously follow, used if set to FOLLOW_TARGET projectile type.
     * @param target The target entity to follow/home into
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
     * Sets the projectile type of this active projectile
     * @param type The type of this active projectile
     */
    public void setActiveProjectileType(ActiveProjectileTypes type) {
        this.activeProjectileType = type;
    }

    public void setProjectileSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void create() {
        super.create();

        // Retrieve the necessary components to perform runtime calculations and updates.
        physicsComponent = getComponent(PhysicsComponent.class);
        renderComponent = getComponent(TextureRenderWithRotationComponent.class);
        gameTime = ServiceLocator.getTimeSource();
    }

    @Override
    public void update() {
        super.update();

        // Don't update it if any of the following parts are missing
        if (physicsComponent == null) { return; }
        if (renderComponent == null) { return; }
        if (activeProjectileType == null) { return; }

        // Defines how the projectile moves over its lifetime
        switch (activeProjectileType) {
            case ARC -> ArcProjectile();
            case FOLLOW_TARGET -> FollowTarget();
            default -> throw new IllegalArgumentException("Unknown active behaviour: " + activeProjectileType);
        }

        // Updates rotation of sprite as needed.
        Vector2 velocity = physicsComponent.getBody().getLinearVelocity();
        float angle = (float) (Math.atan2(velocity.y, velocity.x) * MathUtils.radiansToDegrees) + 90;
        renderComponent.setRotation(angle);
    }

    /**
     * Called every frame, causing the projectile to be affected by gravity in a 2D platformer setting.
     */
    private void ArcProjectile() {
        physicsComponent.getBody().applyForceToCenter(
                new Vector2(0, -gravityStrength * gameTime.getDeltaTime()), true);
    }

    /**
     * Called every frame, causing the projectile to follow a specific entity (the target). Not affected by gravity.
     */
    private void FollowTarget() {
        Vector2 dirToFire = new Vector2(target.getPosition().x - this.getPosition().x,
                target.getPosition().y - this.getPosition().y);
        physicsComponent.getBody().setLinearVelocity(dirToFire.nor().scl(speed));
    }
}
