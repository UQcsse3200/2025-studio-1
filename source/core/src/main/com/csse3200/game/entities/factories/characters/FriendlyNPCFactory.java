package com.csse3200.game.entities.factories.characters;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;

public class FriendlyNPCFactory {
    public static Entity createTip() {
        Entity tip = new Entity()
                .addComponent(new TextureRenderComponent("images/!.png"));

        tip.getComponent(TextureRenderComponent.class).scaleEntity();

        return tip;
    }

    public static Entity createTest() {
        Entity test = new Entity()
                .addComponent(new TextureRenderComponent("images/!.png"));
        test.getComponent(TextureRenderComponent.class).scaleEntity();
        return test;
    }

    public static Entity createNPC1() {
        Entity npc1 = new Entity()
                .addComponent(new TextureRenderComponent("images/!.png"));
        npc1.getComponent(TextureRenderComponent.class).scaleEntity();
        return npc1;
    }

    public static Entity createNPC2() {
        Entity npc2 = new Entity()
                .addComponent(new TextureRenderComponent("images/!.png"));
        npc2.getComponent(TextureRenderComponent.class).scaleEntity();
        return npc2;
    }

    public static Entity createNPC3() {
        Entity npc3 = new Entity()
                .addComponent(new TextureRenderComponent("images/!.png"));
        npc3.getComponent(TextureRenderComponent.class).scaleEntity();
        return npc3;
    }
}