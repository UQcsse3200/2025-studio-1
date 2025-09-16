package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;

/** Storage room: minimal walls with left--Research and right--Tunnel. */
public class StorageGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public StorageGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.FOREST_DEMO,
        new Color(0.12f, 0.12f, 0.10f, 0.26f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
  }

  private void spawnBordersAndDoors() {
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadResearch, this::loadShipping);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadResearch() {
    clearAndLoad(() -> new ResearchGameArea(terrainFactory, cameraComponent));
  }

  private void loadShipping() {
    clearAndLoad(() -> new ShippingGameArea(terrainFactory, cameraComponent));
  }

  @Override
  public String toString() {
    return "Storage";
  }

  @Override
  public Entity getPlayer() {
    // placeholder
    return null;
  }

  public static StorageGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {

    return (new StorageGameArea(terrainFactory, camera));
  }
}


