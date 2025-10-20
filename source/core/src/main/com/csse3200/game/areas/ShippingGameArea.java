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
import com.csse3200.game.entities.factories.LightFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.services.ServiceLocator;

/**
 * The "Shipping" area of the game map. This class:
 * - Builds the terrain (background)
 * - Spawns the player and necessary props
 * - Generates the doors to the previous and next room
 */
public class ShippingGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final float ROOM_DIFF_NUMBER = 7;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private Entity player;

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

        var ls = ServiceLocator.getLightingService();
        if (ls != null && ls.getEngine() != null) {
            ls.getEngine().setAmbientLight(0.65f);
            ls.getEngine().getRayHandler().setShadows(true);
        }

        spawnCeilingCones();
        spawnBordersAndDoors();
        player = spawnPlayer();
        spawnGrokDroids();
        spawnVroombaAndDeepspin();
        spawnFloor();
        spawnShipmentBoxLid();
        spawnShipmentCrane();
        spawnConveyor();
        spawnTeleporter();

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.shippingmap());

        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Shipping"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 8"));
        spawnEntity(ui);
    }

    private void spawnCeilingCones() {
        // Warm-ish cone spotlights from ceiling pointing straight down (-90 degrees)
        var warm = new Color(0.37f, 0.82f, 0.9f, 0.95f); // tweak alpha for brightness
        int rays = 96;
        float dist = 7f;    // reach of the cone
        boolean xray = true; // true = no hard shadows (so it stays “clean”)

        // positions above your play areas (Y slightly below top wall so the hotspot hits tables)
        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(4, 21), true, true);

        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(12, 21), true, true);

        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(27, 21), true, true);
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

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = 8f;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadStorage));
        spawnEntity(rightDoor);
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    /**
     * Spawn 2 high-level grok droids in the room as enemies.
     */
    private void spawnGrokDroids() {
        Entity grok1 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(ShippingGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok1Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok1, grok1Pos, true, false);
        Entity grok2 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(ShippingGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok2Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok2, grok2Pos, true, false);
    }

    /**
     * Spawn a Vroomba and a Deepspin to diversify Shipping enemies.
     */
    private void spawnVroombaAndDeepspin() {
        // Vroomba on the left side floor
        Entity vroomba = NPCFactory.createVroomba(player,
                ServiceLocator.getDifficulty().getRoomDifficulty(ShippingGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 vPos = new GridPoint2(6, 7);
        spawnEntityAt(vroomba, vPos, true, false);

        // Deepspin on the right side near crates
        Entity deepspin = NPCFactory.createDeepspin(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(ShippingGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 dPos = new GridPoint2(22, 8);
        spawnEntityAt(deepspin, dPos, true, false);
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
}
