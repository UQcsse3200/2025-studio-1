package com.csse3200.game.entities.configs.projectiles;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class ArcProjectile extends Entity {
    private PhysicsComponent physicsComponent;
    private TextureRenderWithRotationComponent renderComponent;
    private GameTime gameTime;
    private float gravityStrength;

    public void setGravityStrength(float gravityStrength) {
        this.gravityStrength = gravityStrength;
    }

    @Override
    public void create() {
        super.create();

        physicsComponent = getComponent(PhysicsComponent.class);
        renderComponent = getComponent(TextureRenderWithRotationComponent.class);
        gameTime = ServiceLocator.getTimeSource();
    }

    @Override
    public void update() {
        super.update();

        if (physicsComponent == null || renderComponent == null) { return; }

        physicsComponent.getBody().applyForceToCenter(
                new Vector2(0, -gravityStrength * gameTime.getDeltaTime()), true);
        
        Vector2 velocity = physicsComponent.getBody().getLinearVelocity();
        float angle = (float) (Math.atan2(velocity.y, velocity.x) * MathUtils.radiansToDegrees) + 90;
        renderComponent.setRotation(angle);
    }
}
