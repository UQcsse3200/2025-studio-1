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
import com.csse3200.game.rendering.DoorRenderComponent;

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

  public static Entity createBigThickFloor() {
    Entity bigThickFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/ThickFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    bigThickFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    bigThickFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    bigThickFloor.scaleHeight(20f);
    PhysicsUtils.setScaledCollider(bigThickFloor, 1f, 1f);
    return bigThickFloor;
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
          .addComponent(new TextureRenderComponent("foreg_sprites/general/SmallStair.png"))
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
          .addComponent(new TextureRenderComponent("foreg_sprites/general/SquareTile.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    squareTile.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    squareTile.getComponent(TextureRenderComponent.class).scaleEntity();
    squareTile.scaleHeight(2f);
    PhysicsUtils.setScaledCollider(squareTile, 1f, 1f);
    return squareTile;
  }

  public static Entity createThickFloor() {
    Entity thickFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/ThickFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    thickFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    thickFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    thickFloor.scaleHeight(3f);
    PhysicsUtils.setScaledCollider(thickFloor, 1f, 1f);
    return thickFloor;
  }

  public static Entity createThinFloor() {
    Entity thinFloor =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/general/ThinFloor.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    thinFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    thinFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    thinFloor.scaleHeight(0.8f);
    PhysicsUtils.setScaledCollider(thinFloor, 1f, 1f);
    return thinFloor;
  }

  public static Entity createPurpleSpawnPad() {
    Entity purpSpawn = 
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/spawn_pads/SpawnPadPurple.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    purpSpawn.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    purpSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
    purpSpawn.scaleHeight(0.7f);
    PhysicsUtils.setScaledCollider(purpSpawn, 1f, 1f);
    return purpSpawn;
  }

  public static Entity createRedSpawnPad() {
    Entity purpSpawn = 
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/spawn_pads/SpawnPadRed.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    purpSpawn.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    purpSpawn.getComponent(TextureRenderComponent.class).scaleEntity();
    purpSpawn.scaleHeight(0.7f);
    PhysicsUtils.setScaledCollider(purpSpawn, 1f, 1f);
    return purpSpawn;
  }

  public static Entity createCeilingLight() {
    Entity ceilingLight =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/CeilingLight.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    ceilingLight.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    ceilingLight.getComponent(TextureRenderComponent.class).scaleEntity();
    ceilingLight.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(ceilingLight, 1f, 1f);
    return ceilingLight;
  }

  public static Entity createCrate() {
    Entity crate =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/Crate.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    crate.getComponent(TextureRenderComponent.class).scaleEntity();
    crate.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(crate, 1f, 1f);
    return crate;
  }

  public static Entity createLargeShelf() {
    Entity largeShelf =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/LargeShelf.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    largeShelf.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    largeShelf.getComponent(TextureRenderComponent.class).scaleEntity();
    largeShelf.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(largeShelf, 1f, 1f);
    return largeShelf;
  }

  public static Entity createLongCeilingLight() {
    Entity longCeilingLight =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/LongCeilingLight.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    longCeilingLight.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    longCeilingLight.getComponent(TextureRenderComponent.class).scaleEntity();
    longCeilingLight.scaleHeight(0.5f);
    PhysicsUtils.setScaledCollider(longCeilingLight, 1f, 1f);
    return longCeilingLight;
  }

  public static Entity createMidShelf() {
    Entity midShelf =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/MidShelf.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    midShelf.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    midShelf.getComponent(TextureRenderComponent.class).scaleEntity();
    midShelf.scaleHeight(2f);
    PhysicsUtils.setScaledCollider(midShelf, 1f, 1f);
    return midShelf;
  }

  public static Entity createOfficeChair() {
    Entity officeChair =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/OfficeChair.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    officeChair.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    officeChair.getComponent(TextureRenderComponent.class).scaleEntity();
    officeChair.scaleHeight(1.2f);
    PhysicsUtils.setScaledCollider(officeChair, 1f, 1f);
    return officeChair;
  }

  public static Entity createOfficeDesk() {
    Entity officeDesk =
        new Entity()
          .addComponent(new TextureRenderComponent("foreg_sprites/office/officeDesk.png"))
          .addComponent(new PhysicsComponent())
          .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    officeDesk.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    officeDesk.getComponent(TextureRenderComponent.class).scaleEntity();
    officeDesk.scaleHeight(1.7f);
    PhysicsUtils.setScaledCollider(officeDesk, 1f, 1f);
    return officeDesk;
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
        .addComponent(new DoorRenderComponent(Color.BLACK));
    // Make collider a sensor so it doesn't block movement
    trigger.getComponent(ColliderComponent.class).setSensor(true);
    trigger.setScale(width, height);
    return trigger;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
