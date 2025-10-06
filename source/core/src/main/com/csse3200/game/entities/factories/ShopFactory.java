package com.csse3200.game.entities.factories;

import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.screens.ShopScreenDisplay;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

public final class ShopFactory {
    private ShopFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    public static Entity createShop(ForestGameArea area,
                                    ShopManager manager,
                                    String kioskTexture) {

        Entity shop = InteractableStationFactory.createBaseStation()
                .addComponent(new TextureRenderComponent(kioskTexture))
                .addComponent(manager)
                .addComponent(new ShopScreenDisplay(area, manager));

        shop.getComponent(TextureRenderComponent.class).scaleEntity();
        shop.create();

        return shop;
    }
}
