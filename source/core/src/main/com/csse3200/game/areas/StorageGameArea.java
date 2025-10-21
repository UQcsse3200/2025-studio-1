package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;

/**
 * The "Storage" area of the game map. This class:
 * - Builds the terrain (background)
 * - Spawns the player and necessary props
 * - Generates the doors to the previous and next room
 */
public class StorageGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(4, 20);
    private static boolean isCleared = false;

    /**
     * Initialise this StorageGameArea to use the provided TerrainFactory and camera
     * helper.
     * The camera is used to size the screen-edge walls and place the right-side
     * door trigger.
     *
     * @param terrainFactory  TerrainFactory used to create the terrain for the
     *                        GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera
     *                        (optional but used here).
     * @requires terrainFactory != null
     */
    public StorageGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);

        this.getEvents().addListener("room cleared", StorageGameArea::clearRoom);
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
        StorageGameArea.playerSpawn = newSpawn;
    }

    public static StorageGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {

        return (new StorageGameArea(terrainFactory, camera));
    }

    /**
     * Create the game area, including terrain, static entities (platforms), dynamic
     * entities (player)
     * Entry point for this room. This:
     * - Loads textures
     * - Creates the terrain, walls, and UI label
     * - Spawns player, props, and enemies (to be added)
     */
    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.STORAGE,
                new Color(0.12f, 0.12f, 0.10f, 0.26f));

        spawnBordersAndDoors();
        Entity player = spawnPlayer();
        spawnFloor();
        spawnShipmentBoxLid();
        spawnConveyor();
        spawnTeleporter();

        if (!StorageGameArea.isCleared) {
            startWaves(player);
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.storage1map());
        }

        displayUIEntity("Storage", "Floor 9");
    }

    /**
     * Creates a platform atop the boxes in the pile that has physics colliders.
     */
    private void spawnShipmentBoxLid() {
        float lidX = 5.05f;
        float lidY = 6.05f;

        Entity boxLid = ObstacleFactory.createShipmentBoxes();
        boxLid.setPosition(lidX, lidY);

        spawnEntity(boxLid);
    }

    /**
     * Creates a platform atop the conveyor that has physics colliders.
     */
    private void spawnConveyor() {
        float conveyorX = 0f;
        float conveyorY = 8f;

        Entity conveyor = ObstacleFactory.createConveyor();
        conveyor.setPosition(conveyorX, conveyorY);

        spawnEntity(conveyor);
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        addSolidWallTop(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = 8f;
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadShipping));
        spawnEntity(leftDoor);


        Entity leftDoorSprite = ObstacleFactory.createDoor();
        leftDoorSprite.setPosition(b.leftX(), leftDoorY);
        spawnEntity(leftDoorSprite);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = 4f;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadServer));
        spawnEntity(rightDoor);


        Entity rightDoorSprite = ObstacleFactory.createDoor();
        rightDoorSprite.setPosition(b.rightX() - WALL_WIDTH - 1.0f, rightDoorY);
        spawnEntity(rightDoorSprite);

        if (!StorageGameArea.isCleared) registerDoors(new Entity[]{leftDoor, rightDoor});
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    /** Teleporter bottom-left */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(2f, 3.2f));
        spawnEntity(tp);
    }

    /**
     * Clears the game area and loads the next section (Servers).
     */
    private void loadServer() {
        ServerGameArea.setRoomSpawn(new GridPoint2(6, 8));
        clearAndLoad(() -> new ServerGameArea(terrainFactory, cameraComponent));
    }

    /**
     * Clears the game area and loads the previous section (Shipping).
     */
    private void loadShipping() {
        ShippingGameArea.setRoomSpawn(new GridPoint2(26, 20));
        clearAndLoad(() -> new ShippingGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Storage";
    }

    @Override
    public Entity getPlayer() {
        // placeholder
        return null;
    }

    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        StorageGameArea.isCleared = true;
        logger.debug("Storage is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        StorageGameArea.isCleared = false;
        logger.debug("Storage is uncleared");
    }
}