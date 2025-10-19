package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopDemo;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.ShopFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.services.ServiceLocator;

/**
 * Minimal generic Security room: walls, doors, and a subtle background overlay.
 */
public class SecurityGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private static final float ROOM_DIFF_NUMBER = 2;
    private Entity player;

    public SecurityGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public static SecurityGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new SecurityGameArea(terrainFactory, camera));
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
        SecurityGameArea.playerSpawn = newSpawn;
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SECURITY_ROOM,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));

        spawnBordersAndDoors();
        player = spawnPlayer();
        spawnPlatforms();
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 19));
        spawnSecurityProps();
        spawnEnemies();
        spawnTeleporter();
        spawnSpikes2();
        spawnShopKiosk();
        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.securitymap());

        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Security"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 4"));
        spawnEntity(ui);
    }
    private void spawnShopKiosk() {
        CatalogService catalog = ShopDemo.makeDemoCatalog();
        ShopManager manager = new ShopManager(catalog);
        Entity shop = ShopFactory.createShop(this, manager, "images/VendingMachine.png"); // have as tree now as placeholder, later need to change to actual shop icon
        spawnEntityAt(shop, new GridPoint2(26, 6), true, false);
    }
    private void spawnBordersAndDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY();
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor5));
        spawnEntity(leftDoor);
        addSolidWallTop(b, WALL_WIDTH);
        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.topY() - rightDoorHeight;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadMovingBossRoom));
        spawnEntity(rightDoor);
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    /**
     * Spawns thin floor platforms in the room, including
     * an extra platform below the top-right door.
     */
    private void spawnPlatforms() {
        for (int i = 0; i < 6; i++) {
            GridPoint2 platformPos = new GridPoint2(i * 5 + 2, 5);
            Entity platform = ObstacleFactory.createThinFloor();
            spawnEntityAt(platform, platformPos, true, false);
        }
        for (int i = 0; i < 2; i++) {
            GridPoint2 topRightPlatformPos = new GridPoint2(1 + i * 6, 11);
            Entity topRightPlatform = ObstacleFactory.createThinFloor();
            spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
        }
        /* Extra platform just below the top-right door **/
        GridPoint2 topRightPlatformPos = new GridPoint2(26, 18);
        Entity topRightPlatform = ObstacleFactory.createThinFloor();
        spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
    }

    /**
     * Spawns security-related props, including:
     * Security system (collidable)
     * Red light (decorative)
     * Monitor (decorative)
     * Large security camera (decorative)
     * Security platforms (collidable)
     */
    private void spawnSecurityProps() {

        /* Security System (collidable) **/
        GridPoint2 systemPos = new GridPoint2(57, 6);
        Entity system = ObstacleFactory.createSecuritySystem();
        spawnEntityAt(system, systemPos, true, false);

        /* Red light (decorative) **/
        GridPoint2 redLightPos = new GridPoint2(14, 22);
        Entity redLight = ObstacleFactory.createRedLight();
        spawnEntityAt(redLight, redLightPos, false, false);

        /* Monitor (decorative) **/
        GridPoint2 monitorPos = new GridPoint2(6, 12);
        Entity monitor = ObstacleFactory.createSecurityMonitor();
        spawnEntityAt(monitor, monitorPos, false, false);

        /* Security camera (decorative, from ObstacleFactory) **/
        GridPoint2 cameraPos = new GridPoint2(2, 19);
        Entity securityCamera = ObstacleFactory.createLargeSecurityCamera();
        spawnEntityAt(securityCamera, cameraPos, false, false);

        /* 2 Security Platforms (collidable) **/
        for (int i = 0; i < 2; i++) {
            GridPoint2 platPos = new GridPoint2(24 - i * 5, 10 + i * 4);
            Entity plat = ObstacleFactory.createSecurityPlatform();
            spawnEntityAt(plat, platPos, true, false);
        }
    }

    private void spawnSpikes2() {
        Entity spikes = ObstacleFactory.createSpikes2();
        GridPoint2 spikesSpawn = new GridPoint2(4, 12);
        spawnEntityAt(spikes, spikesSpawn, true, false);
    }

    /**
     * Spawn a Vroomba and Deepspin in Security room.
     */
    private void spawnEnemies() {
        if (player == null)
            return;

        Entity vroomba = com.csse3200.game.entities.factories.characters.NPCFactory.createVroomba(player,
                ServiceLocator.getDifficulty().getRoomDifficulty(SecurityGameArea.ROOM_DIFF_NUMBER));
        spawnEntityAt(vroomba, new GridPoint2(8, 6), true, false);

        Entity deepspin = com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspin(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(SecurityGameArea.ROOM_DIFF_NUMBER));
        spawnEntityAt(deepspin, new GridPoint2(22, 12), true, false);
    }

    /**
     * Teleporter entity bottom-left
     */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(2f, 2.8f));
        spawnEntity(tp);
    }

    private void loadBackToFloor5() {
        MainHall.setRoomSpawn(new GridPoint2(24, 20));
        clearAndLoad(() -> new MainHall(terrainFactory, cameraComponent));
    }

    private void loadMovingBossRoom() {
        MovingBossRoom.setRoomSpawn(new GridPoint2(6, 8));
        clearAndLoad(() -> new MovingBossRoom(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Security";
    }

    @Override
    public Entity getPlayer() {
        return player;
    }
}
