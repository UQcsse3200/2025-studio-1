package com.csse3200.game.areas;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.ItemPickUpComponent;
import com.csse3200.game.components.player.PlayerEquipComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.GameLevelBootstrap;
import com.csse3200.game.files.RegistryEntityPlacer;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;

/**
 * Forest room, now largely data-driven via JSON (levels/forest/*.json).
 * Walls/floor/keycard door remain code-driven for custom logic.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);

    private static final int NUM_ITEMS = 5;
    private static final float WALL_WIDTH = 0.1f;
    private static final String BACKGROUND_MUSIC = "sounds/forestmusic.mp3";
    private static final String[] forestMusic = {BACKGROUND_MUSIC};

    // These arrays remain so your ResourceService preloads art/atlases used by factories
    private static final String[] forestTextures = {
            "backgrounds/SpawnResize.png",
            "images/box_boy_leaf.png", "images/tree.png", "images/ghost_king.png", "images/ghost_1.png",
            "images/grass_1.png", "images/grass_2.png", "images/grass_3.png", "images/hex_grass_1.png",
            "images/hex_grass_2.png", "images/hex_grass_3.png", "images/iso_grass_1.png", "images/iso_grass_2.png",
            "images/iso_grass_3.png", "images/robot-2-attack.png", "images/robot-2-common.png", "images/fireball1.png",
            "images/blackhole1.png", "images/Robot_1.png", "images/Robot_1_attack_Right.png", "images/Boss_3.png",
            "images/mud.png", "images/mud_ball_1.png", "images/mud_ball_2.png", "images/mud_ball_3.png",
            "images/lightsaber.png", "images/lightsaberSingle.png", "images/ammo.png", "images/round.png",
            "images/pistol.png", "images/rifle.png", "images/dagger.png", "images/rapidfirepowerup.png",
            "images/aimbot_powerup.png", "images/doubleprocessorspowerup.png", "images/laser_shot.png", "images/Spawn.png",
            "images/LobbyWIP.png", "images/door.png", "images/KeycardDoor.png", "images/player.png", "images/engineer.png",
            "images/soldier.png", "images/mud.png", "images/healthBench.png", "images/laserball.png", "images/computerBench.png",
            "images/boss_idle.png", "images/robot-2.png", "images/warning.png", "images/missle.png", "images/white_cocoon.png",
            "images/speedBench.png", "images/waterBullet.png", "images/VendingMachine.png", "images/laserball.png",
            "images/MarblePlatform.png", "images/computerBench.png", "images/monster.png", "images/electric_zap.png",
            "images/lightning_bottle.png", "images/ShipmentBoxLid.png", "images/ShipmentCrane.png", "images/Conveyor.png",
            "images/ServerRoomBackground.png", "images/ServerRoomBackgroundResize.png", "images/TunnelRoomBackgResize.png",
            "foreg_sprites/furniture/ServerRack.png", "foreg_sprites/furniture/ServerRack2.png", "foreg_sprites/furniture/Vent.png",
            "images/rocketlauncher.png", "images/rocket.png", "images/rocketExplosion.png", "images/Storage.png", "images/casino.png",
            "images/!.png", "images/NpcDialogue.png", "images/nurse_npc.png", "images/partner.png", "images/remote.png",
            "images/Assistor.png", "images/laserbullet.png", "images/armour-assets/chestplate.png", "images/armour-assets/hood.png",
            "images/blackjack_table.png"
    };
    private static final String[] backgroundTextures = {
            "backgrounds/Reception.png", "backgrounds/Shipping.png", "backgrounds/SpawnResize.png", "backgrounds/Storage.png",
            "images/Storage.png", "images/cards.png", "backgrounds/Storage.png", "backgrounds/MainHall.png", "backgrounds/Office.png",
            "backgrounds/Research.png", "backgrounds/Security.png", "backgrounds/Server.png"
    };
    private static final String[] generalTextures = {
            "foreg_sprites/general/LongFloor.png", "foreg_sprites/general/Railing.png", "foreg_sprites/general/SmallSquare.png",
            "foreg_sprites/general/SmallStair.png", "foreg_sprites/general/SquareTile.png", "foreg_sprites/general/ThickFloor.png",
            "foreg_sprites/general/ThinFloor.png", "foreg_sprites/general/ThinFloor2.png", "foreg_sprites/general/ThinFloor3.png",
            "foreg_sprites/general/Test.png", "foreg_sprites/general/Test.png", "foreg_sprites/furniture/LabPlant1.png",
            "foreg_sprites/furniture/LabPlant2.png", "foreg_sprites/furniture/PurpleWindow.png"
    };
    private static final String[] researchTextures = {
            "images/ResearchBackground.png", "foreg_sprites/Research/Laboratory.png", "foreg_sprites/Research/Microscope.png",
            "foreg_sprites/Research/ResearchDesk.png", "foreg_sprites/Research/ResearchPod.png"
    };
    private static final String[] securityTextures = {
            "images/SecurityBackground.png", "foreg_sprites/general/ThinFloor3.png", "foreg_sprites/Security/Monitor.png",
            "foreg_sprites/Security/Platform.png", "foreg_sprites/Security/RedLight.png", "foreg_sprites/Security/SecuritySystem.png",
            "foreg_sprites/futuristic/storage_crate_green2.png", "foreg_sprites/futuristic/storage_crate_dark2.png",
            "foreg_sprites/futuristic/SecurityCamera3.png", "images/slots_kiosk.png", "images/bell.png", "images/cherry.png",
            "images/diamond.png", "images/lemon.png", "images/watermelon.png"
    };
    private static final String[] spawnPadTextures = {
            "foreg_sprites/spawn_pads/SpawnPadPurple.png", "foreg_sprites/spawn_pads/SpawnPadRed.png"
    };
    private static final String[] officeTextures = {
            "foreg_sprites/office/CeilingLight.png", "foreg_sprites/office/Crate.png", "foreg_sprites/office/LargeShelf.png",
            "foreg_sprites/office/MidShelf.png", "foreg_sprites/office/LongCeilingLight2.png", "foreg_sprites/office/OfficeChair.png",
            "foreg_sprites/office/officeDesk4.png"
    };
    private static final String[] futuristicTextures = {
            "foreg_sprites/futuristic/SecurityCamera3.png", "foreg_sprites/futuristic/EnergyPod.png",
            "foreg_sprites/futuristic/storage_crate_green2.png", "foreg_sprites/futuristic/storage_crate_dark2.png"
    };
    private static final String[] keycardTextures = {
            "images/keycard_lvl1.png", "images/keycard_lvl2.png", "images/keycard_lvl3.png", "images/keycard_lvl4.png"
    };
    private static final String[] forestTextureAtlases = {
            "images/robot-2.atlas", "images/fireball.atlas", "images/blackhole.atlas", "images/Robot_1.atlas", "images/boss_idle.atlas",
            "images/terrain_iso_grass.atlas", "images/ghost.atlas", "images/ghostKing.atlas", "images/ghostGPT.atlas",
            "images/ghostGPTRed.atlas", "images/ghostGPTBlue.atlas", "images/Deepspin.atlas", "images/DeepspinRed.atlas",
            "images/DeepspinBlue.atlas", "images/Grokdroid.atlas", "images/GrokdroidRed.atlas", "images/GrokdroidBlue.atlas",
            "images/Vroomba.atlas", "images/VroombaRed.atlas", "images/VroombaBlue.atlas", "images/Turret.atlas",
            "images/explosion_1.atlas", "images/explosion_2.atlas", "images/engineer.atlas", "images/player.atlas",
            "images/soldier.atlas", "images/boss_explosion.atlas", "images/Boss3_Attacks.atlas", "images/player.atlas",
            "images/terrain_iso_grass.atlas", "images/boss_explosion.atlas", "images/boss2_combined.atlas", "images/Boss3_Attacks.atlas",
            "images/boss3_phase2.atlas", "images/rocketExplosion.atlas", "images/!animation.atlas", "images/guidance_npc.atlas",
            "images/assister_npc.atlas", "images/cards.atlas"
    };
    private static final String[] extraTextures = {
            "foreg_sprites/extras/Spikes.png", "foreg_sprites/extras/Spikes2.png"
    };
    private static final String[] forestSounds = {
            "sounds/Impact4.ogg", "sounds/shot_failed.mp3", "sounds/reload.mp3", "sounds/laser_blast.mp3",
            "sounds/ammo_replenished.mp3", "sounds/upgradeSound.mp3"
    };
    private static final String[] playerSound1 = {"sounds/jump.mp3"};
    private static final String[] enemySounds = {
            "sounds/deepspinDamage.mp3", "sounds/deepspinDeath.mp3", "sounds/vroombaDamage.mp3", "sounds/vroombaDeath.mp3",
            "sounds/GPTDamage.mp3", "sounds/GPTDeath.mp3", "sounds/grokDamage.mp3", "sounds/grokDeath.mp3",
            "sounds/turretDamage.mp3", "sounds/turretDeath.mp3"
    };

    private static GridPoint2 playerSpawn = new GridPoint2(3, 20);
    private final float VERTICAL_HEIGHT_OFFSET = 9.375f;

    private Entity player;

    public ForestGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public static void setRoomSpawn(GridPoint2 newSpawn) {
        if (newSpawn != null) {
            ForestGameArea.playerSpawn = newSpawn;
        }
    }

    public static ForestGameArea load(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        return new ForestGameArea(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        this.baseScaling = 1f;

        ServiceLocator.registerGameArea(this);
        loadAssets();
        displayUI();
        spawnTerrain();

        // player first, so factories that need it (NPCs) can use it
        player = spawnPlayer();
        ServiceLocator.registerPlayer(player);

        // keep your code-driven content
        spawnFloor();
        spawnBottomRightDoor();

        // JSON-driven content (benches, platforms, enemies, keycard, teleporter)
        loadForestFromJson();

        // extras
        playMusic();
        new ItemSpawner(this).spawnItems(ItemSpawnConfig.forestmap());
        spawnGuidanceNpc();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Box Forest"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 1"));
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.SPAWN_ROOM);
        spawnEntity(new Entity().addComponent(terrain));

        if (cameraComponent != null) {
            OrthographicCamera cam = (OrthographicCamera) cameraComponent.getCamera();
            Vector2 camPos = cameraComponent.getEntity().getPosition();
            float viewWidth = cam.viewportWidth;
            float viewHeight = cam.viewportHeight;
            float leftX = camPos.x - viewWidth / 2f;
            float rightX = camPos.x + viewWidth / 2f;
            float bottomY = camPos.y - VERTICAL_HEIGHT_OFFSET / 2f;
            float topY = camPos.y + VERTICAL_HEIGHT_OFFSET / 2f;

            Entity left = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
            left.setPosition(leftX, bottomY);
            spawnEntity(left);

            Entity right = ObstacleFactory.createWall(WALL_WIDTH, viewHeight);
            right.setPosition(rightX - WALL_WIDTH, bottomY);
            spawnEntity(right);

            Entity top = ObstacleFactory.createWall(viewWidth, WALL_WIDTH);
            top.setPosition(leftX, topY - WALL_WIDTH);
            spawnEntity(top);

            // bottom split around a "door" gap
            float doorWidth = Math.max(1f, viewWidth * 0.2f);
            float doorX = camPos.x - doorWidth / 2f;

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

            // right-edge trigger to load next level
            float rightDoorHeight = Math.max(1f, viewHeight * 0.2f);
            float rightDoorY = camPos.y - rightDoorHeight / 2f;
            Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
            rightDoor.setPosition(rightX - WALL_WIDTH - 0.001f, rightDoorY);
            rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadNextLevel));
            spawnEntity(rightDoor); // <-- ensure it actually spawns

            // left-edge door trigger to Casino (helper splits wall)
            Bounds b = getCameraBounds(cameraComponent);
            addVerticalDoorLeft(b, WALL_WIDTH, this::loadCasino);
        }
    }

    private void loadNextLevel() {
        Reception.setRoomSpawn(new GridPoint2(6, 10));
        clearAndLoad(() -> new Reception(terrainFactory, cameraComponent));
    }

    private void loadCasino() {
        clearAndLoad(() -> new CasinoGameArea(terrainFactory, cameraComponent));
    }

    /**
     * JSON bootstrap: load all levels/forest/*.json and place entities
     */
    private void loadForestFromJson() {
        // Separate AssetManager only for any raw texture paths listed in JSON.
        // (Your factories rely on atlases already preloaded via ResourceService.)
        AssetManager assets = new AssetManager();

        RegistryEntityPlacer placer = buildForestPlacer();

        GameLevelBootstrap.loadDirectoryAndPlace(
                assets,
                FileLoader.Location.INTERNAL,
                "levels/forest",
                "*.json",
                placer,
                true // sync load JSON-listed textures (harmless if unused by factories)
        );
    }

    /**
     * Placer that maps type strings from JSON to your factories
     */
    private RegistryEntityPlacer buildForestPlacer() {
        return new RegistryEntityPlacer((name, type, grid) -> {
            if (type == null) return;
            String t = type.trim().toLowerCase(Locale.ROOT);

            if (t.startsWith("enemy:")) {
                spawnEnemyVariant(t.substring("enemy:".length()), grid);
                return;
            }
            if (t.startsWith("bench:")) {
                switch (t) {
                    case "bench:computer" -> {
                        Entity e = InteractableStationFactory.createStation(Benches.COMPUTER_BENCH);
                        spawnEntityAt(e, grid, true, true);
                    }
                    case "bench:health" -> {
                        Entity e = InteractableStationFactory.createStation(Benches.HEALTH_BENCH);
                        spawnEntityAt(e, grid, true, true);
                    }
                    case "bench:speed" -> {
                        Entity e = InteractableStationFactory.createStation(Benches.SPEED_BENCH);
                        spawnEntityAt(e, grid, true, true);
                    }
                    default -> logger.warn("Unknown bench subtype '{}' for '{}'", t, name);
                }
                return;
            }
            if (t.startsWith("prop:")) {
                switch (t) {
                    case "prop:marbleplatform" -> {
                        Entity p = ObstacleFactory.createMarblePlatform();
                        spawnEntityAt(p, grid, false, false);
                    }
                    default -> logger.warn("Unknown prop subtype '{}' for '{}'", t, name);
                }
                return;
            }
            if (t.startsWith("item:keycard:")) {
                try {
                    int level = Integer.parseInt(t.substring("item:keycard:".length()));
                    Entity key = KeycardFactory.createKeycard(level);
                    key.setPosition(terrain.tileToWorldPosition(grid));
                    spawnEntity(key);
                } catch (NumberFormatException nfe) {
                    logger.warn("Bad keycard level in type='{}' for '{}'", t, name);
                }
                return;
            }
            if (t.equals("teleporter")) {
                Entity tp = TeleporterFactory.createTeleporter(terrain.tileToWorldPosition(grid));
                spawnEntity(tp);
                return;
            }

            logger.warn("No handler for type={} (entity={}, at={})", type, name, grid);
        })
                // optional direct registrations to skip fallback switch
                .registerCi("prop:marbleplatform", (n, tt, g) -> {
                    Entity p = ObstacleFactory.createMarblePlatform();
                    spawnEntityAt(p, g, false, false);
                })
                .registerCi("bench:computer", (n, tt, g) -> {
                    Entity e = InteractableStationFactory.createStation(Benches.COMPUTER_BENCH);
                    spawnEntityAt(e, g, true, true);
                })
                .registerCi("bench:health", (n, tt, g) -> {
                    Entity e = InteractableStationFactory.createStation(Benches.HEALTH_BENCH);
                    spawnEntityAt(e, g, true, true);
                })
                .registerCi("bench:speed", (n, tt, g) -> {
                    Entity e = InteractableStationFactory.createStation(Benches.SPEED_BENCH);
                    spawnEntityAt(e, g, true, true);
                });
    }

    /**
     * Map enemy subtypes from JSON to your NPC factory methods
     */
    protected void spawnEnemyVariant(String variantLower, GridPoint2 grid) {
        float scale = getBaseDifficultyScale();

        Entity p = ServiceLocator.getPlayer();
        if (p == null) {
            p = (player != null) ? player : spawnOrRepositionPlayer(new GridPoint2(1, 1));
        }

        Entity e = switch (variantLower) {
            case "ghostgpt" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGhostGPT(p, this, scale);
            case "ghostgptred" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGhostGPTRed(p, this, scale);
            case "ghostgptblue" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGhostGPTBlue(p, this, scale);

            case "deepspin" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspin(p, this, scale);
            case "deepspinred" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspinRed(p, this, scale);
            case "deepspinblue" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspinBlue(p, this, scale);

            case "vroomba" -> com.csse3200.game.entities.factories.characters.NPCFactory.createVroomba(p, scale);
            case "vroombared" -> com.csse3200.game.entities.factories.characters.NPCFactory.createVroombaRed(p, scale);
            case "vroombablue" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createVroombaBlue(p, scale);

            case "grokdroid" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGrokDroid(p, this, scale);
            case "grokdroidred" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGrokDroidRed(p, this, scale);
            case "grokdroidblue" ->
                    com.csse3200.game.entities.factories.characters.NPCFactory.createGrokDroidBlue(p, this, scale);

            case "turret" -> com.csse3200.game.entities.factories.characters.NPCFactory.createTurret(p, this, scale);

            default -> {
                logger.warn("Unknown enemy subtype '{}' at {}", variantLower, grid);
                yield com.csse3200.game.entities.factories.characters.NPCFactory.createGhostGPT(p, this, scale);
            }
        };

        spawnEntityAt(e, grid, true, true);
    }

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

    private Entity spawnPlayer() {
        Entity p = spawnOrRepositionPlayer(playerSpawn);
        if (ServiceLocator.getPlayer() == p) {
            p.getEvents().addListener("equip", this::equipItem);
            p.getEvents().addListener("unequip", this::unequipItem);
        }
        return p;
    }

    private void equipItem(String tex) {
        Entity item = player.getComponent(ItemPickUpComponent.class).createItemFromTexture(tex);
        if (item == null) return;

        float playerZ = player.getComponent(AnimationRenderComponent.class).getZIndex();
        TextureRenderComponent texComp = item.getComponent(TextureRenderComponent.class);
        TextureRenderWithRotationComponent texRotComp = item.getComponent(TextureRenderWithRotationComponent.class);
        if (texRotComp != null) {
            texRotComp.setZIndex(playerZ + 0.01f);
        } else if (texComp != null) {
            texComp.setZIndex(playerZ + 0.01f);
        }

        item.getComponent(HitboxComponent.class).setLayer(PhysicsLayer.OBSTACLE);
        item.getComponent(ItemComponent.class).setPickupable(false);

        PhysicsComponent phys = item.getComponent(PhysicsComponent.class);
        if (phys != null) phys.setBodyType(BodyDef.BodyType.StaticBody);

        ServiceLocator.getGameArea().spawnEntity(item);

        Vector2 offset = item.getComponent(ItemComponent.class).getEquipOffset();
        player.getComponent(PlayerEquipComponent.class).setItem(item, offset);
    }

    private void unequipItem() {
        player.getComponent(PlayerEquipComponent.class).setItem(null, null);
    }

    // --- extras you already had (kept as-is) ---

    private void spawnGuidanceNpc() {
        var waypoints = List.of(new Vector2(12f, 7f), new Vector2(18f, 7f), new Vector2(25f, 12f));
        Entity guide = FriendlyNPCFactory.createGuidanceNpc(player, waypoints);
        spawnEntityAt(guide, new GridPoint2((int) player.getPosition().x + 2, (int) player.getPosition().y), true, true);

        AnimationRenderComponent arc = guide.getComponent(AnimationRenderComponent.class);
        arc.startAnimation("robot_fire");
        guide.setScale(1.2f, 1.2f);
    }

    public Entity spawnGhostGPTProjectile(Vector2 directionToFire, WeaponsStatsComponent source) {
        Entity laser = ProjectileFactory.createEnemyLaserProjectile(directionToFire, source);
        spawnEntityAt(laser, new GridPoint2(0, 0), true, true);
        PhysicsProjectileComponent laserPhysics = laser.getComponent(PhysicsProjectileComponent.class);
        int projectileSpeed = 5;
        laserPhysics.fire(directionToFire, projectileSpeed);
        return laser;
    }

    private void playMusic() {
        Music music = ServiceLocator.getResourceService().getAsset(BACKGROUND_MUSIC, Music.class);
        music.setLooping(true);
        music.setVolume(0.3f);
        music.play();
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService rs = ServiceLocator.getResourceService();
        ensurePlayerAtlas();
        rs.loadTextures(futuristicTextures);
        rs.loadTextures(keycardTextures);
        rs.loadTextures(generalTextures);
        rs.loadTextures(backgroundTextures);
        rs.loadTextures(forestTextures);
        rs.loadTextures(spawnPadTextures);
        rs.loadTextures(officeTextures);
        rs.loadTextures(securityTextures);
        rs.loadTextures(researchTextures);
        rs.loadTextures(extraTextures);
        rs.loadTextureAtlases(forestTextureAtlases);
        rs.loadSounds(playerSound1);
        rs.loadSounds(forestSounds);
        rs.loadSounds(enemySounds);
        rs.loadMusic(forestMusic);

        while (rs.loadForMillis(10)) {
            logger.info("Loading... {}%", rs.getProgress());
        }
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(keycardTextures);
        rs.unloadAssets(futuristicTextures);
        rs.unloadAssets(playerSound1);
        rs.unloadAssets(backgroundTextures);
        rs.unloadAssets(forestTextures);
        rs.unloadAssets(generalTextures);
        rs.unloadAssets(forestTextureAtlases);
        rs.unloadAssets(forestSounds);
        rs.unloadAssets(forestMusic);
        rs.unloadAssets(spawnPadTextures);
        rs.unloadAssets(officeTextures);
        rs.unloadAssets(securityTextures);
        rs.unloadAssets(extraTextures);
    }

    public Entity getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Forest";
    }
}
