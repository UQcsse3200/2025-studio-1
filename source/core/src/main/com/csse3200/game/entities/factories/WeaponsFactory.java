package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TagComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

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
            FileLoader.readClass(LightsaberConfig.class, "configs/lightsaber.json");
    private static final DaggerConfig daggerConfigs =
            FileLoader.readClass(DaggerConfig.class, "configs/dagger.json");

    /**
     * Creates a lightsaber entity.
     *
     * @return lightsaber entity
     */
    public static Entity createLightsaber() {

        Entity lightsaber = createBaseWeapon("melee");
        lightsaber.addComponent(new TextureRenderComponent("images/lightsaberSingle.png"))
            .addComponent(new CombatStatsComponent(0, lightsaberConfigs.baseAttack))
            .addComponent(new ItemComponent(1, "Lightsaber weapon"));
        lightsaber.getComponent(TextureRenderComponent.class).scaleEntity();
        lightsaber.getComponent(PhysicsComponent.class).getBody().setUserData(lightsaber);
        return lightsaber;
    }

    /**
     * Creates a dagger entity.
     * @return A dagger entity.
     */
    public static Entity createDagger() {

        Entity dagger = createBaseWeapon("melee");
        dagger.addComponent(new TextureRenderComponent("images/dagger.png"))
          .addComponent(new CombatStatsComponent(0, daggerConfigs.baseAttack))
          .addComponent(new ItemComponent(1, "Dagger weapon"));
        dagger.getComponent(TextureRenderComponent.class).scaleEntity();
        dagger.scaleHeight(0.55f);
        dagger.getComponent(PhysicsComponent.class).getBody().setUserData(dagger);
        return dagger;
    }

    /**
     * Creates a pistol entity.
     * @return A pistol entity.
     */
    public static Entity createPistol() {
        Entity pistol = createBaseWeapon("ranged");
        pistol.addComponent(new TextureRenderComponent("images/pistol.png"))
                .addComponent(new CombatStatsComponent(0, lightsaberConfigs.baseAttack))
                .addComponent(new ItemComponent(1, "Pistol weapon"));
        pistol.getComponent(TextureRenderComponent.class).scaleEntity();
        pistol.getComponent(PhysicsComponent.class).getBody().setUserData(pistol);
        return pistol;
    }

    /**
     * Create a generic base weapon
     * @param type "melee" or "ranged"
     * @return the base weapon
     */
    private static Entity createBaseWeapon(String type) {
        Entity weapon =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                        .addComponent(new HitboxComponent())
                        .addComponent(new TagComponent(type));

        PhysicsUtils.setScaledCollider(weapon, 0.9f, 0.4f);
        return weapon;
    }

    /**
     * Creates an animation component that can be used with a weapon.
     * @param atlasName Name of the animation atlas.
     * @param player Current player.
     * @return An AnimationRenderComponent that can be attached to a weapon.
     */
    public static AnimationRenderComponent createAnimation(String atlasName, Entity player) {
        TextureAtlas atlas = new TextureAtlas(atlasName);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        animator.addAnimation("anim", 0.1f, Animation.PlayMode.NORMAL);
        player.getEvents().addListener("anim", () -> animator.startAnimation("anim"));
        return animator;
    }

    private WeaponsFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
