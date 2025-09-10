package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/** Room 6 placed to the left of Floor 3. */
public class Floor6GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor6GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    ensureAssets();
    // Choose a distinct terrain look
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_HEX);
    spawnEntity(new Entity().addComponent(terrain));

    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.15f, 0.05f, 0.2f, 0.25f)));
    spawnEntity(overlay);

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();

    }

  private void spawnBordersAndDoors() {
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallTop(b, WALL_WIDTH);
    addSolidWallBottom(b, WALL_WIDTH);
    addSolidWallLeft(b, WALL_WIDTH);
    addVerticalDoorRight(b, WALL_WIDTH, this::loadBackToFloor3);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayerWithArrowKeys();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor3() {
    if (!beginTransition()) return;
    try {
      for (Entity entity : areaEntities) { entity.dispose(); }
      areaEntities.clear();
      dispose();
      Floor3GameArea room3 = new Floor3GameArea(terrainFactory, cameraComponent);
      room3.create();
    } finally { endTransition(); }
  }

  private void ensureAssets() {
    String[] textures = new String[] {
      "images/hex_grass_1.png", "images/hex_grass_2.png", "images/hex_grass_3.png",
      "foreg_sprites/general/LongFloor.png",
      "foreg_sprites/general/ThickFloor.png",
      "foreg_sprites/general/SmallSquare.png",
      "foreg_sprites/general/SmallStair.png",
      "foreg_sprites/general/SquareTile.png"
    };
    ensureTextures(textures);
    ensurePlayerAtlas();
  }
}


