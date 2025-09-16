package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;

import javax.swing.*;

/**
 * The "Shipping" area of the game map. This class:
 * - Builds the terrain (background)
 * - Spawns the player and necessary props
 * - Generates the doors to the previous and next room
 */
public class ShippingGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  /**
   * Initialise this ShippingGameArea to use the provided TerrainFactory and camera helper.
   * The camera is used to size the screen-edge walls and place the right-side door trigger.
   *
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea (required).
   * @param cameraComponent Camera helper supplying an OrthographicCamera (optional but used here).
   * @requires terrainFactory != null
   */
  public ShippingGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
      super(terrainFactory, cameraComponent);
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player)
   * Entry point for this room. This:
   * - Loads textures
   * - Creates the terrain, walls, and UI label
   * - Spawns player, props, and enemies (to be added)
   */
  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SHIPPING,
        new Color(0.12f, 0.12f, 0.10f, 0.26f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
    spawnShipmentBoxLid();
    spawnShipmentCrane();
    spawnConveyor();

    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Shipping"));
    spawnEntity(ui);
  }

  /**
   * Creates a platform atop the boxes in the truck that has physics colliders.
   */
  private void spawnShipmentBoxLid() {
    float lidX = 7.25f;
    float lidY = 5.1f;

    Entity BoxLid = ObstacleFactory.createShipmentBoxes();
    BoxLid.setPosition(lidX, lidY);

    spawnEntity(BoxLid);
  }

  /**
   * Creates a platform atop the crane that has physics colliders.
   */
  private void spawnShipmentCrane() {
    float craneX = 8.7f;
    float craneY = 7.85f;

    Entity ShipmentCrane = ObstacleFactory.createShipmentCrane();
    ShipmentCrane.setPosition(craneX, craneY);

    spawnEntity(ShipmentCrane);
  }

  /**
   * Creates a platform atop the conveyor that has physics colliders.
   */
  private void spawnConveyor() {
    float conveyorX = 10.7f;
    float conveyorY = 8f;

    Entity Conveyor = ObstacleFactory.createConveyor();
    Conveyor.setPosition(conveyorX, conveyorY);

    spawnEntity(Conveyor);
  }

  private void spawnBordersAndDoors() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY;
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadResearch));
    spawnEntity(leftDoor);

    addSolidWallRight(b, WALL_WIDTH);

    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = 8f;
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadStorage));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  /**
   * Clears the game area and loads the previous section (Research).
   */
  private void loadResearch() {
    clearAndLoad(() -> new ResearchGameArea(terrainFactory, cameraComponent));
  }

  /**
   * Clears the game area and loads the next section (Storage).
   */
  private void loadStorage() {
    clearAndLoad(() -> new StorageGameArea(terrainFactory, cameraComponent));
  }
}