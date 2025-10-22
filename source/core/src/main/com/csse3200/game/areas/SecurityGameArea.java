package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;


/**
 * Minimal generic Security room: walls, doors, and a subtle background overlay.
 */
public class SecurityGameArea extends GameArea {
    private Entity player;
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private static boolean isCleared = false;

    public SecurityGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);

        this.getEvents().addListener("room cleared", SecurityGameArea::clearRoom);
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
        spawnTeleporter();
        spawnSpikes2();
        spawnNurse(player);

        spawnEnemiesAndWeapons();
        displayUIEntity("Security", "Floor 4");
    }

    public void spawnEnemiesAndWeapons() {
        if (!SecurityGameArea.isCleared) {
            startWaves(player);
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.securitymap());
        }
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

        if (!SecurityGameArea.isCleared) registerDoors(new Entity[]{leftDoor, rightDoor});
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
        GridPoint2 systemPos = new GridPoint2(27, 6);
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
     * Teleporter entity bottom-left
     */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(2f, 2.8f));
        spawnEntity(tp);
    }

    private void loadBackToFloor5() {
        MainHall.setRoomSpawn(new GridPoint2(25, 17));
        clearAndLoad(() -> new MainHall(terrainFactory, cameraComponent));
    }

    private void loadMovingBossRoom() {
        MovingBossRoom.setRoomSpawn(new GridPoint2(1, 7));
        clearAndLoad(() -> new MovingBossRoom(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Security";
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
        SecurityGameArea.isCleared = true;
        logger.debug("Security is cleared");
    }

    private void spawnNurse(Entity player) {
        GridPoint2 pos = new GridPoint2(20, 8);

        Entity nurse = FriendlyNPCFactory.createNurseNpc(player);
        spawnEntityAt(nurse, pos, true, true);
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        SecurityGameArea.isCleared = false;
        logger.debug("Security is uncleared");
    }
}
