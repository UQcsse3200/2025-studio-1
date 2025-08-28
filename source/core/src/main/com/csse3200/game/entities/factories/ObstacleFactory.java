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

  public static Entity createLongFloor() {
    Entity longFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    longFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    longFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    longFloor.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(longFloor, 1f, 1f);
    return longFloor;
  }

  public static Entity createRailing() {
    Entity railing =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/Railing.png"));

    railing.getComponent(TextureRenderComponent.class).scaleEntity();
    railing.scaleHeight(0.7f);
    return railing;
  }

  public static Entity createSmallSquare() {
    Entity smallSquare =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/SmallSquare.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    smallSquare.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    smallSquare.getComponent(TextureRenderComponent.class).scaleEntity();
    smallSquare.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(smallSquare, 1f, 1f);
    return smallSquare;
  }

  public static Entity createSmallStair() {
    Entity smallStair =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    smallStair.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    smallStair.getComponent(TextureRenderComponent.class).scaleEntity();
    smallStair.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(smallStair, 1f, 1f);
    return smallStair;
  }

  public static Entity createSquareTile() {
    Entity squareTile =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    squareTile.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    squareTile.getComponent(TextureRenderComponent.class).scaleEntity();
    squareTile.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(squareTile, 1f, 1f);
    return squareTile;
  }

  public static Entity createThickFloor() {
    Entity thickFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    thickFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    thickFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    thickFloor.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(thickFloor, 1f, 1f);
    return thickFloor;
  }

  public static Entity createThinFloor() {
    Entity thinFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/LongFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    thinFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    thinFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    thinFloor.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(thinFloor, 1f, 1f);
    return thinFloor;
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
