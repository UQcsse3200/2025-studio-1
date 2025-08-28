package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.badlogic.gdx.graphics.Color;
import com.csse3200.game.rendering.SolidColorRenderComponent;

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

  /**
   * Creates a door trigger as a thin sensor line (no collision) that can be placed at a border.
   * The door can be detected via collisionStart events with the player's hitbox.
   * @param width width in world units
   * @param height height in world units
   * @return sensor entity
   */
  public static Entity createDoorTrigger(float width, float height) {
    Entity trigger = new Entity()
        .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.DEFAULT))
        .addComponent(new SolidColorRenderComponent(Color.BLACK));
    // Make collider a sensor so it doesn't block movement
    trigger.getComponent(ColliderComponent.class).setSensor(true);
    trigger.setScale(width, height);
    return trigger;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
