package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.services.SaveLoadService;

/** Office room: minimal walls and two doors (left--Security, right--Elevator). */
public class OfficeGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public OfficeGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.FOREST_DEMO,
        new Color(0.10f, 0.10f, 0.12f, 0.28f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
  }

  // Assets ensured via GenericLayout

  private void spawnBordersAndDoors() {
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadSecurity, this::loadElevator);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadSecurity() {
    clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
  }

  private void loadElevator() {
    clearAndLoad(() -> new ElevatorGameArea(terrainFactory, cameraComponent));
  }

  @Override
  public String toString() {
    return "Office";
  }

  public static OfficeGameArea load(SaveLoadService.PlayerInfo load) {
    return (new OfficeGameArea(terrainFactory, cameraComponent));
  }
}


