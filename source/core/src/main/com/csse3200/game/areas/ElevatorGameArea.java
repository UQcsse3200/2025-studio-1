package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.system.ObstacleFactory;

/** Elevator room: minimal walls and two doors (left--Office, right--Research). */
public class ElevatorGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public ElevatorGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    // Ensure the thin floor texture is available for the elevator room
    ensureTextures(new String[] { "foreg_sprites/general/ThinFloor3.png", "images/Elevator background.png" });
    // Use the dedicated elevator background
    terrain = terrainFactory.createTerrain(TerrainType.ELEVATOR);
    spawnEntity(new Entity().addComponent(terrain));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
  }

  // Assets ensured via GenericLayout

  private void spawnBordersAndDoors() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);

    addSolidWallTop(b, WALL_WIDTH);


    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY; // ground level
    float leftTopSegHeight = Math.max(0f, b.topY - (leftDoorY + leftDoorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(b.leftX, leftDoorY + leftDoorHeight);
      spawnEntity(leftTop);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadOffice));
    spawnEntity(leftDoor);


    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.bottomY; // ground level
    float rightTopSegHeight = Math.max(0f, b.topY - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(b.rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadResearch));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = com.csse3200.game.entities.factories.characters.PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadOffice() {
    clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
  }

  private void loadResearch() {
    clearAndLoad(() -> new ResearchGameArea(terrainFactory, cameraComponent));
  }

  /**
   * Override default floor spawning to use the thin floor sprite in the elevator.
   */
  @Override
  protected void spawnFloor() {
    for (int i = 0; i < 25; i += 4) {
      GridPoint2 floorspawn = new GridPoint2(i, 6);
      Entity floor = ObstacleFactory.createThinFloor();
      spawnEntityAt(floor, floorspawn, false, false);
      // Nudge down slightly to sit visually on the ground
      floor.setPosition(floor.getPosition().x, floor.getPosition().y - 0.3f);
    }
  }
}


