package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.ItemHoldComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.*;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Forest area for the demo game with trees, a player, and some enemies. */
public class ForestGameArea extends GameArea {
  private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
  private static final int NUM_TREES = 7;
  private static final int NUM_ROBOTS = 1;
  private static final int NUM_ITEMS = 5;//this is for ItemFactory
  private static final int NUM_GHOSTS = 1;
  private static final int NUM_GHOST_GPTS = 1;
  private static final int NUM_DEEP_SPIN = 1;
  private static final int NUM_GROK_DROID = 1;
  private static final int NUM_VROOMBA = 1;
  private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
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
    "images/player.png",
    "images/mud.png",
    "images/heart.png"
  };

  private static final String[] forestTextureAtlases = {
    "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas",
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
    "images/explosion_2.atlas",
    "images/player.atlas"

  };
  private static final String[] forestSounds = {"sounds/Impact4.ogg"};
  private static final String backgroundMusic = "sounds/BGM_03_mp3.mp3";
  private static final String[] forestMusic = {backgroundMusic};

  private final TerrainFactory terrainFactory;

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
  public ForestGameArea(TerrainFactory terrainFactory) {
    super();
    this.terrainFactory = terrainFactory;
  }

  /** Create the game area, including terrain, static entities (trees), dynamic entities (player) */
  @Override
  public void create() {

    loadAssets();
    ServiceLocator.registerGameArea(this);
    displayUI();
    spawnTerrain();
    spawnTrees();

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

    spawnGhosts();
    spawnGhostKing();
    int choice = (int)(Math.random() * 3);
    if (choice == 0) {
      spawnBoss2();
    } else if (choice == 1) {
      spawnRobots();
    } else {
      spawnBoss3();
    }
    spawnGhostGPT();
    spawnDeepspin();
    spawnGrokDroid();
    spawnVroomba();
    playMusic();
    spawnItems();
  }

  private void spawnRobots() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_ROBOTS; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity robot = NPCFactory.createRobot(player);
      spawnEntityAt(robot, randomPos, true, true);
    }
  }

  private void displayUI() {
    Entity ui = new Entity();
    ui.addComponent(new GameAreaDisplay("Box Forest"));
    spawnEntity(ui);
  }

  private void spawnTerrain() {
    // Background terrain
    terrain = terrainFactory.createTerrain(TerrainType.FOREST_DEMO);
    spawnEntity(new Entity().addComponent(terrain));

    // Terrain walls
    float tileSize = terrain.getTileSize();
    GridPoint2 tileBounds = terrain.getMapBounds(0);
    Vector2 worldBounds = new Vector2(tileBounds.x * tileSize, tileBounds.y * tileSize);

    // Left
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y), GridPoint2Utils.ZERO, false, false);
    // Right
    spawnEntityAt(
        ObstacleFactory.createWall(WALL_WIDTH, worldBounds.y),
        new GridPoint2(tileBounds.x, 0),
        false,
        false);
    // Top
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH),
        new GridPoint2(0, tileBounds.y),
        false,
        false);
    // Bottom
    spawnEntityAt(
        ObstacleFactory.createWall(worldBounds.x, WALL_WIDTH), GridPoint2Utils.ZERO, false, false);
  }

  private void spawnTrees() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0);

    for (int i = 0; i < NUM_TREES; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      randomPos.y = 2;
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, randomPos, true, false);
    }
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
      Entity item = ItemFactory.createItem("images/heart.png");
      spawnEntityAt(item, randomPos, true, false);
    }
  }

  private Entity spawnPlayer() {
    Entity newPlayer = PlayerFactory.createPlayer();
    spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
    return newPlayer;
  }

  private Entity spawnDagger() {
    Entity newDagger = WeaponsFactory.createWeapon(Weapons.DAGGER);
    Vector2 newDaggerOffset = new Vector2(0.7f, 0.3f);
    newDagger.addComponent(new ItemHoldComponent(this.player, newDaggerOffset));

    return newDagger;
  }

  private void equipItem(Entity item) {
    InventoryComponent inventory = this.player.getComponent(InventoryComponent.class);
    inventory.addItem(item);
    spawnEntityAt(item, PLAYER_SPAWN, true, true);
  }

  private Entity spawnLightsaber() {
    Entity newLightsaber = WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
    Vector2 newLightsaberOffset = new Vector2(0.7f, -0.1f);
    newLightsaber.addComponent(new ItemHoldComponent(this.player, newLightsaberOffset));

    //Commented out since lightsaber animation is a work in progress
    //AnimationRenderComponent lightSaberAnimator = WeaponsFactory.createAnimation("images/lightSaber.atlas", this.player);
    //newLightsaber.addComponent(lightSaberAnimator);

    return newLightsaber;
  }

  private Entity spawnPistol() {
    Entity newPistol = WeaponsFactory.createWeapon(Weapons.PISTOL);
    Vector2 newPistolOffset = new Vector2(0.45f, 0.02f);
    newPistol.addComponent(new ItemHoldComponent(this.player, newPistolOffset));
    return newPistol;
  }

  private Entity spawnRifle() {
    Entity newRifle = WeaponsFactory.createWeapon(Weapons.RIFLE);
    Vector2 newRifleOffset = new Vector2(0.25f, 0.15f);
    newRifle.addComponent(new ItemHoldComponent(this.player, newRifleOffset));
    return newRifle;
  }

  // Enemy Projectiles
  public Entity spawnEnemyProjectile(Vector2 directionToFire, WeaponsStatsComponent source) {
    Entity laser = ProjectileFactory.createEnemyProjectile(directionToFire, source);
    spawnEntityAt(laser, new GridPoint2(0, 0), true, true);

    return laser;
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
  private void spawnBoss2() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 pos = RandomUtils.random(minPos, maxPos);
    Entity boss2 = BossFactory.createBoss2(player);
    spawnEntityAt(boss2, pos, true, true);
  }
  //new added boss3
  private void spawnBoss3() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    GridPoint2 pos = RandomUtils.random(minPos, maxPos);
    Entity boss3 = BossFactory.createBoss3(player);
    spawnEntityAt(boss3, pos, true, true);
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
  private void spawnGhostGPT() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < NUM_GHOST_GPTS; i++) {
        GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
        Entity ghostGPT = NPCFactory.createGhostGPT(player, this);
        spawnEntityAt(ghostGPT, randomPos, true, true);
    }
  }
  /**
   * Adds NUM_Deep_spin amount of GhostGPT enemies onto the map.
   */
  private void spawnDeepspin() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < NUM_DEEP_SPIN; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity deepspin = NPCFactory.createDeepspin(player, this);
      spawnEntityAt(deepspin, randomPos, true, true);
    }
  }
  /**
   * Adds NUM_GROK_DROID amount of GrokDroid enemies onto the map.
   */
  private void spawnGrokDroid() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < NUM_GROK_DROID; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity grokDroid = NPCFactory.createGrokDroid(player, this);
      spawnEntityAt(grokDroid, randomPos, true, true);
    }
  }
  /**
   * Adds NUM_VROOMBA amount of GrokDroid enemies onto the map.
   */
  private void spawnVroomba() {
    GridPoint2 minPos = new GridPoint2(0, 0);
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(3, 3);

    for (int i = 0; i < NUM_VROOMBA; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity vroomba = NPCFactory.createVroomba(player, this);
      spawnEntityAt(vroomba, randomPos, true, true);
    }
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
    resourceService.loadTextures(forestTextures);
    resourceService.loadTextureAtlases(forestTextureAtlases);
    resourceService.loadSounds(forestSounds);
    resourceService.loadMusic(forestMusic);

    while (resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(forestTextures);
    resourceService.unloadAssets(forestTextureAtlases);
    resourceService.unloadAssets(forestSounds);
    resourceService.unloadAssets(forestMusic);
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
