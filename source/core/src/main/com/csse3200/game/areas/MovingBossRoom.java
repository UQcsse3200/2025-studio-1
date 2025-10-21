package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This is the room that holds the Ground Moving Boss Boss.
 * This boss is a small robot that moves towards the player and attacks
 * <p>
 * Room is empty except for boss and player
 */
public class MovingBossRoom extends GameArea {
    private static GridPoint2 playerSpawn = new GridPoint2(3, 10);
    private static boolean isCleared = false;

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

        this.getEvents().addListener("room cleared", MovingBossRoom::clearRoom);
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
        spawnBossAndItems();
        displayUIEntity("Moving Boss Room", null);

        player = spawnPlayer();

        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 6));
        spawnAssistor();
        spawnNurse();

        spawnVisibleFloor();
    }

    public void spawnBossAndItems() {
        if (!MovingBossRoom.isCleared) {
            spawnBoss();
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.bossmap());
        }
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    public void spawnBoss() {
        GridPoint2 pos = new GridPoint2(15, 20);

        Entity boss = BossFactory.createRobot(player);

        boss.getEvents().addListener("death", () -> ServiceLocator.getTimeSource().delayKeycardSpawn(0.05f, () -> {
            Entity keycard = KeycardFactory.createKeycard(2);
            keycard.setPosition(new Vector2(3f, 7f));
            spawnEntity(keycard);
        }));

        spawnEntityAt(boss, pos, true, true);
        registerEnemy(boss);
    }

    private void spawnAssistor() {
        GridPoint2 pos = new GridPoint2(7, 8);

        Entity assistor = FriendlyNPCFactory.createAssisterNpc(player);
        spawnEntityAt(assistor, pos, true, true);
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
        rightDoor.addComponent(new KeycardGateComponent(2, () -> {
            ColliderComponent collider = rightDoor.getComponent(ColliderComponent.class);
            if (collider != null) collider.setEnabled(false);
            loadOffice();
        }));
        spawnEntity(rightDoor);

        if (!MovingBossRoom.isCleared) registerDoors(new Entity[]{leftDoor});
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
        MovingBossRoom.playerSpawn = newSpawn;
    }

    public Entity getPlayer() {
        return player;
    }

    public void loadSecurity() {
        SecurityGameArea.setRoomSpawn(new GridPoint2(24, 22));
        clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
    }

    public void loadOffice() {
        OfficeGameArea.setRoomSpawn(new GridPoint2(2, 14));
        clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
    }


    private void spawnNurse() {
        GridPoint2 pos = new GridPoint2(20, 8); // 在地图右侧,与Assistor对称

        Entity nurse = FriendlyNPCFactory.createNurseNpc(player);
        spawnEntityAt(nurse, pos, true, true);
    }

    @Override
    public String toString() {
        return "MovingBoss";
    }

    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        MovingBossRoom.isCleared = true;
        logger.debug("Moving Boss Room is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        MovingBossRoom.isCleared = false;
        logger.debug("Moving Boss Room is uncleared");
    }

    /**
     * FOR TESTING PURPOSES
     */
    public static boolean getClearField() {
        return MovingBossRoom.isCleared;
    }
}
