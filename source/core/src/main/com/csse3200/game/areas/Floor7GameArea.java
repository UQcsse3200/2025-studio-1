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

/** Room 7 placed to the left of Floor 4. */
public class Floor7GameArea extends GameArea {
  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor7GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    ensureAssets();
    // Distinct terrain look
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_ISO);
    spawnEntity(new Entity().addComponent(terrain));

    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f);
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.05f, 0.15f, 0.2f, 0.25f)));
    spawnEntity(overlay);

    spawnBordersAndDoors();
    spawnPlayer();
    spawnFloor();

  }


  private void spawnBordersAndDoors() {
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;
    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;

    // Solid borders on top/bottom and solid left
    Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    top.setPosition(leftX, topY - WALL_WIDTH);
    spawnEntity(top);
    Entity bottom = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
    bottom.setPosition(leftX, bottomY);
    spawnEntity(bottom);
    Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    left.setPosition(leftX, bottomY);
    spawnEntity(left);

    // Right border split with a vertical door -> back to Floor 4
    float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float rightDoorY = camPos.y - rightDoorHeight / 0.7f;
    float rightTopSegHeight = Math.max(0f, (topY) - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    float rightBottomSegHeight = Math.max(0f, (rightDoorY - bottomY));
    if (rightBottomSegHeight > 0f) {
      Entity rightBottom = ObstacleFactory.createWall(WALL_WIDTH, rightBottomSegHeight);
      rightBottom.setPosition(rightX - WALL_WIDTH, bottomY);
      spawnEntity(rightBottom);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor4));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayerWithArrowKeys();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadBackToFloor4() {
    if (!beginTransition()) return;
    try {
      for (Entity entity : areaEntities) { entity.dispose(); }
      areaEntities.clear();
      dispose();
      Floor4GameArea room4 = new Floor4GameArea(terrainFactory, cameraComponent);
      room4.create();
    } finally { endTransition(); }
  }

  private void ensureAssets() {
    com.csse3200.game.services.ResourceService rs = com.csse3200.game.services.ServiceLocator.getResourceService();
    String[] textures = new String[] {
      "images/iso_grass_1.png", "images/iso_grass_2.png", "images/iso_grass_3.png",
      "foreg_sprites/general/LongFloor.png",
      "foreg_sprites/general/ThickFloor.png",
      "foreg_sprites/general/SmallSquare.png",
      "foreg_sprites/general/SmallStair.png",
      "foreg_sprites/general/SquareTile.png"
    };
    java.util.List<String> toLoad = new java.util.ArrayList<>();
    for (String t : textures) if (!rs.containsAsset(t, com.badlogic.gdx.graphics.Texture.class)) toLoad.add(t);
    if (!toLoad.isEmpty()) { rs.loadTextures(toLoad.toArray(new String[0])); rs.loadAll(); }
    if (!rs.containsAsset("images/player.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[] {"images/player.atlas"}); rs.loadAll();
    }
  }
}


