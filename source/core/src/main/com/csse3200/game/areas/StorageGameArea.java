package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.services.ServiceLocator;

/**
 * The "Storage" area of the game map. This class:
 * - Builds the terrain (background)
 * - Spawns the player and necessary props
 * - Generates the doors to the previous and next room
 */
public class StorageGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(4, 20);
    private static final float ROOM_DIFF_NUMBER = 8;
    private Entity player;

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
        player = spawnPlayer();
        spawnFloor();
        spawnShipmentBoxLid();
        spawnConveyor();
        spawnGrokDroids();

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.storage1map());

        Entity ui = new Entity();
        ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Storage"));
        spawnEntity(ui);
    }

    /**
     * Creates a platform atop the boxes in the pile that has physics colliders.
     */
    private void spawnShipmentBoxLid() {
        float lidX = 5.05f;
        float lidY = 6.05f;

        Entity BoxLid = ObstacleFactory.createShipmentBoxes();
        BoxLid.setPosition(lidX, lidY);

        spawnEntity(BoxLid);
    }

    /**
     * Creates a platform atop the conveyor that has physics colliders.
     */
    private void spawnConveyor() {
        float conveyorX = 0f;
        float conveyorY = 8f;

        Entity Conveyor = ObstacleFactory.createConveyor();
        Conveyor.setPosition(conveyorX, conveyorY);

        spawnEntity(Conveyor);
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = 8f;
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadShipping));
        spawnEntity(leftDoor);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = 4f;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadServer));
        spawnEntity(rightDoor);
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(PLAYER_SPAWN);
    }

    /**
     * Spawn 2 high-level grok droids in the room as enemies.
     */
    private void spawnGrokDroids() {
        Entity grok1 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(StorageGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok1Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok1, grok1Pos, true, false);
        Entity grok2 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(StorageGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok2Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok2, grok2Pos, true, false);
    }

    /**
     * Clears the game area and loads the next section (Servers).
     */
    private void loadServer() {
        clearAndLoad(() -> new ServerGameArea(terrainFactory, cameraComponent));
    }

    /**
     * Clears the game area and loads the previous section (Shipping).
     */
    private void loadShipping() {
        roomNumber++;
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

    public static StorageGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {

        return (new StorageGameArea(terrainFactory, camera));
    }
}
