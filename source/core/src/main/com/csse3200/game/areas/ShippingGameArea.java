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
 * The "Shipping" area of the game map. This class:
 * - Builds the terrain (background)
 * - Spawns the player and necessary props
 * - Generates the doors to the previous and next room
 */
public class ShippingGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private static boolean isCleared = false;

    /**
     * Initialise this ShippingGameArea to use the provided TerrainFactory and
     * camera helper.
     * The camera is used to size the screen-edge walls and place the right-side
     * door trigger.
     *
     * @param terrainFactory  TerrainFactory used to create the terrain for the
     *                        GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera
     *                        (optional but used here).
     * @requires terrainFactory != null
     */
    public ShippingGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);

        this.getEvents().addListener("room cleared", ShippingGameArea::clearRoom);
    }


    public static ShippingGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new ShippingGameArea(terrainFactory, camera));
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
        ShippingGameArea.playerSpawn = newSpawn;
    }

    /**
     * Create the game area, including terrain, static entities (trees), dynamic
     * entities (player)
     * Entry point for this room. This:
     * - Loads textures
     * - Creates the terrain, walls, and UI label
     * - Spawns player, props, and enemies (to be added)
     */
    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SHIPPING,
                new Color(0.12f, 0.12f, 0.10f, 0.26f));

        spawnBordersAndDoors();
        Entity player = spawnPlayer();
        spawnFloor();
        spawnShipmentBoxLid();
        spawnShipmentCrane();
        spawnConveyor();
        spawnTeleporter();

        if (!ShippingGameArea.isCleared) {
            startWaves(player);
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.shippingmap());
        }

        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Shipping"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 8"));
        spawnEntity(ui);
    }

    /**
     * Creates a platform atop the boxes in the truck that has physics colliders.
     */
    private void spawnShipmentBoxLid() {
        float lidX = 7.25f;
        float lidY = 5.1f;

        Entity BoxLid = ObstacleFactory.createShipmentBoxes();
        BoxLid.setPosition(lidX, lidY);

        spawnEntity(BoxLid);
    }

    /**
     * Creates a platform atop the crane that has physics colliders.
     */
    private void spawnShipmentCrane() {
        float craneX = 8.7f;
        float craneY = 7.85f;

        Entity ShipmentCrane = ObstacleFactory.createShipmentCrane();
        ShipmentCrane.setPosition(craneX, craneY);

        spawnEntity(ShipmentCrane);
    }

    /**
     * Creates a platform atop the conveyor that has physics colliders.
     */
    private void spawnConveyor() {
        float conveyorX = 10.7f;
        float conveyorY = 8f;

        Entity Conveyor = ObstacleFactory.createConveyor();
        Conveyor.setPosition(conveyorX, conveyorY);

        spawnEntity(Conveyor);
    }

    /**
     * Bottom-left teleporter
     */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(0.5f, 3f));
        spawnEntity(tp);
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        addSolidWallTop(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY();
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadFlyingBossRoom));
        spawnEntity(leftDoor);


        Entity leftDoorSprite = ObstacleFactory.createDoor();
        leftDoorSprite.setPosition(b.leftX(), leftDoorY);
        spawnEntity(leftDoorSprite);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = 8f;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadStorage));
        spawnEntity(rightDoor);


        Entity rightDoorSprite = ObstacleFactory.createDoor();
        rightDoorSprite.setPosition(b.rightX() - WALL_WIDTH - 1.0f, rightDoorY);
        spawnEntity(rightDoorSprite);

        if (!ShippingGameArea.isCleared) registerDoors(new Entity[]{leftDoor, rightDoor});
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    /**
     * Clears the game area and loads the previous section (Research).
     */
    private void loadFlyingBossRoom() {
        FlyingBossRoom.setRoomSpawn(new GridPoint2(24, 8));
        clearAndLoad(() -> new FlyingBossRoom(terrainFactory, cameraComponent));
    }

    /**
     * Clears the game area and loads the next section (Storage).
     */
    private void loadStorage() {
        StorageGameArea.setRoomSpawn(new GridPoint2(4, 20));
        clearAndLoad(() -> new StorageGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Shipping";
    }

    public Entity getPlayer() {
        // placeholder
        return null;
    }

    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        ShippingGameArea.isCleared = true;
        logger.debug("Shipping is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        ShippingGameArea.isCleared = false;
        logger.debug("Shipping is uncleared");
    }
}
