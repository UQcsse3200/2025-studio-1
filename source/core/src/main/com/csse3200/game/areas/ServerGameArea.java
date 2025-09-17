package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.components.gamearea.GameAreaDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Server Room. Has several platforms as well as server racks sprites.
 * Is attached to Tunnel Room.
 */
public class ServerGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ServerGameArea.class);

  private static final float WALL_WIDTH = 0.1f;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

  private Entity player;

  /**
   * Constructor for the Server Room, simples calls GameArea constructor.
   * @param terrainFactory the game's terrain factory (set in MainGameScreen)
   * @param cameraComponent the game's camera component (set in MainGameScreen)
   */
  public ServerGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super(terrainFactory, cameraComponent);
  }

  /**
   * The create function for the Server Room. Loads assets, displays UI, spawns terrain,
   * spawns in side wall, platforms, room objects (server racks) and spawn pads.
   * Then spawns the player in bottom left, spawns a rifle on the purple spawn pad,
   * and then spawns the floor.
   */
  @Override
  public void create() {
    ServiceLocator.registerGameArea(this);

    GenericLayout.ensureGenericAssets(this);
    GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SERVER_ROOM,
        new Color(0.10f, 0.12f, 0.10f, 0.24f));

    displayUI();
    spawnTerrain();
    spawnBigWall();
    spawnPlatforms();
    spawnRoomObjects();
    spawnCratesAndRailing();
    spawnSpawnPads();
    spawnBordersAndDoors();
    spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 21));

    spawnFloor();

    player = spawnPlayer();
    spawnGPTs();

    ItemSpawner itemSpawner = new ItemSpawner(this);
    itemSpawner.spawnItems(ItemSpawnConfig.servermap());

    Entity ui = new Entity();
    ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Server Room"));
    spawnEntity(ui);
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"))
            .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
    spawnEntity(ui);
  }

  /**
   * Getter method for the player entity
   * @return Entity player
   */
  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

  /**
   * Spawns the 3 platforms on the right side of the level.
   * 
   * Outer loop affects y level, inner loop affects x level.
   */
  private void spawnPlatforms() {

    for (int j = 10; j < 20; j += 4) {
      for (int i = 15; i <= 30; i += 5) {
        GridPoint2 platformSpawn = new GridPoint2((i + (j/3)), j);
        Entity platform = ObstacleFactory.createThinFloor();
        spawnEntityAt(platform, platformSpawn, false, false);
      }
    }
  }

  /**
   * Spwans the ambient static objects for this level, including spawn pads
   * and server racks.
   */
  private void spawnRoomObjects() {
    for (int i = 20; i < 30; i+= 1) {
      Entity rack = ObstacleFactory.createServerRack1();
      GridPoint2 rackSpawn = new GridPoint2(i, 7);
      spawnEntityAt(rack, rackSpawn, false, false);
    }
  }

  /**
   * Spawns some other objects in the room, including crates and a railing
   * accross the bottom floo
   */
  private void spawnCratesAndRailing() {
    Entity crate1 = ObstacleFactory.createCrate();
    GridPoint2 crateSpawn1 = new GridPoint2(11, 7);
    spawnEntityAt(crate1, crateSpawn1, false, false);
    Entity crate2 = ObstacleFactory.createCrate();
    GridPoint2 crateSpawn2 = new GridPoint2(13, 7);
    spawnEntityAt(crate2, crateSpawn2, false, false);
    Entity crate3 = ObstacleFactory.createCrate();
    GridPoint2 crateSpawn3 = new GridPoint2(13, 9);
    spawnEntityAt(crate3, crateSpawn3, false, false);
    
    for (int i = 0; i <= 30; i += 3) {
      Entity railing = ObstacleFactory.createRailing();
      GridPoint2 railingSpawn = new GridPoint2(i, 7);
      spawnEntityAt(railing, railingSpawn, false, false);
    }
  }

  /**
   * Spawn the spawn pads in the room. The enemy spawn pad (red spawn pad) will
   * go on the top floor, whereas the weapon spawn pad (purple spawn pad) will go
   * on the second floor.
   */
  private void spawnSpawnPads() {
    Entity spawnPad = ObstacleFactory.createRedSpawnPad();
    GridPoint2 spawnPadSpawn = new GridPoint2(25, 19);
    spawnEntityAt(spawnPad, spawnPadSpawn, false, false);
    Entity spawnPad2 = ObstacleFactory.createPurpleSpawnPad();
    GridPoint2 spawnPadSpawn2 = new GridPoint2(25, 15);
    spawnEntityAt(spawnPad2, spawnPadSpawn2, false, false);
  }

  /**
   * Spawn 2 high-level GPTs in the room as enemies.
   */
  private void spawnGPTs() {
    Entity ghost1 = NPCFactory.createGhostGPT(player, this, 2.5f);
    GridPoint2 ghost1Pos = new GridPoint2(25, 20);
    spawnEntityAt(ghost1, ghost1Pos, true, false);
    Entity ghost2 = NPCFactory.createGhostGPT(player, this, 2.5f);
    GridPoint2 ghost2Pos = new GridPoint2(25, 20);
    spawnEntityAt(ghost2, ghost2Pos, true, false);
  }

  /**
   * Adds a very tall thick-floor as a background wall/divider.
   */
  private void spawnBigWall() {
    GridPoint2 wallSpawn = new GridPoint2(-14, 0);
    Entity bigWall = ObstacleFactory.createBigThickFloor();
    spawnEntityAt(bigWall, wallSpawn, true, false);
  }

  /**
   * Builds terrain for SPAWN_ROOM and wraps the visible screen with thin physics walls
   * based on the camera viewport. Also adds a right-side door trigger that loads next level.
   */
  private void spawnTerrain() {
    // Build the ground
    terrain = terrainFactory.createTerrain(TerrainType.SERVER_ROOM);
    spawnEntity(new Entity().addComponent(terrain));

    // Build screen edges and the right-side door if a camera is available
    if (cameraComponent != null) {
      OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
      Vector2 camPos = cameraComponent.getEntity().getPosition();
      float viewWidth = cam.viewportWidth;
      float viewHeight = cam.viewportHeight;

      float leftX = camPos.x - viewWidth / 2f;
      float rightX = camPos.x + viewWidth / 2f;
      float bottomY = camPos.y - viewHeight / 2f;
      float topY = camPos.y + viewHeight / 2f;

      // Left screen border
      Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
      left.setPosition(leftX, bottomY);
      spawnEntity(left);

      // Right screen border
      Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
      right.setPosition(rightX - WALL_WIDTH, bottomY);
      spawnEntity(right);

      // Top screen border
      Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
      top.setPosition(leftX, topY - WALL_WIDTH);
      spawnEntity(top);

      // Leave a bottom gap in the middle if needed, then add a right-door trigger
      float doorWidth = Math.max(1f, viewWidth * 0.2f);
      float doorX = camPos.x - doorWidth / 2f;

      // Bottom screen border split into two segments leaving a gap for the door
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
    }
  }

  /**
   * Getter method for the player entity
   * @return Entity player
   */
  public Entity getPlayer() {
    return player;
  }

  /**
   * Spawns the borders and doors of the room.
   * Left door -> Storage, Right door -> Tunnel
   * Different to genericLayout as the right door is up high
   * at the third platfomr level.
   */
  private void spawnBordersAndDoors() {
    if (cameraComponent == null) return;
    Bounds b = getCameraBounds(cameraComponent);
    addSolidWallLeft(b, WALL_WIDTH);
    float leftDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float leftDoorY = b.bottomY;
    Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
    leftDoor.setPosition(b.leftX + 0.001f, leftDoorY);
    leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadStorage));
    spawnEntity(leftDoor);

    addSolidWallRight(b, WALL_WIDTH);

    float rightDoorHeight = Math.max(1f, b.viewHeight * 0.2f);
    float rightDoorY = b.topY - rightDoorHeight;
    Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
    rightDoor.setPosition(b.rightX - WALL_WIDTH - 0.001f, rightDoorY);
    rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadTunnel));
    spawnEntity(rightDoor);
  }

  private void loadTunnel() {
      roomNumber--;
    clearAndLoad(() -> new TunnelGameArea(terrainFactory, cameraComponent));
  }

  private void loadStorage() {
      roomNumber++;
    clearAndLoad(() -> new StorageGameArea(terrainFactory, cameraComponent));
  }


  @Override
  public String toString() {
    return "Server";
  }

  public static ServerGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
    return (new ServerGameArea(terrainFactory, camera));
  }
}