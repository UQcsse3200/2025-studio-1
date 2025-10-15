package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Armour;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public class WorldPickUpFactory {
    public static Entity createWeaponPickup(Weapons type) {
        String tex = type.getConfig().texturePath;

        Entity pickup = new Entity()
                .addComponent(new TextureRenderComponent(tex))
                .addComponent(new PhysicsComponent())
                //.addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent())
                .addComponent(new ItemComponent());

        pickup.getComponent(ItemComponent.class).setTexture(tex);
        pickup.getComponent(ItemComponent.class).setType(
                type.getConfig().weaponType == com.csse3200.game.entities.configs.ItemTypes.MELEE
                        ? ItemTypes.MELEE : ItemTypes.RANGED);

        pickup.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.StaticBody);
        return pickup;
    }

    public static Entity createArmourPickup(Armour type) {
        String tex = type.getConfig().texturePath;

        Entity pickup = new Entity()
                .addComponent(new TextureRenderComponent(tex))
                .addComponent(new PhysicsComponent())
                .addComponent(new HitboxComponent())
                .addComponent(new ItemComponent());

        pickup.getComponent(ItemComponent.class).setTexture(tex);
        pickup.getComponent(ItemComponent.class).setType(
                type.getConfig().armourType == ItemTypes.CHESTPLATE_ARMOUR
                        ? ItemTypes.CHESTPLATE_ARMOUR : ItemTypes.HOOD_ARMOUR);

        pickup.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.StaticBody);
        return pickup;
    }

    /**
     * Build from texture when dropping
     */
    public static Entity createPickupFromTexture(String texture) {
        for (Weapons w : Weapons.values()) {
            if (texture.equals(w.getConfig().texturePath)) {
                return createWeaponPickup(w);
            }
        }

        for (Armour a : Armour.values()) {
            if (texture.equals(a.getConfig().texturePath)) {
                return createArmourPickup(a);
            }
        }
        return null;
    }
}
