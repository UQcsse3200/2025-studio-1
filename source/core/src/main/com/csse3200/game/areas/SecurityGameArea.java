package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/** Minimal generic Security room: walls, doors, and a subtle background overlay. */
public class SecurityGameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  public SecurityGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.FOREST_DEMO,
        new Color(0.08f, 0.08f, 0.1f, 0.30f));

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();
  }

  // Assets ensured via GenericLayout

  private void spawnBordersAndDoors() {
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadBackToFloor5, this::loadOffice);
  }

  private void spawnPlayer() {
    GenericLayout.spawnArrowPlayerAt(this, PLAYER_SPAWN);
  }

  private void loadBackToFloor5() {
    clearAndLoad(() -> new Floor5GameArea(terrainFactory, cameraComponent));
  }

  private void loadOffice() {
    clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
  }

  @Override
  public String toString() {
    return "Security";
  }
}


