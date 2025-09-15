package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Second floor with different background and arrow-key controls. */
public class Floor2GameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Floor2GameArea.class);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(8, 10);
  private static final float WALL_WIDTH = 0.1f;

  public Floor2GameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  @Override
  public void create() {
    ensureAssets();
    spawnTerrain();
    spawnWallsAndDoor();
    spawnPlayer();
    spawnFloor();
    spawnholoclock();
    spawnplatform2();
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
            "images/background-reception.png",
            "images/tree.png",
            "foreg_sprites/general/LongFloor.png",
            "foreg_sprites/general/ThickFloor.png",
            "foreg_sprites/general/SmallSquare.png",
            "foreg_sprites/general/SmallStair.png",
            "foreg_sprites/general/SquareTile.png",
            "images/keycard_lvl2.png",
            "images/platform-2.png",
            "images/holo-clock.png"

    };
    ensureTextures(needed);
    ensurePlayerAtlas();
  }

  // Removed area-specific dispose to avoid double disposal during transitions

  private void spawnTerrain() {
    setupTerrainWithOverlay(terrainFactory, TerrainType.LOBBY, new Color(0.1f, 0.1f, 0.2f, 0.25f));
  }

  private void spawnWallsAndDoor() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    addSolidWallRight(b, WALL_WIDTH);

    // Left vertical door
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
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(() -> loadArea(ForestGameArea.class)));
    spawnEntity(leftDoor);

    // Right vertical door
    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.bottomY; // ground level
    float rightTopSegHeight = Math.max(0f, b.topY - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(b.rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY+7f);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(() -> loadArea(Floor5GameArea.class)));
    spawnEntity(rightDoor);
  }
  private void spawnplatform2() {
    float PlatformX = 0f;
    float PlatformY = 3.5f;
    float PlatformX2 = 3f;
    float PlatformY2 = 7f;
    float PlatformX3 = 9f;
    float PlatformY3 = 8f;
    Entity Platform1 = ObstacleFactory.createplatform2();
    Platform1.setPosition(PlatformX, PlatformY);
    spawnEntity(Platform1);
    Entity Platform2 = ObstacleFactory.createplatform2();
    Platform2.setPosition(PlatformX2, PlatformY2);
    spawnEntity(Platform2);
    Entity Platform3 = ObstacleFactory.createplatform2();
    Platform3.setPosition(PlatformX3, PlatformY3);
    spawnEntity(Platform3);

  }
  private void spawnholoclock() {
    float PlatformX = 10f;
    float PlatformY = 3.5f;
    Entity clock1 = ObstacleFactory.createholoclock();
    clock1.setPosition(PlatformX, PlatformY);
    spawnEntity(clock1);
  }
  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }

}



