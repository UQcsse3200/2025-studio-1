package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.screens.ShopScreenDisplay;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

public final class ShopFactory {
    private ShopFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    public static Entity createShop(ForestGameArea area,
                                    ShopManager manager,
                                    String kioskTexture) {

        Entity shop = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TextureRenderComponent(kioskTexture))
                .addComponent(manager)
                .addComponent(new ShopScreenDisplay(area, manager));
        shop.create();

        shop.setInteractable(true);

        PhysicsComponent phys = shop.getComponent(PhysicsComponent.class);
        phys.setBodyType(BodyDef.BodyType.StaticBody);
        BodyUserData userData = new BodyUserData();
        userData.entity = shop;
        phys.getBody().setUserData(userData);

        shop.getComponent(TextureRenderComponent.class).scaleEntity();

        HitboxComponent hitbox = shop.getComponent(HitboxComponent.class);
        hitbox.setAsBox(new Vector2(1.0f, 2f));
        try {
            hitbox.setSensor(true);
        } catch (Throwable ignored) {
        }

        return shop;
    }
}
