package com.csse3200.game.entities.factories.characters;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

public class FriendlyNPCFactory {
    public static Entity createTip() {
        Entity tip = new Entity()
                .addComponent(new TextureRenderComponent("images/fireball.png"));

        tip.getComponent(TextureRenderComponent.class).scaleEntity();

        return tip;
    }
}