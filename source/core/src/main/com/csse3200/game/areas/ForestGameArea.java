package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;

import javax.naming.spi.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final int NUM_TREES = 7;
  private static final int NUM_GHOSTS = 0;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(3, 7);
  private static final float WALL_WIDTH = 0.1f;
  private static final String[] forestTextures = {
    "images/box_boy_leaf.png",
    "images/tree.png",
    "images/ghost_king.png",
    "images/ghost_1.png",
    "images/grass_1.png",
    "images/grass_2.png",
    "images/grass_3.png",
    "images/hex_grass_1.png",
    "images/hex_grass_2.png",
    "images/hex_grass_3.png",
    "images/iso_grass_1.png",
    "images/iso_grass_2.png",
    "images/iso_grass_3.png",
    "images/Spawn.png",
    "images/SpawnResize.png",
    "images/LobbyWIP.png",
          "images/door.png"
  };
  //General Sprites for the game
  private static final String[] generalTextures = {
      "foreg_sprites/general/LongFloor.png",
      "foreg_sprites/general/Railing.png",
      "foreg_sprites/general/SmallSquare.png",
      "foreg_sprites/general/SmallStair.png",
      "foreg_sprites/general/SquareTile.png",
      "foreg_sprites/general/ThickFloor.png",
      "foreg_sprites/general/ThinFloor.png",
  };
  private static final String[] spawnPadTextures = {
      "foreg_sprites/spawn_pads/SpawnPadPurple.png",
      "foreg_sprites/spawn_pads/SpawnPadRed.png",
  };
  private static final String[] officeTextures = {
          "foreg_sprites/office/CeilingLight.png",
          "foreg_sprites/office/Crate.png",
          "foreg_sprites/office/LargeShelf.png",
          "foreg_sprites/office/MidShelf.png",
          "foreg_sprites/office/LongCeilingLight2.png",
          "foreg_sprites/office/OfficeChair.png",
          "foreg_sprites/office/officeDesk4.png",

  };
  private static final String[] futuristicTextures = {
          "foreg_sprites/futuristic/SecurityCamera3.png",
          "foreg_sprites/futuristic/EnergyPod.png",
          "foreg_sprites/futuristic/storage_crate_green2.png",
          "foreg_sprites/futuristic/storage_crate_dark2.png",
  };
  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas"
  };
  private static final String[] forestSounds = {"sounds/Impact4.ogg"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  private Entity player;

  /**
   * Initialise this ForestGameArea to use the provided TerrainFactory.
   * @param terrainFactory TerrainFactory used to create the terrain for the GameArea.
   * @requires terrainFactory != null
   */
  public ForestGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
    super();
    this.terrainFactory = terrainFactory;
    this.cameraComponent = cameraComponent;
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
  @Override
  public void create() {
    loadAssets();

    displayUI();

    spawnTerrain();
    player = spawnPlayer();

    spawnFloor();
    spawnPad();
    spawnCrates();
    spawnPlatforms();
    spawnBottomRightDoor();
    spawnSecurityCamera();
    spawnEnergyPod();
    spawnStorageCrates();
    spawnBigWall();

    playMusic();
    float keycardX = 1f;
    float keycardY = 7f;

    Entity keycard = KeycardFactory.createKeycard(1); // assuming level 1
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);

  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"))
      .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
    spawnEntity(new Entity().addComponent(terrain));

    // Screen walls (camera viewport bounds) and a simple door trigger at the bottom center
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

      // Door trigger: a thin black line at bottom center
      float doorWidth = Math.max(1f, viewWidth * 0.2f);
      float doorHeight = WALL_WIDTH;
      float doorX = camPos.x - doorWidth / 2f;
      float doorY = bottomY + 0.001f; // slight offset to sit above border
      float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
      float rightDoorY = camPos.y - rightDoorHeight / 2f;

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

      Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
      rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
      rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(() -> this.loadNextLevel()));

    }
  }


  private void loadNextLevel() {
    // Dispose current floor and switch to Floor2GameArea
    for (Entity entity : areaEntities) {
      entity.dispose();
    }
    areaEntities.clear();

    Floor2GameArea floor2 = new Floor2GameArea(terrainFactory, cameraComponent);
    floor2.create();
  }

  private void spawnTrees() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_TREES; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, randomPos, true, false);
    }
  }

  private void spawnPlatforms() {
    for (int i = 0; i < 3; i++) {
      GridPoint2 platformPos = new GridPoint2(i * 5, 10);
      Entity platform = ObstacleFactory.createThinFloor();
      spawnEntityAt(platform, platformPos, true, false);
    }

    GridPoint2 lightPos = new GridPoint2(9, 9);
    Entity longCeilingLight = ObstacleFactory.createLongCeilingLight();
    spawnEntityAt(longCeilingLight, lightPos, true, false);

    Entity officeDesk = ObstacleFactory.createOfficeDesk();
    spawnEntityAt(officeDesk, new GridPoint2(5, 11), true, false);
  }
  private void spawnBottomRightDoor() {
    float doorX = 14f;
    float doorY = 3f;

    Entity door = ObstacleFactory.createDoorTrigger(20f, 40f);
    TextureRenderComponent texture = new TextureRenderComponent("images/door.png");
    door.addComponent(texture);
    texture.scaleEntity();
    door.setPosition(doorX, doorY);
    door.addComponent(new KeycardGateComponent(1, () -> {
      logger.info("Bottom-right platform door unlocked — loading next level");
      loadNextLevel();
    }));

    spawnEntity(door);
  }

  private void spawnPad() {
    GridPoint2 spawnPadPos = new GridPoint2(20, 3);

    Entity spawnPad = ObstacleFactory.createPurpleSpawnPad();

    spawnEntityAt(spawnPad, spawnPadPos, false, false);
  }

  private void spawnBigWall() {
    GridPoint2 wallSpawn = new GridPoint2(-14, 0);
    Entity bigWall = ObstacleFactory.createBigThickFloor();
    spawnEntityAt(bigWall, wallSpawn, true, false);
  }
  /**
   * Spawns several item entities at random positions in the game area.
   * The number of items is set by NUM_ITEMS.
   * Each item is created and placed at a random spot on the terrain.
   */
  private void spawnItems() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_ITEMS; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity item = ItemFactory.createItem();
      spawnEntityAt(item, randomPos, true, false);
    }
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

  private void spawnGhosts() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_GHOSTS; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity ghost = NPCFactory.createGhost(player);
      spawnEntityAt(ghost, randomPos, true, true);
    }
  }

  private void spawnGhostKing() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
    Entity ghostKing = NPCFactory.createGhostKing(player);
    spawnEntityAt(ghostKing, randomPos, true, true);
  }

  /**
   * Adds NUM_GHOST_GPTS amount of GhostGPT enemies onto the map.
   */
  // private void spawnGhostGPT() {
  //   GridPoint2 minPos = new GridPoint2(0, 0);
  //   GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

  //   for (int i = 0; i < NUM_GHOST_GPTS; i++) {
  //       GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
  //       Entity ghostGPT = NPCFactory.createGhostGPT(player, this);
  //       spawnEntityAt(ghostGPT, randomPos, true, true);
  //   }
  // }

  private void spawnGhostGPT() {
    GridPoint2 spawn1 = new GridPoint2(20, 20);
    GridPoint2 spawn2 = new GridPoint2(25, 20);

    Entity ghostGPT = NPCFactory.createGhostGPT(player, this);
    spawnEntityAt(ghostGPT, spawn1, true, true);
    Entity ghostGPT2 = NPCFactory.createGhostGPT(player, this);
    spawnEntityAt(ghostGPT2, spawn2, true, true);
  }

  private void spawnCrates() {
    GridPoint2 cratePos = new GridPoint2(17, 6);
    Entity crate = ObstacleFactory.createCrate();
    spawnEntityAt(crate, cratePos, true, false);
  }

//Spawning Camera to the right top of map
  private void spawnSecurityCamera() {
    GridPoint2 cameraPos = new GridPoint2(27, 19);
    Entity securityCamera = ObstacleFactory.createLargeSecurityCamera();
    spawnEntityAt(securityCamera, cameraPos, true, false);
  }
//Adding Energy pod sprite at the floor
  private void spawnEnergyPod() {
    GridPoint2 energyPodPos = new GridPoint2(20, 6);
    Entity energyPod = ObstacleFactory.createLargeEnergyPod();
    spawnEntityAt(energyPod, energyPodPos, false, false);
  }

  //Two Storage crates sprites on the floor
  private void spawnStorageCrates() {
    // Green crate
    GridPoint2 greenCratePos = new GridPoint2(5, 5);
    Entity greenCrate = ObstacleFactory.createStorageCrateGreen();
    spawnEntityAt(greenCrate, greenCratePos, true, false);
    greenCrate.setPosition(greenCrate.getPosition().x, greenCrate.getPosition().y + 0.25f);
    // Dark crate
    GridPoint2 darkCratePos = new GridPoint2(26, 5);
    Entity darkCrate = ObstacleFactory.createStorageCrateDark();
    spawnEntityAt(darkCrate, darkCratePos, true, false);
    darkCrate.setPosition(darkCrate.getPosition().x, darkCrate.getPosition().y + 0.25f);
  }

  private void playMusic() {
    Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
    music.setLooping(true);
    music.setVolume(0.3f);
    music.play();
  }

  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(generalTextures);
    resourceService.loadTextures(forestTextures);
    resourceService.loadTextures(spawnPadTextures);
    resourceService.loadTextures(officeTextures);
    resourceService.loadTextureAtlases(forestTextureAtlases);
    resourceService.loadSounds(forestSounds);
    resourceService.loadMusic(forestMusic);

    while (!resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(forestTextures);
    resourceService.unloadAssets(generalTextures);
    resourceService.unloadAssets(forestTextureAtlases);
    resourceService.unloadAssets(forestSounds);
    resourceService.unloadAssets(forestMusic);
    resourceService.unloadAssets(spawnPadTextures);
    resourceService.unloadAssets(officeTextures);
  }

  @Override
  public void dispose() {
    super.dispose();
    ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
    this.unloadAssets();
  }

  public Entity getPlayer() {
    return player;
  }
}
