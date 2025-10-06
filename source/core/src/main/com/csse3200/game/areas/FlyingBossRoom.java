package com.csse3200.game.areas;


import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.gamearea.GameAreaDisplay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the room that holds the Flying Boss.
 * The boss is a flying enemy that spawns at the top of the map and
 * shoots projectiles at the player.
 * 
 * There are two platforms that can possibly server as cover as well as a floor
 * at the bottom
 */
public class FlyingBossRoom extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(FlyingBossRoom.class);
    private static GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

    private static GridPoint2 playerSpawn = new GridPoint2(3, 10);

    private static final float WALL_WIDTH = 0.1f;

    private Entity player;

    /**
     * Creates a new FlyingBossRoom for the room where the flying boss spawns.
     * 
     * @param terrainFactory  TerrainFactory used to create the terrain for the
     *                        GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera
     *                        (optional but used here).
     * @requires terrainFactory not null
     */
    public FlyingBossRoom(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    /**
     * Creates the room by:
     * - spawning doors
     * - displaying the UI
     * - spawns player
     * - spawns platforms
     * - spanws walls
     * - spawns the flying boss
     * - spawns items
     */
    @Override
    public void create() {
        ServiceLocator.registerGameArea(this);

        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SERVER_ROOM,
                new Color(0.10f, 0.12f, 0.10f, 0.24f));

        spawnBordersAndDoors();
        displayUI();

        player = spawnPlayer();

        spawnPlatforms();
        spawnBigWall();

        spawnFlyingBoss();
        spawnObjectDoors(new GridPoint2(0, 7), new GridPoint2(28, 7));

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.bossmap());

        spawnVisibleFloor();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Flying Boss Room"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Flying Boss Room"));
        spawnEntity(ui);
    }

    private void spawnPlatforms() {
        Entity platform1 = ObstacleFactory.createThinFloor();
        GridPoint2 platform1Pos = new GridPoint2(4, 10);
        spawnEntityAt(platform1, platform1Pos, false, false);

        Entity platform3 = ObstacleFactory.createThinFloor();
        GridPoint2 platform3Pos = new GridPoint2(22, 10);
        spawnEntityAt(platform3, platform3Pos, false, false);
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(PLAYER_SPAWN);
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
     * Spawns the borders and doors of the room.
     * Different to genericLayout as the right door is up high
     * at the third platform level.
     */
    private void spawnBordersAndDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY();
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadResearch));
        spawnEntity(leftDoor);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadShipping));
        spawnEntity(rightDoor);
    }

    /**
     * Setter method for the player spawn point
     * should be used when the player is traversing through the rooms
     * 
     * @param newSpawn the new spawn point
     */
    public static void setRoomSpawn(GridPoint2 newSpawn) {
        if (newSpawn == null) {
            return;
        }
        FlyingBossRoom.PLAYER_SPAWN = newSpawn;
    }

    public Entity getPlayer() {
        return player;
    }

    public void loadShipping() {
        ShippingGameArea.setRoomSpawn(new GridPoint2(4, 8));
        clearAndLoad(() -> new ShippingGameArea(terrainFactory, cameraComponent));
    }

    public void loadResearch() {
        ResearchGameArea.setRoomSpawn(new GridPoint2(25, 24));
        clearAndLoad(() -> new ResearchGameArea(terrainFactory, cameraComponent));
    }
}
