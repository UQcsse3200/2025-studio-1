package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Factory to create non-playable character (NPC) entities with predefined components.
 *
 * <p>Each NPC entity type should have a creation method that returns a corresponding entity.
 * Predefined entity properties can be loaded from lightsaberConfigs stored as json files which are defined in
 * "NPCConfigs".
 *
 * <p>If needed, this factory can be separated into more specific factories for entities with
 * similar characteristics.
 */
public class WeaponsFactory {
    private static final LightsaberConfig lightsaberConfigs =
            FileLoader.readClass(LightsaberConfig.class, "lightsaberConfigs/lightsaber.json");
    private static final DaggerConfig daggerConfigs =
            FileLoader.readClass(DaggerConfig.class, "configs/dagger.json");

    /**
     * Creates a ghost entity.
     *
     * @param target entity to chase
     * @return entity
     */
    public static Entity createLightsaber() {
        Entity lightsaber =
                new Entity()
                        .addComponent(new TextureRenderComponent("images/templightsaber.png"))
//                        .addComponent(new PhysicsComponent())
//                        .addComponent(new ColliderComponent())
//                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
                        .addComponent(new CombatStatsComponent(0, lightsaberConfigs.baseAttack));


        lightsaber.getComponent(TextureRenderComponent.class).scaleEntity();
        return lightsaber;
    }

    public static Entity createDagger() {
        Entity dagger= new Entity()
                .addComponent(new TextureRenderComponent("images/dagger.png"))
                .addComponent(new CombatStatsComponent(0, daggerConfigs.baseAttack));

        dagger.getComponent(TextureRenderComponent.class).scaleEntity();
        dagger.scaleHeight(0.75f);
        return dagger;
    }

    /**
     * Creates a generic NPC to be used as a base entity by more specific NPC creation methods.
     *
     * @return entity
     */
    private static Entity createBaseWeapon(Entity target) {
        Entity weapon =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));

        PhysicsUtils.setScaledCollider(weapon, 0.9f, 0.4f);
        return weapon;
    }

    private WeaponsFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
