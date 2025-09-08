package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.TagComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.entity.EntityComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.ItemComponent;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.rendering.AnimationRenderComponent;

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

    public static Entity createWeapon(WeaponConfig config) {
        Entity weapon = ItemFactory.createItem(config.texturePath);
        weapon.addComponent(new WeaponsStatsComponent(config));

        ItemComponent item = weapon.getComponent(ItemComponent.class);

        switch (config.weaponType) {
            case RANGED -> item.setType(ItemTypes.RANGED);
            case MELEE -> item.setType(ItemTypes.MELEE);
        }

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
