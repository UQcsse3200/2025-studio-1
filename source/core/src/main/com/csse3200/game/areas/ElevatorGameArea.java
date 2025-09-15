package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;

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
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.FOREST_DEMO,
        new Color(0.12f, 0.10f, 0.10f, 0.26f));

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

  @Override
  public String toString() {
    return "Elevator";
  }
}


