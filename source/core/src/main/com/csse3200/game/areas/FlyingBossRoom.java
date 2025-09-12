package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.ItemHoldComponent;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
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
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.utils.math.GridPoint2Utils;
import com.csse3200.game.utils.math.RandomUtils;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.rendering.TextureRenderComponent;

import javax.naming.spi.ObjectFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the room that holds the Flying Boss.
 * The boss is a flying enemy that spawns at the top of the map and
 * shoots projectiles at the player.
 * 
 * There are two platforms that can possibly server as cover as well as a floor at the bottom
 */
public class FlyingBossRoom extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(FlyingBossRoom.class);
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(3, 5);

    private static final float WALL_WIDTH = 0.1f;

    /** Files or pictures used by the game (enemy/props,etc.). */
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
        "images/player.png",
        "images/mud.png",
        "images/heart.png"
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
    };

    /** Spawn pad textures. */
    private static final String[] spawnPadTextures = {
        "foreg_sprites/spawn_pads/SpawnPadPurple.png",
        "foreg_sprites/spawn_pads/SpawnPadRed.png",
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
        "images/player.atlas",
        "images/player.atlas",
        "images/terrain_iso_grass.atlas",
        "images/ghost.atlas",
        "images/ghostKing.atlas",
        "images/ghostGPT.atlas",
        "images/explosion_1.atlas",
        "images/explosion_2.atlas",
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
     * Creates a new FlyingBossRoom for the room where the flying boss spawns.
     * 
     * @param terrainFactory TerrainFactory used to create the terrain for the GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera (optional but used here).
     * @requires terrainFactory not null
     */
    public FlyingBossRoom(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super();
        this.terrainFactory = terrainFactory;
        this.cameraComponent = cameraComponent;
    }

    /**
     * Creates the room by:
     *  - loading assest
     *  - displaying the UI
     *  - spawning terrain (without door triggers)
     *  - spawn player and rifle
     *  - spawns floors
     *  - spawns prop
     * - spawns flying boss
     */
    @Override
    public void create() {
        loadAssets();
    
        displayUI();
        spawnTerrain();

        player = spawnPlayer();
        // dagger = spawnDagger();
        // pistol = spawnPistol();
        rifle = spawnRifle();
        // lightsaber = spawnLightsaber();

        this.equipItem(rifle);

        spawnFloor();
        spawnPlatforms();
        spawnBigWall();


        spawnFlyingBoss();
    }

    private void spawnPlatforms() {
        Entity platform1 = ObstacleFactory.createThinFloor();
        GridPoint2 platform1Pos = new GridPoint2(0, 10);
        spawnEntityAt(platform1, platform1Pos, false, false);

        Entity platform2 = ObstacleFactory.createThinFloor();
        GridPoint2 platform2Pos = new GridPoint2(20, 10);
        spawnEntityAt(platform2, platform2Pos, false, false);
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
        spawnEntity(ui);
    }

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        return newPlayer;
    }

    private void spawnFlyingBoss() {
        GridPoint2 pos = new GridPoint2(15, 20);

        Entity flyingBoss = BossFactory.createBoss2(player);
        spawnEntityAt(flyingBoss, pos, true, true);
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
        terrain = terrainFactory.createTerrain(TerrainType.SPAWN_ROOM);
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

        // // Thin sensor line on the right that loads the next level on overlap
        // Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        //     rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
        //     rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(() -> this.loadNextLevel()));
        //     spawnEntity(rightDoor);
        }
    }

    private void equipItem(Entity item) {
        InventoryComponent inventory = this.player.getComponent(InventoryComponent.class);
        inventory.addItem(item);
        spawnEntityAt(item, PLAYER_SPAWN, true, true);
    }

    private Entity spawnRifle() {
        Entity newRifle = WeaponsFactory.createWeapon(Weapons.RIFLE);
        Vector2 newRifleOffset = new Vector2(0.25f, 0.15f);
        newRifle.addComponent(new ItemHoldComponent(this.player, newRifleOffset));
        return newRifle;
    }

  /**
   * Loads all textures, atlases, sounds and music needed by this room.
   * Blocks briefly until loading is complete. If you add new art, put it here.
   */
  private void loadAssets() {
    logger.debug("Loading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.loadTextures(generalTextures);
    resourceService.loadTextures(forestTextures);
    resourceService.loadTextures(spawnPadTextures);
    resourceService.loadTextureAtlases(forestTextureAtlases);
    resourceService.loadSounds(forestSounds);
    resourceService.loadMusic(forestMusic);

    while (resourceService.loadForMillis(10)) {
      // This could be upgraded to a loading screen
      logger.info("Loading... {}%", resourceService.getProgress());
    }
  }

  /**
   * Unloads assets that were loaded in {@link #loadAssets()}.
   * Call this when leaving the room to free memory.
   */
  private void unloadAssets() {
    logger.debug("Unloading assets");
    ResourceService resourceService = ServiceLocator.getResourceService();
    resourceService.unloadAssets(forestTextures);
    resourceService.unloadAssets(generalTextures);
    resourceService.unloadAssets(forestTextureAtlases);
    resourceService.unloadAssets(forestSounds);
    resourceService.unloadAssets(forestMusic);
    resourceService.unloadAssets(spawnPadTextures);
  }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();
    }

    @Override
    public void dispose() {
        super.dispose();
        // ServiceLocator.getResourceService().getAsset(backgroundMusic, Music.class).stop();
        this.unloadAssets();
    }


    public Entity getPlayer() {
        return player;
    }
}
