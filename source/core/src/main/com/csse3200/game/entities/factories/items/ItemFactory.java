package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
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
     * This class is called by other Factories, to add other components as needed.
     * @param texture texture path of the item's texture
     * @return entity representing an item
     */
    public static Entity createItem(String texture) {
        Entity item = new Entity()
                .addComponent(new ItemComponent())
                .addComponent(new HitboxComponent())
                .addComponent(new TextureRenderComponent(texture))
                .addComponent(new PhysicsComponent());

        item.getComponent(ItemComponent.class).setTexture(texture);
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
