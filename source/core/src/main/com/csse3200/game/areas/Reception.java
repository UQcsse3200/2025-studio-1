package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.services.SaveLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Second floor with different background and arrow-key controls. */
public class Reception extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(Reception.class);
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(8, 10);
  private static final float WALL_WIDTH = 0.1f;
  private static final int NUM_TREES = 8; // Number of trees to spawn

  public Reception(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
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
    spawndesk_reception();
    spawncomic_stand();


    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Reception"));
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
            "images/holo-clock.png",
            "images/desk_reception.png",
            "images/comics.png"
    };
    ensureTextures(needed);
    ensurePlayerAtlas();
  }

  private void spawnTerrain() {
    setupTerrainWithOverlay(terrainFactory, TerrainType.LOBBY, new Color(0.1f, 0.1f, 0.2f, 0.25f));
  }

  private void spawnWallsAndDoor() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    addSolidWallRight(b, WALL_WIDTH);
    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY;
    float leftTopSegHeight = Math.max(0f, b.topY - (leftDoorY + leftDoorHeight));
    if (leftTopSegHeight > 0f) {
      Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
      leftTop.setPosition(b.leftX, leftDoorY + leftDoorHeight);
      spawnEntity(leftTop);
    }
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadForest));
    spawnEntity(leftDoor);


    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.bottomY; // ground level
    float rightTopSegHeight = Math.max(0f, b.topY - (rightDoorY + rightDoorHeight));
    if (rightTopSegHeight > 0f) {
      Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
      rightTop.setPosition(b.rightX - WALL_WIDTH, rightDoorY + rightDoorHeight);
      spawnEntity(rightTop);
    }
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY+8f);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor5));
    spawnEntity(rightDoor);
  }

  private void loadForest() {
    clearAndLoad(() -> new ForestGameArea(terrainFactory, cameraComponent));
  }

  private void loadBackToFloor5() {
    clearAndLoad(() -> new MainHall(terrainFactory, cameraComponent));
  }

  private void spawnPlayer() {
    Entity player = PlayerFactory.createPlayer();
    spawnEntityAt(player, PLAYER_SPAWN, true, true);
  }
  private void spawnplatform2() {
    float PlatformX = 5.5f;
    float PlatformY = 3f;
    float PlatformX2 = 1f;
    float PlatformY2 = 6f;
    float PlatformX3 = 7f;
    float PlatformY3 = 7f;
    float PlatformX4 = 11f;
    float PlatformY4 = 9f;
    Entity Platform1 = ObstacleFactory.createplatform2();
    Platform1.setPosition(PlatformX, PlatformY);
    spawnEntity(Platform1);
    Entity Platform2 = ObstacleFactory.createplatform2();
    Platform2.setPosition(PlatformX2, PlatformY2);
    spawnEntity(Platform2);
    Entity Platform3 = ObstacleFactory.createplatform2();
    Platform3.setPosition(PlatformX3, PlatformY3);
    spawnEntity(Platform3);
    Entity Platform4 = ObstacleFactory.createplatform2();
    Platform4.setPosition(PlatformX4, PlatformY4);
    spawnEntity(Platform4);

  }
  /**Spawning the clock on the second platform**/
  private void spawnholoclock() {
    float PlatformX = 0.8f;
    float PlatformY = 7.45f;
    Entity clock1 = ObstacleFactory.createholoclock();
    clock1.setPosition(PlatformX, PlatformY);
    spawnEntity(clock1);
  }
  /**spawning a help desk featuring a robot to make the room look like reception **/
  private void spawndesk_reception() {
    float PlatformX = 12.5f;
    float PlatformY = 3.5f;
    Entity desk1 = ObstacleFactory.createdesk_reception();
    desk1.setPosition(PlatformX, PlatformY);
    spawnEntity(desk1);
  }
  /**spawning a comic stand near the reception desk **/
  private void spawncomic_stand() {
    float PlatformX = 11f;
    float PlatformY = 3.5f;
    Entity stand1 = ObstacleFactory.createcomic_stand();
    stand1.setPosition(PlatformX, PlatformY);
    spawnEntity(stand1);

  @Override
  public String toString() {
    return "Reception";
  }

  public static Reception load(TerrainFactory terrainFactory, CameraComponent camera) {
    return (new Reception(terrainFactory, camera));
  }

  public Entity getPlayer() {
    return null;
  }
}
}


