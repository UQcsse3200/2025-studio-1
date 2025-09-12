package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.badlogic.gdx.graphics.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/** Second floor with different background and arrow-key controls. */
public class Floor2GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Floor2GameArea.class);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(8, 10);
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
    ensureAssets();
    spawnTerrain();
    spawnWallsAndDoor();
    spawnTrees(); // Add tree spawning
    spawnPlayer();
    spawnFloor();
    float keycardX = 13f;
    float keycardY = 10f;
    Entity keycard = KeycardFactory.createKeycard(2);
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);

    // UI label
    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 2"));
    spawnEntity(ui);
  }

  /** Ensure Floor 2 specific textures/atlases are loaded before use */
  private void ensureAssets() {
    String[] needed = new String[] {
        "images/LobbyWIP.png",
        "images/tree.png",
        "foreg_sprites/general/LongFloor.png",
        "foreg_sprites/general/ThickFloor.png",
        "foreg_sprites/general/SmallSquare.png",
        "foreg_sprites/general/SmallStair.png",
        "foreg_sprites/general/SquareTile.png",
        "images/keycard_lvl2.png"
    };
    ensureTextures(needed);
    ensurePlayerAtlas();
  }

  @Override
  public void dispose() {
    super.dispose();
    ResourceService rs = ServiceLocator.getResourceService();
    // Keep shared assets (like player atlas) loaded; unload only local visuals.
    rs.unloadAssets(new String[] {
        "images/LobbyWIP.png",
        "images/tree.png",
        "foreg_sprites/general/LongFloor.png",
        "foreg_sprites/general/ThickFloor.png",
        "foreg_sprites/general/SmallSquare.png",
        "foreg_sprites/general/SmallStair.png",
        "foreg_sprites/general/SquareTile.png",
        "images/keycard_lvl2.png"
    });
  }

  private void spawnTerrain() {
    setupTerrainWithOverlay(terrainFactory, TerrainType.LOBBY, new Color(0.1f, 0.1f, 0.2f, 0.25f));
  }

  private void spawnWallsAndDoor() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    addSolidWallRight(b, WALL_WIDTH);
    addHorizontalDoorTop(b, WALL_WIDTH, this::loadRoom3);

    // Bottom door slightly above ground
    float doorWidth = Math.max(1f, b.viewWidth * 0.2f);
    float doorX = b.camPos.x - doorWidth / 2f;
    float leftSegWidth = Math.max(0f, doorX - b.leftX);
    if (leftSegWidth > 0f) {
      Entity bottomLeft = ObstacleFactory.createWall(leftSegWidth, WALL_WIDTH);
      bottomLeft.setPosition(b.leftX, b.bottomY);
      spawnEntity(bottomLeft);
    }
    float rightSegStart = doorX + doorWidth;
    float rightSegWidth = Math.max(0f, (b.leftX + b.viewWidth) - rightSegStart);
    if (rightSegWidth > 0f) {
      Entity bottomRight = ObstacleFactory.createWall(rightSegWidth, WALL_WIDTH);
      bottomRight.setPosition(rightSegStart, b.bottomY);
      spawnEntity(bottomRight);
    }
    Entity bottomDoor = ObstacleFactory.createDoorTrigger(doorWidth, WALL_WIDTH);
    bottomDoor.setPosition(doorX, b.bottomY + 0.1f);
    bottomDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadPreviousLevel));
    spawnEntity(bottomDoor);

    // Left vertical door resting on ground level
    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY; // ground level
    float leftTopSegHeight = Math.max(0f, b.topY - (leftDoorY + leftDoorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(b.leftX, leftDoorY + leftDoorHeight);
      spawnEntity(leftTop);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadRoom4));
    spawnEntity(leftDoor);

    // Right vertical door resting on ground level
    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.bottomY; // ground level
    float rightTopSegHeight = Math.max(0f, b.topY - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(b.rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadRoom5));
    spawnEntity(rightDoor);
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayerWithArrowKeys();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

  private void loadPreviousLevel() {
    // Keep the special ghost atlas handling as is, before delegating
    com.csse3200.game.services.ResourceService rs = ServiceLocator.getResourceService();
    if (!rs.containsAsset("images/ghost.atlas", com.badlogic.gdx.graphics.g2d.TextureAtlas.class)) {
      rs.loadTextureAtlases(new String[]{"images/ghost.atlas", "images/ghostKing.atlas"});
      rs.loadAll();
    }
    clearAndLoad(() -> new ForestGameArea(terrainFactory, cameraComponent));
  }

  private void loadRoom3() {
    clearAndLoad(() -> new Floor3GameArea(terrainFactory, cameraComponent));
  }

  private void loadRoom4() {
    clearAndLoad(() -> new Floor4GameArea(terrainFactory, cameraComponent));
  }

  private void loadRoom5() {
    clearAndLoad(() -> new Floor5GameArea(terrainFactory, cameraComponent));
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


