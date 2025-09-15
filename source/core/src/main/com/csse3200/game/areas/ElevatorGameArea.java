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
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadOffice, this::loadResearch);
  }

  private void spawnPlayer() {
    GenericLayout.spawnArrowPlayerAt(this, PLAYER_SPAWN);
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


