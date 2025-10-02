package com.csse3200.game.entities.factories;

import com.csse3200.game.components.TagComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class PowerupsFactory {

    public static Entity createRapidFire() {
        Entity powerup = createBasePowerup();

        powerup.addComponent(new TextureRenderComponent("images/rapidfirepowerup.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.addComponent(new TagComponent("rapidfire"));


        return powerup;
    }

    public static Entity createUnlimitedAmmo() {
        Entity powerup = createBasePowerup();

        powerup.addComponent(new TextureRenderComponent("images/rifle.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.addComponent(new TagComponent("unlimitedammo"));


        return powerup;
    }

    public static Entity createAimBot() {
        Entity powerup = createBasePowerup();

        powerup.addComponent(new TextureRenderComponent("images/aimbot_powerup.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.addComponent(new TagComponent("aimbot"));


        return powerup;
    }

    public static Entity createBasePowerup() {
        Entity powerup = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                .addComponent(new HitboxComponent());

        PhysicsUtils.setScaledCollider(powerup, 0.5f, 0.5f);
        return powerup;
    }

    private PowerupsFactory() {
        throw new IllegalArgumentException("Instantiating static util class");
    }
}
