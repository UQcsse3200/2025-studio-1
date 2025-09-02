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

/**
 * Builds common static props and triggers used by the level (trees, floors, desks, crates, etc.).
 * Each method returns a ready-to-place Entity with the right components already attached.
 * Where needed we set a StaticBody so the object does not move, and assign a collider layer
 * to decide if it should block the player (OBSTACLE) or just detect overlap (DEFAULT + sensor).
 */
public class ObstacleFactory {

  /**
   * Makes a tree that the player can run into (it blocks like a rock).
   * - Texture: images/tree.png
   * - Physics: StaticBody + OBSTACLE collider (solid)
   * - Scale: slightly taller for better readability
   * @return a solid tree Entity
   */

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
    tree.scaleHeight(1f);
    PhysicsUtils.setScaledCollider(tree, 0.5f, 0.2f);
    return tree;
  }

  /**
   * Long, thin floor piece for platforms/walkways.
   * Solid so the player can stand on it.
   * @return a static floor Entity
   */
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

  /**
   * A very tall wall-like block (same art as thick floor but much taller).
   * Good for visual dividers in the background.
   * @return a tall static prop
   */
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

  /**
   * Decorative railing. Visual-only: no physics/collision so it never blocks the player.
   * @return a simple sprite Entity
   */
  public static Entity createRailing() {
    Entity railing =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/general/Railing.png"));

    railing.getComponent(TextureRenderComponent.class).scaleEntity();
    railing.scaleHeight(0.7f);
    return railing;
  }

  /**
   * Small, solid square tile useful as a step or blocker.
   * @return a static solid tile
   */
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

  /**
   * Short stair-like prop the player cannot pass through.
   * @return a static solid stair block
   */
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

  /**
   * Square floor tile, collidable so it behaves like ground.
   * @return a static square tile
   */
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

  /**
   * Regular thick floor block (short version of the big wall).
   * @return a static ground piece
   */
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
// Added the thin floor on the map where the computer is placed
  public static Entity createThinFloor() {
    Entity thinFloor =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/general/ThinFloor3.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    thinFloor.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    thinFloor.getComponent(TextureRenderComponent.class).scaleEntity();
    thinFloor.scaleHeight(0.8f);
    PhysicsUtils.setScaledCollider(thinFloor, 1f, 1f);
    return thinFloor;
  }

  /**
   * Purple spawn pad prop. Solid so it rests on the ground like other props.
   * @return a static pad entity
   */
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

  /**
   * Red spawn pad prop. Identical behaviour to the purple pad.
   * @return a static pad entity
   */
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

  /**
   * Small ceiling light prop. Solid only so it can be positioned consistently — it’s decorative.
   * @return a static light entity
   */
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

  /**
   * Wooden crate that blocks the player (useful for cover or decoration).
   * @return a static crate entity
   */
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

  /**
   * Large shelf prop that blocks movement (like furniture in the way).
   * @return a static shelf entity
   */
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

  // Added ceiling lights on under the ThinFloor
  public static Entity createLongCeilingLight() {
    Entity longCeilingLight =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/office/LongCeilingLight2.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    longCeilingLight.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    longCeilingLight.getComponent(TextureRenderComponent.class).scaleEntity();
    longCeilingLight.scaleHeight(2.7f);
    PhysicsUtils.setScaledCollider(longCeilingLight, 1f, 1f);
    return longCeilingLight;
  }

  /**
   * Mid-height shelf prop that blocks movement.
   * @return a static shelf entity
   */
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

  /**
   * Office chair prop that the player cannot pass through.
   * @return a static chair entity
   */
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

  /**
   * Front-facing office desk placed on the thin floor. Solid to act like furniture.
   * @return a static desk entity
   */
  public static Entity createOfficeDesk() {
    Entity officeDesk =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/office/officeDesk4.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    officeDesk.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    officeDesk.getComponent(TextureRenderComponent.class).scaleEntity();
    officeDesk.scaleHeight(2.0f);
    PhysicsUtils.setScaledCollider(officeDesk, 1f, 1f);
    return officeDesk;
  }

  /**
   * Security camera sprite (visual only). No physics/collider, so it never blocks the player.
   * @return a decorative camera entity
   */
  public static Entity createLargeSecurityCamera() {
    Entity cam = new Entity()
            .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/SecurityCamera3.png"));
    cam.getComponent(TextureRenderComponent.class).scaleEntity();
    cam.scaleHeight(1.9f);
    return cam;
  }

  /**
   * Glowing energy pod that acts as a solid prop on the floor.
   * Collider is slightly shorter (0.9) so it seats nicely on tiles when scaled.
   * @return a static energy pod entity
   */
  public static Entity createLargeEnergyPod() {
    Entity energyPod =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/EnergyPod.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    energyPod.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    energyPod.getComponent(TextureRenderComponent.class).scaleEntity();
    energyPod.scaleHeight(1.5f);
    PhysicsUtils.setScaledCollider(energyPod, 1f, 0.9f);
    return energyPod;
  }

  /**
   * Green futuristic storage crate.
   * @return a static crate entity (green)
   */
  public static Entity createStorageCrateGreen() {
    Entity crate =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/storage_crate_green2.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    crate.getComponent(TextureRenderComponent.class).scaleEntity();
    crate.scaleHeight(1.5f);
    PhysicsUtils.setScaledCollider(crate, 1f, 1f);
    return crate;
  }

  /**
   * Dark futuristic storage crate.
   * @return a static crate entity (dark)
   */
  public static Entity createStorageCrateDark() {
    Entity crate =
            new Entity()
                    .addComponent(new TextureRenderComponent("foreg_sprites/futuristic/storage_crate_dark2.png"))
                    .addComponent(new PhysicsComponent())
                    .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));

    crate.getComponent(PhysicsComponent.class).setBodyType(BodyType.StaticBody);
    crate.getComponent(TextureRenderComponent.class).scaleEntity();
    crate.scaleHeight(1.5f);
    PhysicsUtils.setScaledCollider(crate, 1f, 1f);
    return crate;
  }

  public static Entity createWall(float width, float height) {
    Entity wall = new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
    wall.setScale(width, height);
    return wall;
  }

  /**
   * Thin trigger line used for doors/exits.
   * - Layer: DEFAULT
   * - Sensor: true (detects overlap, does not push)
   * You can attach your own component to react on contact (e.g., change room).
   *
   * @param width world width of the trigger
   * @param height world height of the trigger
   * @return a non-blocking trigger entity
   */
  public static Entity createDoorTrigger(float width, float height) {
    Entity trigger = new Entity()
            .addComponent(new PhysicsComponent().setBodyType(BodyType.StaticBody))
            .addComponent(new ColliderComponent().setLayer(PhysicsLayer.DEFAULT))
            .addComponent(new SolidColorRenderComponent(Color.BLACK));
    trigger.getComponent(ColliderComponent.class).setSensor(true);
    trigger.setScale(width, height);
    return trigger;
  }

  private ObstacleFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
