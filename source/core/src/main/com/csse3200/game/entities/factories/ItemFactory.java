package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Utility factory class for creating item entities in the game.
 * <p>
 * <p>Each item entity type should have a creation method that returns a corresponding entity.
 */
public class ItemFactory {
    /**
     * Creates and configures a new item entity.
     * The item has a texture, physics, and other needed parts.
     * @return entity representing an item
     */
    public static Entity createItem() {
        String texture = "images/heart.png";
        Entity itemTest = new Entity()
                .addComponent(new TextureRenderComponent(texture))
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                .addComponent(new HitboxComponent())
                .addComponent(new ItemComponent(1, texture));


        itemTest.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        itemTest.getComponent(TextureRenderComponent.class).scaleEntity();
        itemTest.scaleHeight(1.0f);
        PhysicsUtils.setScaledCollider(itemTest, 1.0f, 1.0f);

        itemTest.getComponent(PhysicsComponent.class).getBody().setUserData(itemTest);

        return itemTest;
    }
    /**
     * Stops you from making an ItemFactory object.
     * If you try, it throws an error.
     */
    private ItemFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}



