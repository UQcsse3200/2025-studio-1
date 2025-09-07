package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.entity.EntityComponent;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility factory class for creating item entities in the game.
 * <p>
 * <p>Each item entity type should have a creation method that returns a corresponding entity.
 */
public class ItemFactory {
    private static final Logger log = LoggerFactory.getLogger(ItemFactory.class);

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
        itemTest.scaleHeight(1.0f);  // redundant
        PhysicsUtils.setScaledCollider(itemTest, 1.0f, 1.0f);  // redundant

        itemTest.getComponent(PhysicsComponent.class).getBody().setUserData(itemTest);

        return itemTest;
    }

    public static Entity createItem(String texture) {
        Entity item = new Entity()
                .addComponent(new ItemComponent())
                .addComponent(new TextureRenderComponent(texture))
                .addComponent(new PhysicsComponent())
                .addComponent(new EntityComponent());

        item.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
        item.getComponent(TextureRenderComponent.class).scaleEntity();
        item.getComponent(PhysicsComponent.class).getBody().setUserData(item);

        return item;
    }

    /**
     * Stops you from making an ItemFactory object.
     * If you try, it throws an error.
     */
    private ItemFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}



