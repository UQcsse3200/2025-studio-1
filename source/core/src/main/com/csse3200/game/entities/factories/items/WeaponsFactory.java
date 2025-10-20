package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.attachments.LaserComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.items.RangedUseComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.RangedWeaponConfig;
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

    private WeaponsFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

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

        //Set the weapon's name from the config
        ItemComponent item = weapon.getComponent(ItemComponent.class);
        setItemNameFromConfig(config, item);

        // Attach type to weapon
        switch (config.weaponType) {
            case RANGED:
                RangedWeaponConfig rangedConfig = (RangedWeaponConfig) config;
                item.setType(ItemTypes.RANGED);
                weapon.addComponent(new MagazineComponent(rangedConfig.ammoCapacity));

                // using TextureRenderWithRotationComponent to allow guns to follow cursor
                weapon.addComponent(new TextureRenderWithRotationComponent(rangedConfig.texturePath));
                weapon.getComponent(TextureRenderComponent.class).disableComponent();
                weaponStats.setCoolDown((float) rangedConfig.shootTimer);

                weapon.addComponent(new RangedUseComponent());
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
     * Creates a weapon with attachments on it
     *
     * @param weaponType the type of weapon
     * @param laser      true if laser attachment is wanted
     * @param bullet     true if bullet attachment is wanted
     * @return the new weapon with attachments
     */
    public static Entity createWeaponWithAttachment(Weapons weaponType, boolean laser, boolean bullet) {
        Entity weapon = createWeapon(weaponType);

        if (weaponType != Weapons.PISTOL && weaponType != Weapons.RIFLE) {
            return weapon;
        }

        if (laser) {
            weapon.addComponent(new LaserComponent());
        }

        if (bullet) {
            weapon.addComponent(new BulletEnhancerComponent());
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

    /**
     * Copies the name from the specified {@link WeaponConfig} into the given {@link ItemComponent}.
     * If the config's name is not null or empty, this sets the item's name to match the config.
     *
     * @param config is the weapon configuration containing the name to assign
     * @param item   is the ItemComponent whose name will be assigned
     */
    public static void setItemNameFromConfig(WeaponConfig config, ItemComponent item) {
        if (!config.getName().isEmpty()) {
            item.setName(config.getName());
        }
    }
}
