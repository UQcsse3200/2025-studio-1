package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.ItemHoldComponent;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
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

  /** Files or pictures used by the game (enemy/props,etc.). */
  private static final String HEART = "images/heart.png";

  /** Server Room background images */
  private static final String[] serverBackground = {
    "images/ServerRoomBackground.png",
    "images/ServerRoomBackgroundResize.png",
  };

  /** Server Room server rack sprites + vent */
  private static final String[] serverRacks = {
    "foreg_sprites/furniture/ServerRack.png",
    "foreg_sprites/furniture/ServerRack2.png",
    "foreg_sprites/furniture/Vent.png",
  };

  /** 'Forest Textures' contains important weapon and NPC sprites */
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
    "images/robot-2-attack.png",
    "images/robot-2-common.png",
    "images/fireball1.png",
    "images/blackhole1.png",
    "images/Robot_1.png",
    "images/Robot_1_attack_Right.png",
    "images/Boss_3.png",
    "images/mud.png",
    "images/mud_ball_1.png",
    "images/mud_ball_2.png",
    "images/mud_ball_3.png",
    "images/lightsaber.png",
    "images/lightsaberSingle.png",
    "images/ammo.png",
    "images/round.png",
    "images/pistol.png",
    "images/rifle.png",
    "images/dagger.png",
    "images/laser_shot.png",
    "images/Spawn.png",
    "images/SpawnResize.png",
    "images/LobbyWIP.png",
    "images/door.png",
    "images/KeycardDoor.png",
    "images/player.png",
    "images/mud.png",
    HEART,
    "images/MarblePlatform.png",
    "images/computerBench.png",
  };

  /** General prop textures (floors, tiles, etc.). */
  private static final String[] generalTextures = {
    "foreg_sprites/general/LongFloor.png",
    "foreg_sprites/general/Railing.png",
    "foreg_sprites/general/SmallSquare.png",
    "foreg_sprites/general/SmallStair.png",
    "foreg_sprites/general/SquareTile.png",
    "foreg_sprites/general/ThickFloor.png",
    "foreg_sprites/general/ThinFloor.png",
    "foreg_sprites/general/ThinFloor2.png",
    "foreg_sprites/general/ThinFloor3.png",
    "foreg_sprites/general/Test.png",
    "foreg_sprites/office/Crate.png",
  };

  /** Spawn pad textures. */
  private static final String[] spawnPadTextures = {
    "foreg_sprites/spawn_pads/SpawnPadPurple.png",
    "foreg_sprites/spawn_pads/SpawnPadRed.png",
  };

  /** Futuristic props used in this room (camera, energy pod, crates). */
  private static final String[] futuristicTextures = {
    "foreg_sprites/futuristic/SecurityCamera3.png",
    "foreg_sprites/futuristic/EnergyPod.png",
    "foreg_sprites/futuristic/storage_crate_green2.png",
    "foreg_sprites/futuristic/storage_crate_dark2.png",
  };

  /** keycard textures  */
  private static final String[] keycardTextures = {
    "images/keycard_lvl1.png",
    "images/keycard_lvl2.png",
    "images/keycard_lvl3.png",
    "images/keycard_lvl4.png",
  };

  /** Texture atlases for animated entities */
  private static final String[] forestTextureAtlases = {
    "images/robot-2.atlas", "images/fireball.atlas", "images/blackhole.atlas", "images/Robot_1.atlas",
    "images/boss_idle.atlas",
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas",
    "images/ghostGPT.atlas",
    "images/Deepspin.atlas",
    "images/Grokdroid.atlas",
    "images/Vroomba.atlas",
    "images/explosion_1.atlas",
    "images/explosion_2.atlas",
    "images/player.atlas",
    "images/player.atlas",
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas",
    "images/ghostGPT.atlas",
    "images/explosion_1.atlas",
    "images/explosion_2.atlas",
  };


  private static final String[] playerSound1 = {"sounds/jump.mp3"};
  private static final String[] forestSounds = {"sounds/Impact4.ogg"};
  private static final String[] enemySounds = {"sounds/shock.mp3", "sounds/pop.mp3"};

  private static final String BACKGROUND_MUSIC = "sounds/BGM_03.mp3";

  private static final String[] forestMusic = {BACKGROUND_MUSIC};

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

    loadAssets();
    displayUI();
    spawnTerrain();
    spawnBigWall();
    spawnPlatforms();
    spawnRoomObjects();
    spawnCratesAndRailing();
    spawnSpawnPads();
    spawnBordersAndDoors();

    spawnFloor();
    player = spawnPlayer();

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
   * Adds a very tall thick-floor as a background wall/divider.
   */
  private void spawnBigWall() {
    GridPoint2 wallSpawn = new GridPoint2(-14, 0);
    Entity bigWall = ObstacleFactory.createBigThickFloor();
    spawnEntityAt(bigWall, wallSpawn, true, false);
  }


  /**
   * Spawns a rifle on top of the purple spawn pad.
   * @return Entity rifle
   */
  private Entity spawnRifle() {
    Entity newRifle = WeaponsFactory.createWeapon(Weapons.RIFLE);
    Vector2 newRifleOffset = new Vector2(0.25f, 0.15f);
    newRifle.addComponent(new ItemHoldComponent(this.player, newRifleOffset));
    return newRifle;
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
   * Loads all textures, atlases, sounds and music needed by this room.
   * Blocks briefly until loading is complete. If you add new art, put it here.
   */
  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(serverBackground);
    resourceService.loadTextures(futuristicTextures);
    resourceService.loadTextures(keycardTextures);
    resourceService.loadTextures(generalTextures);
    resourceService.loadTextures(forestTextures);
    resourceService.loadTextures(spawnPadTextures);
    resourceService.loadTextures(serverRacks);
    resourceService.loadTextureAtlases(forestTextureAtlases);
    resourceService.loadSounds(playerSound1);
    resourceService.loadSounds(forestSounds);
    resourceService.loadSounds(enemySounds);
    resourceService.loadMusic(forestMusic);

    while (resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /**
   * Getter method for the player entity
   * @return Entity player
   */
  public Entity getPlayer() {
    return player;
  }

  private void spawnBordersAndDoors() {
    GenericLayout.addLeftRightDoorsAndWalls(this, cameraComponent, WALL_WIDTH,
        this::loadStorage, this::loadTunnel);
  }

  private void loadTunnel() {
      roomNumber--;
    clearAndLoad(() -> new TunnelGameArea(terrainFactory, cameraComponent));
  }

  private void loadStorage() {
      roomNumber++;
    clearAndLoad(() -> new StorageGameArea(terrainFactory, cameraComponent));
  }
}