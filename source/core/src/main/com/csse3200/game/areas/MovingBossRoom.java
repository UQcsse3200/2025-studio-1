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
 * This is the room that holds the Ground Moving Boss Boss.
 * This boss is a small robot that moves towards the player and attacks
 * 
 * Room is empty except for boss and player
 */
public class MovingBossRoom extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(MovingBossRoom.class);

    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(3, 10);

    private static final float WALL_WIDTH = 0.1f;

    private Entity player;

    /**
     * Creates a new MovingBossRoom for the room where the flying boss spawns.
     * 
     * @param terrainFactory  TerrainFactory used to create the terrain for the
     *                        GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera
     *                        (optional but used here).
     * @requires terrainFactory not null
     */
    public MovingBossRoom(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    /**
     * Creates the room by:
     * - loading assest
     * - displaying the UI
     * - spawning terrain (without door triggers)
     * - spawn player and rifle
     * - spawns static boss
     * - spawns floors
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

        spawnBigWall();

        spawnBoss();
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 6));

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.bossmap());

        spawnFloor();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Moving Boss Room"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Moving Boss Room"));
        spawnEntity(ui);
    }

    private Entity spawnPlayer() {
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        return newPlayer;
    }

    private void spawnBoss() {
        GridPoint2 pos = new GridPoint2(15, 20);

        Entity boss = BossFactory.createRobot(player);
        spawnEntityAt(boss, pos, true, true);
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
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadSecurity));
        spawnEntity(leftDoor);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadOffice));
        spawnEntity(rightDoor);
    }

    public Entity getPlayer() {
        return player;
    }

    public void loadSecurity() {
        clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
    }

    public void loadOffice() {
        clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
    }
}
