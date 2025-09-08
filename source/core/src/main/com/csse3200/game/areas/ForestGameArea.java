package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.ItemHoldComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.NPCFactory;
import com.csse3200.game.entities.factories.ObstacleFactory;
import com.csse3200.game.entities.factories.PlayerFactory;
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
  private static final int NUM_GHOSTS = 2;
  private static final int NUM_GHOST_GPTS = 4;
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
    "images/lightsaber.png",
    "images/lightsaberSingle.png",
    "images/ammo.png",
    "images/round.png",
    "images/pistol.png",
    "images/rifle.png",
    "images/dagger.png",
    "images/laser_shot.png",
    "images/mud.png"
  };
  
  private static final String[] forestTextureAtlases = {
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

  private Entity player;


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
    playMusic();

    player = spawnPlayer();
    Entity dagger = spawnDagger();
    Entity pistol = spawnPistol();
    Entity rifle = spawnRifle();
    Entity lightsaber = spawnLightsaber();
    Entity rapidFirePowerup = spawnRapidFirePowerup();
    Entity bullet = spawnBullet();

    //These are commented out since there is no equip feature yet
//    this.equipItem(pistol);
//    this.equipItem(lightsaber);
//    this.equipItem(dagger);
    this.equipItem(rifle);

    spawnBullet();

//    spawnGhosts();
//    spawnGhostKing();
//    spawnGhostGPT();
//    playMusic();
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
    GridPoint2 maxPos = terrain.getMapBounds(0).sub(2, 2);

    for (int i = 0; i < NUM_TREES; i++) {
      GridPoint2 randomPos = RandomUtils.random(minPos, maxPos);
      Entity tree = ObstacleFactory.createTree();
      spawnEntityAt(tree, randomPos, true, false);
    }
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
  private Entity spawnBullet() {
    Entity newBullet = ProjectileFactory.createPistolBullet();
    spawnEntityAt(newBullet, new GridPoint2(5, 5), true, true);
    return newBullet;
  }

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

  private Entity spawnRapidFirePowerup() {
    Entity newRapidFirePowerup = PowerupsFactory.createRapidFire();
    spawnEntityAt(newRapidFirePowerup, new GridPoint2(25, 20), true, true);
    return newRapidFirePowerup;
  }

  // Enemy Projectiles
  public Entity spawnLaserProjectile(Vector2 directionToFire) {
    Entity laser = ProjectileFactory.createLaserShot(directionToFire);
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



    while (!resourceService.loadForMillis(10)) {
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
