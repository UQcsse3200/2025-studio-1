package com.csse3200.game.entities.factories;

import com.csse3200.game.components.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class PowerupsFactory {

    public static Entity createRapidFire() {
        Entity powerup = createBasePowerup("rapidfire");

        powerup.addComponent(new TextureRenderComponent("images/pistol.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.getComponent(PhysicsComponent.class).getBody().setUserData(powerup);

        return powerup;
    }

    public static Entity createUnlimitedAmmo() {
        Entity powerup = createBasePowerup("unlimitedammo");

        powerup.addComponent(new TextureRenderComponent("images/rifle.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.getComponent(PhysicsComponent.class).getBody().setUserData(powerup);

        return powerup;
    }

    public static Entity createBasePowerup(String type) {
        Entity powerup = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                .addComponent(new HitboxComponent())
                .addComponent(new TagComponent(type));

        PhysicsUtils.setScaledCollider(powerup, 0.5f, 0.5f);
        return powerup;
    }

    private PowerupsFactory() {
        throw new IllegalArgumentException("Instantiating static util class");
    }
}
