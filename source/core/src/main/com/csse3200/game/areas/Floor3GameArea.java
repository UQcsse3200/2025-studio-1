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

/** Room 3 with its own background styling. */
public class Floor3GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor3GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    ensureAssets();
    // Different base terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
    spawnEntity(new Entity().addComponent(terrain));

    // Distinct overlay color
    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.2f, 0.1f, 0.1f, 0.25f)));
    spawnEntity(overlay);

    spawnBordersAndReturnDoor();
    spawnPlayer();
    spawnFloor();

    float keycardX = 13f;
    float keycardY = 10f;
    Entity keycard = KeycardFactory.createKeycard(3);
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);

  }

  /** Ensure textures/atlases needed by Floor 3 are available */
  private void ensureAssets() {
    com.csse3200.game.services.ResourceService rs = com.csse3200.game.services.ServiceLocator.getResourceService();
    String[] textures = new String[] {
      "images/grass_1.png", "images/grass_2.png", "images/grass_3.png",
      "foreg_sprites/general/LongFloor.png",
      "images/keycard_lvl3.png"
    };
    java.util.List<String> toLoad = new java.util.ArrayList<>();
    for (String t : textures) {
      if (!rs.containsAsset(t, com.badlogic.gdx.graphics.Texture.class)) {
        toLoad.add(t);
      }
    }
    if (!toLoad.isEmpty()) {
      rs.loadTextures(toLoad.toArray(new String[0]));
      rs.loadAll();
    }
    if (!rs.containsAsset("images/player.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[] {"images/player.atlas"});
      rs.loadAll();
    }
  }

  private void spawnBordersAndReturnDoor() {
    Bounds b = getCameraBounds(cameraComponent);
    addVerticalDoorLeft(b, WALL_WIDTH, this::loadFloor6);
    addSolidWallRight(b, WALL_WIDTH);
    addSolidWallTop(b, WALL_WIDTH);
    addHorizontalDoorBottom(b, WALL_WIDTH, this::loadBackToFloor2);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor2() {
    clearAndLoad(() -> new Floor2GameArea(terrainFactory, cameraComponent));
  }

  private void loadFloor6() {
    clearAndLoad(() -> new Floor6GameArea(terrainFactory, cameraComponent));
  }
}


