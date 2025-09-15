package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;

/** Tunnel room: minimal walls with left door back to Storage. */
public class TunnelGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public TunnelGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.FOREST_DEMO,
        new Color(0.08f, 0.08f, 0.12f, 0.28f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
  }

  private void spawnBordersAndDoors() {
    Bounds b = getCameraBounds(cameraComponent);
    addVerticalDoorLeft(b, WALL_WIDTH, this::loadStorage);
    addSolidWallRight(b, WALL_WIDTH);
    addSolidWallTop(b, WALL_WIDTH);
    addSolidWallBottom(b, WALL_WIDTH);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadStorage() {
    clearAndLoad(() -> new StorageGameArea(terrainFactory, cameraComponent));
  }

  @Override
  public String toString() {
    return "Tunnel";
  }

}


