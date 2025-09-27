package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.attachments.LaserComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.RifleConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;

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

    /**
     * Creates a new weapon entity based on the given weapon type.
     * <p>
     * This method retrieves the {@link WeaponConfig} for the provided {@link Weapons} type,
     * builds an {@link Entity} using its texture, and attaches the appropriate
     * components to configure its stats and item type (melee or ranged).
     * </p>
     *
     * @param weaponType the type of weapon to create (defines stats and configuration)
     * @return a fully configured {@link Entity} representing the weapon
     */
    public static Entity createWeapon(Weapons weaponType) {
        WeaponConfig config = weaponType.getConfig();
        Entity weapon = ItemFactory.createItem(config.texturePath);
        weapon.addComponent(new WeaponsStatsComponent(config));
        WeaponsStatsComponent weaponStats = weapon.getComponent(WeaponsStatsComponent.class);
        weaponStats.setCoolDown(0.2f);


        ItemComponent item = weapon.getComponent(ItemComponent.class);
        setItemNameFromConfig(config, item);

        // Attach type to weapon
        switch (config.weaponType) {
            case RANGED:
                item.setType(ItemTypes.RANGED);
                weapon.addComponent(new MagazineComponent(20));
                // using TextureRenderWithRotationComponent to allow guns to follow cursor
                weapon.addComponent(new TextureRenderWithRotationComponent(config.texturePath));
                weapon.getComponent(TextureRenderComponent.class).disableComponent();
                if (weaponType.getConfig() instanceof RifleConfig) {
                    weapon.addComponent(new LaserComponent());
                    weapon.addComponent(new BulletEnhancerComponent());
                }
                break;
            case MELEE:
                item.setType(ItemTypes.MELEE);
                break;
            default:
                item.setType(ItemTypes.NONE);
                break;
        }
        return weapon;
    }

    /**
     * Creates an animation component that can be used with a weapon.
     *
     * @param atlasName Name of the animation atlas.
     * @param player    Current player.
     * @return An AnimationRenderComponent that can be attached to a weapon.
     */
    public static AnimationRenderComponent createAnimation(String atlasName, Entity player) {
        TextureAtlas atlas = new TextureAtlas(atlasName);
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        animator.addAnimation("anim", 0.1f, Animation.PlayMode.NORMAL);
        player.getEvents().addListener("anim", () -> animator.startAnimation("anim"));
        return animator;
    }

    public static void setItemNameFromConfig(WeaponConfig config, ItemComponent item) {
        if (config.getName() != null && !config.getName().isEmpty()) {
            item.setName(config.getName());
        }
    }


    private WeaponsFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
