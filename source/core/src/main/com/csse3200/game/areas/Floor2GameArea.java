package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.InventoryComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Second floor with different background and arrow-key controls. */
public class Floor2GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Floor2GameArea.class);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
  private static final float WALL_WIDTH = 0.1f;
  private static final int NUM_TREES = 8; // Number of trees to spawn

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  public Floor2GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  @Override
  public void create() {
    spawnTerrain();
    spawnWallsAndDoor();
    spawnTrees(); // Add tree spawning
    spawnPlayer();
    spawnFloor();

    float keycardX = 2f;
    float keycardY = 12f;
    Entity keycard = KeycardFactory.createKeycard(2);
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);
  }

  private void spawnTerrain() {
    // Use a different terrain/tileset as a base
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO_HEX);
    spawnEntity(new Entity().addComponent(terrain));

    // Add a semi-transparent overlay to tint background color differently
    Entity overlay = new Entity();
    overlay.setScale(1000f, 1000f); // large overlay
    overlay.setPosition(-500f, -500f);
    overlay.addComponent(new SolidColorRenderComponent(new Color(0.1f, 0.1f, 0.2f, 0.25f)));
    spawnEntity(overlay);
  }

  private void spawnWallsAndDoor() {
    if (cameraComponent == null) return;
    OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
    Vector2 camPos = cameraComponent.getEntity().getPosition();
    float viewWidth = cam.viewportWidth;
    float viewHeight = cam.viewportHeight;

    float leftX = camPos.x - viewWidth / 2f;
    float rightX = camPos.x + viewWidth / 2f;
    float bottomY = camPos.y - viewHeight / 2f;
    float topY = camPos.y + viewHeight / 2f;

    // Borders
    Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    left.setPosition(leftX, bottomY);
    spawnEntity(left);

    Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
    right.setPosition(rightX - WALL_WIDTH, bottomY);
    spawnEntity(right);

    // Top border split with a door in the middle -> Room 3
    float topDoorWidth = Math.max(1f, viewWidth * 0.2f);
    float topDoorX = camPos.x - topDoorWidth / 2f;
    float topLeftSegWidth = Math.max(0f, topDoorX - leftX);
    if (topLeftSegWidth > 0f) {
      Entity topLeft = ObstacleFactory.createWall(topLeftSegWidth, WALL_WIDTH);
      topLeft.setPosition(leftX, topY - WALL_WIDTH);
      spawnEntity(topLeft);
    }
    float topRightStart = topDoorX + topDoorWidth;
    float topRightSegWidth = Math.max(0f, (leftX + viewWidth) - topRightStart);
    if (topRightSegWidth > 0f) {
      Entity topRight = ObstacleFactory.createWall(topRightSegWidth, WALL_WIDTH);
      topRight.setPosition(topRightStart, topY - WALL_WIDTH);
      spawnEntity(topRight);
    }
    Entity topDoor = ObstacleFactory.createDoorTrigger(topDoorWidth, WALL_WIDTH);
    topDoor.setPosition(topDoorX, topY - WALL_WIDTH + 0.001f);
    topDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadRoom3, 1));
    spawnEntity(topDoor);

    // Bottom border split with a door in the middle -> Back to Floor 1
    float doorWidth = Math.max(1f, viewWidth * 0.2f);
    float doorHeight = WALL_WIDTH;
    float doorX = camPos.x - doorWidth / 2f;

    float leftSegmentWidth = Math.max(0f, doorX - leftX);
    if (leftSegmentWidth > 0f) {
      Entity bottomLeft = ObstacleFactory.createWall(leftSegmentWidth, WALL_WIDTH);
      bottomLeft.setPosition(leftX, bottomY);
      spawnEntity(bottomLeft);
    }
    float rightSegmentStart = doorX + doorWidth;
    float rightSegmentWidth = Math.max(0f, (leftX + viewWidth) - rightSegmentStart);
    if (rightSegmentWidth > 0f) {
      Entity bottomRight = ObstacleFactory.createWall(rightSegmentWidth, WALL_WIDTH);
      bottomRight.setPosition(rightSegmentStart, bottomY);
      spawnEntity(bottomRight);
    }

    Entity bottomDoor = ObstacleFactory.createDoorTrigger(doorWidth, doorHeight);
    bottomDoor.setPosition(doorX, bottomY + 0.001f);
    bottomDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadPreviousLevel, 0));
    spawnEntity(bottomDoor);

    // Left border split with a vertical door -> Room 4
    float leftDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float leftDoorY = camPos.y - leftDoorHeight / 0.4f;
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
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadRoom4, 1));
    spawnEntity(leftDoor);

    // Right border split with a vertical door -> Room 5
    float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
    float rightDoorY = camPos.y - rightDoorHeight / 0.4f;
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
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadRoom5, 1));
    spawnEntity(rightDoor);
  }



  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayerWithArrowKeys();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  public void loadPreviousLevel() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();

    // Ensure ghost atlases are loaded before recreating Floor 1
    ResourceService rs = ServiceLocator.getResourceService();
    if (!rs.containsAsset("images/ghost.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[]{"images/ghost.atlas", "images/ghostKing.atlas"});
      rs.loadAll();
    }

    ForestGameArea floor1 = new ForestGameArea(terrainFactory, cameraComponent);
    floor1.create();
  }



  private void loadRoom3() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor3GameArea room3 = new Floor3GameArea(terrainFactory, cameraComponent);
    room3.create();
  }

  private void loadRoom4() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor4GameArea room4 = new Floor4GameArea(terrainFactory, cameraComponent);
    room4.create();
  }

  private void loadRoom5() {
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();
    Floor5GameArea room5 = new Floor5GameArea(terrainFactory, cameraComponent);
    room5.create();
  }

  private void spawnTrees() {
    // Spawn trees in fixed positions around the map
    GridPoint2[] treePositions = {
        new GridPoint2(5, 15),   // Top left area
        new GridPoint2(15, 15),  // Top right area
        new GridPoint2(5, 5),    // Bottom left area
        new GridPoint2(15, 5),   // Bottom right area
        new GridPoint2(10, 20),  // Top center
        new GridPoint2(10, 0),   // Bottom center
        new GridPoint2(0, 10),   // Left center
        new GridPoint2(20, 10)   // Right center
    };

    for (int i = 0; i < NUM_TREES && i < treePositions.length; i++) {
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, treePositions[i], true, false);
    }
  }
}


