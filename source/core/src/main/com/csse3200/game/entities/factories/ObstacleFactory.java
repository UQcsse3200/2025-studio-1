package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;


// TODO delete these imports when finished testing
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.components.entity.item.ItemComponent;
/**
 * Factory to create obstacle entities.
 *
 * <p>Each obstacle entity type should have a creation method that returns a corresponding entity.
 */
public class ObstacleFactory {

  /**
   * Creates a tree entity.
   * @return entity
   */
  public static Entity createTree() {
    Entity tree =
        new Entity()
            .addComponent(new TextureRenderComponent("images/tree.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    tree.getComponent(TextureRenderComponent.class).scaleEntity();
    tree.scaleHeight(2.5f);
    PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);
    return tree;
  }

  // This function was used for testing the itemPickUp functionality, and might be useful later for further testing
//  public static Entity createTree() {
//    Entity tree = new Entity()
//            .addComponent(new TextureRenderComponent("images/mud.png"))
//            .addComponent(new PhysicsComponent())
//            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.ITEM))
//            .addComponent(new HitboxComponent())
//            .addComponent(new ItemComponent(1));
//
//
//    tree.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
//    tree.getComponent(TextureRenderComponent.class).scaleEntity();
//    tree.scaleHeight(2.5f);
//    PhysicsUtils.setScaledCollider(tree, 2.0f, 1.8f);
//
//    //new  tree.getComponent(PhysicsComponent.class).getBody().setUserData(tree);
//
//    return tree;
//  }



  /**
   * Creates an invisible physics wall.
   * @param width Wall width in world units
   * @param height Wall height in world units
   * @return Wall entity of given width and height
   */
  public static Entity createWall(float width, float height) {
    Entity wall = new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    wall.setScale(width, height);
    return wall;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}

