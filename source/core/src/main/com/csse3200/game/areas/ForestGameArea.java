package com.csse3200.game.areas;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.items.ItemHoldComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopDemo;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.PowerupsFactory;
import com.csse3200.game.entities.factories.ShopFactory;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;

/**
 * A playable “Forest” style room. This class:
 * - Loads assets for this scene
 * - Builds the terrain and screen edges
 * - Spawns the player, props (desk, crates, energy pod), a keycard door, and enemies
 * - Starts background music
 * Think of this as the level assembler as it doesn’t know how to build each object,
 * it just asks the right factories to create them and places them in the world.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(3, 20);
    private static final int NUM_ITEMS = 5;//this is for ItemFactory
    // private static final int NUM_TURRETS = 1;
    private static final float WALL_WIDTH = 0.1f;

    private final float VERTICAL_HEIGHT_OFFSET = 9.375f;

    /**
     * Files or pictures used by the game (enemy/props,etc.).
     */
    private static final String HEART = "images/heart.png";
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
            "images/heart.png",
            "images/healthBench.png",
            "images/laserball.png",
            "images/computerBench.png",
            "images/boss_idle.png",
            "images/robot-2.png",
            "images/warning.png",
            "images/missle.png",
            "images/white_cocoon.png",
            "images/speedBench.png",
            "images/waterBullet.png",
            "images/VendingMachine.png",
            HEART,
            "images/heart.png",
            "images/laserball.png",
            "images/MarblePlatform.png",
            "images/computerBench.png",
            "images/monster.png",
            "images/electric_zap.png",
            "images/lightning_bottle.png",
            "images/Shipping.png",
            "images/ShipmentBoxLid.png",
            "images/ShipmentCrane.png",
            "images/Conveyor.png",
            "images/ServerRoomBackground.png",
            "images/ServerRoomBackgroundResize.png",
            "images/TunnelRoomBackgResize.png",
            "foreg_sprites/furniture/ServerRack.png",
            "foreg_sprites/furniture/ServerRack2.png",
            "foreg_sprites/furniture/Vent.png",
            "images/Storage.png"
    };

    /**
     * General prop textures (floors, tiles, etc.).
     */
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
            "foreg_sprites/general/Test.png"
    };
    private static final String[] researchTextures = {
            "images/ResearchBackground.png",
            "foreg_sprites/Research/Laboratory.png",
            "foreg_sprites/Research/Microscope.png",
            "foreg_sprites/Research/ResearchDesk.png",
            "foreg_sprites/Research/ResearchPod.png"
    };
    private static final String[] securityTextures = {
            "images/SecurityBackground.png",
            "foreg_sprites/general/ThinFloor3.png",
            "foreg_sprites/Security/Monitor.png",
            "foreg_sprites/Security/Platform.png",
            "foreg_sprites/Security/RedLight.png",
            "foreg_sprites/Security/SecuritySystem.png",
            "foreg_sprites/futuristic/storage_crate_green2.png",
            "foreg_sprites/futuristic/storage_crate_dark2.png",
            "foreg_sprites/futuristic/SecurityCamera3.png"
    };

    /**
     * Spawn pad textures.
     */
    private static final String[] spawnPadTextures = {
            "foreg_sprites/spawn_pads/SpawnPadPurple.png",
            "foreg_sprites/spawn_pads/SpawnPadRed.png",
    };

    /**
     * Office furniture textures used on the upper platform.
     */
    private static final String[] officeTextures = {
            "foreg_sprites/office/CeilingLight.png",
            "foreg_sprites/office/Crate.png",
            "foreg_sprites/office/LargeShelf.png",
            "foreg_sprites/office/MidShelf.png",
            "foreg_sprites/office/LongCeilingLight2.png",
            "foreg_sprites/office/OfficeChair.png",
            "foreg_sprites/office/officeDesk4.png",
    };

    /**
     * Futuristic props used in this room (camera, energy pod, crates).
     */
    private static final String[] futuristicTextures = {
            "foreg_sprites/futuristic/SecurityCamera3.png",
            "foreg_sprites/futuristic/EnergyPod.png",
            "foreg_sprites/futuristic/storage_crate_green2.png",
            "foreg_sprites/futuristic/storage_crate_dark2.png",
    };

    /**
     * keycard textures
     */
    private static final String[] keycardTextures = {
            "images/keycard_lvl1.png",
            "images/keycard_lvl2.png",
            "images/keycard_lvl3.png",
            "images/keycard_lvl4.png",
    };

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
            "images/Turret.atlas",
            "images/explosion_1.atlas",
            "images/explosion_2.atlas",
            "images/player.atlas",
            "images/boss_explosion.atlas",
            "images/Boss3_Attacks.atlas",
            "images/player.atlas",
            "images/terrain_iso_grass.atlas",
            "images/ghost.atlas",
            "images/ghostKing.atlas",
            "images/ghostGPT.atlas",
            "images/explosion_1.atlas",
            "images/explosion_2.atlas",
            "images/boss_explosion.atlas",
            "images/boss2_combined.atlas",
            "images/Boss3_Attacks.atlas",
            "images/boss3_phase2.atlas"
    };
    private static final String[] forestSounds = {"sounds/Impact4.ogg",
            "sounds/shot_failed.mp3",
            "sounds/reload.mp3",
            "sounds/laser_blast.mp3",
            "sounds/ammo_replenished.mp3"};

    private static final String[] playerSound1 = {"sounds/jump.mp3"};
    private static final String[] enemySounds = {"sounds/enemyDamage.mp3", "sounds/enemyDeath.mp3"};
    private static final String BACKGROUND_MUSIC = "sounds/BGM_03.mp3";

    private static final String[] forestMusic = {BACKGROUND_MUSIC};

    private Entity player;
    private Entity dagger;
    private Entity lightsaber;
    private Entity bullet;
    private Entity pistol;
    private Entity rifle;


    /**
     * Initialise this ForestGameArea to use the provided TerrainFactory and camera helper.
     * The camera is used to size the screen-edge walls and place the right-side door trigger.
     *
     * @param terrainFactory  TerrainFactory used to create the terrain for the GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera (optional but used here).
     * @requires terrainFactory != null
     */
    public ForestGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    /**
     * Entry point for this room. This:
     * - Loads textures/sounds/music
     * - Registers this room in the ServiceLocator
     * - Creates the terrain, walls, and UI label
     * - Spawns player, props (desk, crates, pod), door (with keycard gate), and enemies
     * - Starts background music
     */
    @Override
    public void create() {
        ServiceLocator.registerGameArea(this);
        loadAssets();
        displayUI();
        spawnTerrain();
        spawnComputerBench();
        spawnHealthBench();
        spawnSpeedBench();

        player = spawnPlayer();
        ServiceLocator.registerPlayer(player);
        spawnFloor();
        spawnBottomRightDoor();
        spawnMarblePlatforms();
        spawnShopKiosk();
        SecureRandom random = new SecureRandom();
        int choice = random.nextInt(3);
        switch (choice) {
            case 0 -> spawnBoss2();
            case 1 -> spawnRobots();
            default -> spawnBoss3();
        }
        playMusic();
        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.forestmap());

        // Place a keycard on the floor so the player can unlock the door
        float keycardX = 1f;
        float keycardY = 15f;
        Entity keycard = KeycardFactory.createKeycard(1);
        keycard.setPosition(new Vector2(keycardX, keycardY));
        spawnEntity(keycard);

        spawnItems();
    }

    private void spawnRobots() {
        GridPoint2 pos = new GridPoint2(8, 13);
        Entity robot = BossFactory.createRobot(player);
        spawnEntityAt(robot, pos, true, true);

    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
        spawnEntity(ui);
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
            float bottomY = camPos.y - VERTICAL_HEIGHT_OFFSET / 2f;
            float topY = camPos.y + VERTICAL_HEIGHT_OFFSET / 2f;

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

            // Thin sensor line on the right that loads the next level on overlap
            Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
            rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
            rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(() -> this.loadNextLevel()));
            // spawnEntity(rightDoor);
        }
    }

    /**
     * Disposes current entities and switches to Floor2GameArea.
     * This is called by the door/keycard logic when the player exits.
     */
    private void loadNextLevel() {
        roomNumber++;
        // Use the safe, render-thread transition helper
        clearAndLoad(() -> new Reception(terrainFactory, cameraComponent));
    }

    /**
     * Builds the upper walkway: three thin floors, a long ceiling light, and a front-facing desk.
     */
    private void spawnPlatforms() {
        for (int i = 0; i < 3; i++) {
            GridPoint2 platformPos = new GridPoint2(i * 5, 10);
            Entity platform = ObstacleFactory.createThinFloor();
            spawnEntityAt(platform, platformPos, true, false);
        }
        Entity officeDesk = ObstacleFactory.createOfficeDesk();
        spawnEntityAt(officeDesk, new GridPoint2(5, 11), true, false);
    }

    private void spawnShopKiosk() {
        CatalogService catalog = ShopDemo.makeDemoCatalog();
        ShopManager manager = new ShopManager(catalog);

        Entity shop = ShopFactory.createShop(this, manager, "images/VendingMachine.png"); // have as tree now as placeholder, later need to change to actual shop icon
        spawnEntityAt(shop, new GridPoint2(18, 7), true, false);
    }

    private void spawnComputerBench() {
        Entity bench = InteractableStationFactory.createStation(Benches.COMPUTER_BENCH);
        spawnEntityAt(bench, new GridPoint2(2, 7), true, true);

    }

    private void spawnHealthBench() {
        Entity bench = InteractableStationFactory.createStation(Benches.HEALTH_BENCH);
        spawnEntityAt(bench, new GridPoint2(8, 7), true, true);
    }

    private void spawnSpeedBench() {
        Entity bench = InteractableStationFactory.createStation(Benches.SPEED_BENCH);
        spawnEntityAt(bench, new GridPoint2(25, 7), true, true);
    }


    /**
     * Places a large door sprite at the bottom-right platform. The door uses a keycard gate:
     * when the player has key level 1, the door callback triggers and we load the next level.
     */
    private void spawnBottomRightDoor() {
        float doorX = 13.9f;
        float doorY = 3.75f;

        Entity door = ObstacleFactory.createDoorTrigger(20f, 40f);
        TextureRenderComponent texture = new TextureRenderComponent("images/KeycardDoor.png");
        door.addComponent(texture);
        texture.scaleEntity();
        door.setPosition(doorX, doorY);
        door.addComponent(new KeycardGateComponent(1, () -> {
            logger.info("Bottom-right platform door unlocked — loading next level");
            loadNextLevel();
        }));

        spawnEntity(door);
    }

    /**
     * Places two platforms within the room for players to jump on.
     */
    private void spawnMarblePlatforms() {
        float platformX = 2.5f;
        float platformX2 = 5.4f;
        float platformX3 = 8.2f;
        float platformX4 = 11.1f;
        float platformY = 6f;
        float platformY2 = 8f;

        Entity platform1 = ObstacleFactory.createMarblePlatform();
        platform1.setPosition(platformX, platformY);

        Entity platform2 = ObstacleFactory.createMarblePlatform();
        platform2.setPosition(platformX2, platformY2);

        Entity platform3 = ObstacleFactory.createMarblePlatform();
        platform3.setPosition(platformX3, platformY2);

        Entity platform4 = ObstacleFactory.createMarblePlatform();
        platform4.setPosition(platformX4, platformY);

        spawnEntity(platform1);
        spawnEntity(platform2);
        spawnEntity(platform3);
        spawnEntity(platform4);
    }

    /**
     * Places the purple spawn pad on the lower floor (visual prop).
     */
    private void spawnPad() {
        GridPoint2 spawnPadPos = new GridPoint2(20, 3);

        Entity spawnPad = ObstacleFactory.createPurpleSpawnPad();

        spawnEntityAt(spawnPad, spawnPadPos, false, false);
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
     * Spawns several item entities at random positions in the game area.
     * The number of items is set by NUM_ITEMS.
     * Each item is created and placed at a random spot on the terrain.
     */
    private void spawnItems() {
        GridPoint2 firstPos = new GridPoint2(5, 25);
        GridPoint2 secondPos = new GridPoint2(10, 25);
        GridPoint2 thirdPos = new GridPoint2(15, 25);

        spawnEntityAt(ItemFactory.createItem(HEART), firstPos, true, false);
        spawnEntityAt(ItemFactory.createItem(HEART), secondPos, true, false);
        spawnEntityAt(ItemFactory.createItem(HEART), thirdPos, true, false);
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
        inventory.setCurrItem(item);
        spawnEntityAt(item, PLAYER_SPAWN, true, true);
    }

    private Entity spawnLightsaber() {
        Entity newLightsaber = WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
        Vector2 newLightsaberOffset = new Vector2(0.9f, -0.2f);
        newLightsaber.addComponent(new ItemHoldComponent(this.player, newLightsaberOffset));
        AnimationRenderComponent lightSaberAnimator = WeaponsFactory.createAnimation("images/lightSaber.atlas", this.player);
        newLightsaber.addComponent(lightSaberAnimator);
        lightSaberAnimator.startAnimation("anim");

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
        Vector2 newRifleOffset = new Vector2(0.8f, 0.15f);
        newRifle.addComponent(new ItemHoldComponent(this.player, newRifleOffset));
        return newRifle;
    }

    private Entity spawnRapidFirePowerup() {
        Entity newRapidFirePowerup = PowerupsFactory.createRapidFire();
        spawnEntityAt(newRapidFirePowerup, new GridPoint2(2, 40), true, true);
        return newRapidFirePowerup;
    }

    private void spawnBoss2() {
        GridPoint2 pos = new GridPoint2(5, 8);
        Entity boss2 = BossFactory.createBoss2(player);
        spawnEntityAt(boss2, pos, true, true);
    }

    //new added boss3
    private void spawnBoss3() {
        GridPoint2 pos = new GridPoint2(15, 16);

        Entity boss3 = BossFactory.createBoss3(player);
        spawnEntityAt(boss3, pos, true, true);
    }

    public void spawnItem(Entity item, GridPoint2 position) {
        spawnEntityAt(item, position, false, false);
    }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();
    }

    /**
     * Loads all textures, atlases, sounds and music needed by this room.
     * Blocks briefly until loading is complete. If you add new art, put it here.
     */
    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(futuristicTextures);
        resourceService.loadTextures(keycardTextures);
        resourceService.loadTextures(generalTextures);
        resourceService.loadTextures(forestTextures);
        resourceService.loadTextures(spawnPadTextures);
        resourceService.loadTextures(officeTextures);
        resourceService.loadTextures(securityTextures);
        resourceService.loadTextures(researchTextures);
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
     * Unloads assets that were loaded in {@link #loadAssets()}.
     * Call this when leaving the room to free memory.
     */
    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(keycardTextures);
        resourceService.unloadAssets(futuristicTextures);
        resourceService.unloadAssets(playerSound1);
        resourceService.unloadAssets(forestTextures);
        resourceService.unloadAssets(generalTextures);
        resourceService.unloadAssets(forestTextureAtlases);
        resourceService.unloadAssets(forestSounds);
        resourceService.unloadAssets(forestMusic);
        resourceService.unloadAssets(spawnPadTextures);
        resourceService.unloadAssets(officeTextures);
        resourceService.unloadAssets(securityTextures);
    }

    // Removed area-specific dispose to avoid double disposal during transitions


    public Entity getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Forest";
    }

    public static ForestGameArea load(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        return (new ForestGameArea(terrainFactory, cameraComponent));
    }
}
