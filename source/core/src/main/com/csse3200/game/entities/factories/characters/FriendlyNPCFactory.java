package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.friendlynpc.TipComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class FriendlyNPCFactory {
    public static Entity createTip() {
        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/!animation.atlas", TextureAtlas.class);
        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.addAnimation("float",       0.12f, Animation.PlayMode.LOOP);
        Entity tip = new Entity().addComponent(arc);
        arc.scaleEntity();
        arc.startAnimation("float");
        return tip;
    }

    public static Entity createTest(Entity player) {
        Entity test = new Entity()
                .addComponent(new TextureRenderComponent("images/fireball.png"));
        test.getComponent(TextureRenderComponent.class).scaleEntity();
        test.addComponent(new TipComponent(test, player, 3f));
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