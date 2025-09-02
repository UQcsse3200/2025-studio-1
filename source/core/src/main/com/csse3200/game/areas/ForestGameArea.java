package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.ItemHoldComponent;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.BossFactory;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
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
  private static final int NUM_ROBOTS = 1;
  private static final int NUM_ITEMS = 5;//this is for ItemFactory
  private static final int NUM_GHOST_GPTS = 4;
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
    "images/robot-2-attack.png",
    "images/robot-2-common.png",
          "images/fireball1.png",
          "images/blackhole1.png",
            "images/Robot_1.png",
            "images/Robot_1_attack_left.png",
            "images/Robot_1_attack_right.png",
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
    "images/mud.png",
    "images/heart.png"
  };
  //General Sprites for the game
  private static final String[] generalTextures = {
          "foreg_sprites/general/LongFloor.png",
          "foreg_sprites/general/Railing.png",
          "foreg_sprites/general/SmallSquare.png",
          "foreg_sprites/general/SmallStair.png",
          "foreg_sprites/general/SquareTile.png",
          "foreg_sprites/general/ThickFloor.png",
          "foreg_sprites/general/ThinFloor3.png",
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
          "foreg_sprites/futuristic/storage_crate_dark2.png"
  };
  
  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas",
    "images/robot-2.atlas", "images/fireball.atlas", "images/blackhole.atlas", "images/Robot_1.atlas",
          "images/boss_idle.atlas",
    "images/terrain_iso_grass.atlas",
    "images/ghost.atlas",
    "images/ghostKing.atlas",
    "images/ghostGPT.atlas",
    "images/explosion_1.atlas",
    "images/explosion_2.atlas"

  };
  private static final String[] forestSounds = {"sounds/Impact4.ogg"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;
  private final CameraComponent cameraComponent;

  private Entity player;
  private Entity dagger;
  private Entity lightsaber;
  private Entity bullet;
  private Entity pistol;
  private Entity rifle;

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
    ServiceLocator.registerGameArea(this);
    displayUI();
    spawnTerrain();
    player = spawnPlayer();
    dagger = spawnDagger();
    pistol = spawnPistol();
    rifle = spawnRifle();
    lightsaber = spawnLightsaber();


    //These are commented out since there is no equip feature yet
    //this.equipItem(pistol);
    //this.equipItem(lightsaber);
    //this.equipItem(dagger);
    this.equipItem(rifle);

    spawnFloor();
    spawnPad();
    spawnCrates();
    spawnPlatforms();
    spawnBottomRightDoor();
    spawnSecurityCamera();
    spawnEnergyPod();
    spawnStorageCrates();
    spawnBigWall();
    // spawnGhosts();
    // spawnGhostKing();
    int choice = (int)(Math.random() * 3);
    if (choice == 0) {
      spawnBoss2();
    } else if (choice == 1) {
      spawnRobots();
    } else {
      spawnBoss3();
    }
    spawnGhostGPT();
    playMusic();
    float keycardX = 1f;
    float keycardY = 7f;

    Entity keycard = KeycardFactory.createKeycard(1); // assuming level 1
    keycard.setPosition(new Vector2(keycardX, keycardY));
    spawnEntity(keycard);

    spawnItems();
  }

  private void spawnRobots() {
    GridPoint2 spawnPos = new GridPoint2(20, 20);

    Entity robot = NPCFactory.createRobot(player);
    spawnEntityAt(robot, spawnPos, true, true);
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"))
      .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.SPAWN_ROOM);
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

      Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
      left.setPosition(leftX, bottomY);
      spawnEntity(left);

      Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
      right.setPosition(rightX - WALL_WIDTH, bottomY);
      spawnEntity(right);

      Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
      top.setPosition(leftX, topY - WALL_WIDTH);
      spawnEntity(top);

      float doorWidth = Math.max(1f, viewWidth * 0.2f);
      float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
      float rightDoorY = camPos.y - rightDoorHeight / 2f;

      float leftSegmentWidth = Math.max(0f, (camPos.x - doorWidth/2f) - leftX);
      if (leftSegmentWidth > 0f) {
        Entity bottomLeft = ObstacleFactory.createWall(leftSegmentWidth, WALL_WIDTH);
        bottomLeft.setPosition(leftX, bottomY);
        spawnEntity(bottomLeft);
      }
      float rightSegmentStart = camPos.x + doorWidth/2f;
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
      GridPoint2 platformPos = new GridPoint2(i * 5, 11);
      Entity platform = ObstacleFactory.createThinFloor();
      spawnEntityAt(platform, platformPos, true, false);
    }

    GridPoint2 lightPos = new GridPoint2(11, 8);
    Entity longCeilingLight = ObstacleFactory.createLongCeilingLight();
    spawnEntityAt(longCeilingLight, lightPos, true, false);

    Entity officeDesk = ObstacleFactory.createOfficeDesk();
    spawnEntityAt(officeDesk, new GridPoint2(5, 12), true, false);
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
    GridPoint2 firstPos = new GridPoint2(5, 25);
    GridPoint2 secondPos = new GridPoint2(10, 25);
    GridPoint2 thirdPos = new GridPoint2(15, 25);

    spawnEntityAt(ItemFactory.createItem(), firstPos, true, false);
    spawnEntityAt(ItemFactory.createItem(), secondPos, true, false);
    spawnEntityAt(ItemFactory.createItem(), thirdPos, true, false);
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

  private Entity spawnDagger() {
    Entity newDagger = WeaponsFactory.createDagger();
    Vector2 newDaggerOffset = new Vector2(0.7f, 0.3f);
    newDagger.addComponent(new ItemHoldComponent(this.player, newDaggerOffset));
    return newDagger;
  }

  private void equipItem(Entity item) {
    this.player.setCurrItem(item);
    spawnEntityAt(item, PLAYER_SPAWN, true, true);

  }

  private Entity getItem() {
    return this.player.getCurrItem();
  }

  private Entity spawnLightsaber() {
    Entity newLightsaber = WeaponsFactory.createLightsaber();
    Vector2 newLightsaberOffset = new Vector2(0.7f, -0.1f);
    newLightsaber.addComponent(new ItemHoldComponent(this.player, newLightsaberOffset));

    //Commented out since lightsaber animation is a work in progress
    //AnimationRenderComponent lightSaberAnimator = WeaponsFactory.createAnimation("images/lightSaber.atlas", this.player);
    //newLightsaber.addComponent(lightSaberAnimator);

    return newLightsaber;
  }

//Commented out since bullet functionality is in progress with guns
//  private Entity spawnBullet() {
//    Entity newBullet = ProjectileFactory.createPistolBullet();
//    spawnEntityAt(newBullet, new GridPoint2(5, 5), true, true);
//    return newBullet;
//  }

  private Entity spawnPistol() {
    Entity newPistol = WeaponsFactory.createPistol();
    Vector2 newPistolOffset = new Vector2(0.45f, 0.02f);
    newPistol.addComponent(new ItemHoldComponent(this.player, newPistolOffset));
    return newPistol;
  }

  private Entity spawnRifle() {
    Entity newRifle = WeaponsFactory.createRifle();
    Vector2 newRifleOffset = new Vector2(0.25f, 0.15f);
    newRifle.addComponent(new ItemHoldComponent(this.player, newRifleOffset));
    return newRifle;
  }

  // Enemy Projectiles
  public Entity spawnLaserProjectile(Vector2 directionToFire) {
    Entity laser = ProjectileFactory.createLaserShot(directionToFire);
    spawnEntityAt(laser, new GridPoint2(0, 0), true, true);

    return laser;
  }

  // private void spawnGhosts() {
  //   GridPoint2 minPos = new GridPoint2(0, 0);
  //   GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

  //   for (int i = 0; i < NUM_GHOSTS; i++) {
  //     GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
  //     Entity ghost = NPCFactory.createGhost(player);
  //     spawnEntityAt(ghost, randomPos, true, true);
  //   }
  // }

  private void spawnBoss2() {
    GridPoint2 pos = new GridPoint2(22, 20);

    Entity boss2 = BossFactory.createBoss2(player);
    spawnEntityAt(boss2, pos, true, true);
  }
  //new added boss3
  private void spawnBoss3() {
    GridPoint2 pos = new GridPoint2(20, 20);
    Entity boss3 = BossFactory.createBoss3(player);
    spawnEntityAt(boss3, pos, true, true);
  }

  // private void spawnGhostKing() {
  //   GridPoint2 minPos = new GridPoint2(0, 0);
  //   GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

  //   GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
  //   Entity ghostKing = NPCFactory.createGhostKing(player);
  //   spawnEntityAt(ghostKing, randomPos, true, true);
  // }

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
    resourceService.loadTextures(futuristicTextures);
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
    resourceService.unloadAssets(futuristicTextures);
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
