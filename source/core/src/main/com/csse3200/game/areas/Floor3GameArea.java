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
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;

    // Left border split with a vertical door -> Floor 6
    float leftDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float leftDoorY = camPos.y - leftDoorHeight / 2f;
    float leftTopSegHeight = Math.max(0f, (topY) - (leftDoorY + leftDoorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(leftX, leftDoorY + leftDoorHeight);
      spawnEntity(leftTop);
    }
    float leftBottomSegHeight = Math.max(0f, (leftDoorY - bottomY));
    if (leftBottomSegHeight > 0f) {
      Entity leftBottom = ObstacleFactory.createWall(WALL_WIDTH, leftBottomSegHeight);
      leftBottom.setPosition(leftX, bottomY);
      spawnEntity(leftBottom);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadFloor6));
    spawnEntity(leftDoor);
    Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    right.setPosition(rightX - WALL_WIDTH, bottomY);
    spawnEntity(right);
    Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    top.setPosition(leftX, topY - WALL_WIDTH);
    spawnEntity(top);
    // Bottom border split with a door in the middle -> back to Floor 2
    float doorWidth = Math.max(1f, viewWidth * 0.2f);
    float doorX = camPos.x - doorWidth / 2f;
    float leftSegWidth = Math.max(0f, doorX - leftX);
    if (leftSegWidth > 0f) {
      Entity bottomLeft = ObstacleFactory.createWall(leftSegWidth, WALL_WIDTH);
      bottomLeft.setPosition(leftX, bottomY);
      spawnEntity(bottomLeft);
    }
    float rightSegStart = doorX + doorWidth;
    float rightSegWidth = Math.max(0f, (leftX + viewWidth) - rightSegStart);
    if (rightSegWidth > 0f) {
      Entity bottomRight = ObstacleFactory.createWall(rightSegWidth, WALL_WIDTH);
      bottomRight.setPosition(rightSegStart, bottomY);
      spawnEntity(bottomRight);
    }
    Entity bottomDoor = ObstacleFactory.createDoorTrigger(doorWidth, WALL_WIDTH);
    bottomDoor.setPosition(doorX, bottomY + 0.001f);
    bottomDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor2));
    spawnEntity(bottomDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor2() {
    if (!beginTransition()) return;
    try {
      for (Entity entity : areaEntities) {
        entity.dispose();
      }
      areaEntities.clear();
      dispose();
      Floor2GameArea floor2 = new Floor2GameArea(terrainFactory, cameraComponent);
      floor2.create();
    } finally {
      endTransition();
    }
  }

  private void loadFloor6() {
    if (!beginTransition()) return;
    try {
      for (Entity entity : areaEntities) {
        entity.dispose();
      }
      areaEntities.clear();
      dispose();
      Floor6GameArea room6 = new Floor6GameArea(terrainFactory, cameraComponent);
      room6.create();
    } finally {
      endTransition();
    }
  }
}


