package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.TagComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class PowerupsFactory {

    private static final float duration = 30f;

    public static Entity createRapidFire() {
        Entity powerup = createBasePowerup("rapidfire");

        powerup.addComponent(new TextureRenderComponent("images/pistol.png"));
        powerup.getComponent(TextureRenderComponent.class).scaleEntity();
        powerup.getComponent(PhysicsComponent.class).getBody().setUserData(powerup);

        return powerup;
    }

    public static void applyRapidFire(Entity player, float seconds) {
        CombatStatsComponent playerStats =
                player.getComponent(CombatStatsComponent.class);

        if (playerStats != null) {
            float originalCooldown = playerStats.getCoolDown();

            playerStats.setCoolDown(0f);

            // why is this not working :(((((((
            player.addComponent(new Component() {
                private float elapsed = 0f;

                @Override
                public void update() {
                    float dt = ServiceLocator.getTimeSource().getDeltaTime();
                    elapsed += dt;
                    if (elapsed > seconds) {
                        playerStats.setCoolDown(originalCooldown);
                        this.setEnabled(false);
                    }
                }
            });
        }
    }

    public static Entity createBasePowerup(String type) {
        Entity powerup = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                .addComponent(new HitboxComponent())
//                .addComponent(new ItemComponent(1))
                .addComponent(new TagComponent(type));

        PhysicsUtils.setScaledCollider(powerup, 0.5f, 0.5f);
        return powerup;
    }

    private PowerupsFactory() {
        throw new IllegalArgumentException("Instantiating static util class");
    }
}
