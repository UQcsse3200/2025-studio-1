package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * This is the room that holds the static Boss.
 * The boss is a static enemy that spawns on the floor and
 * shoots projectiles outwards from itself. Most
 * challenging boss.
 * <p>
 * Room is empty except for boss and player
 */
public class StaticBossRoom extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(3, 10);
    private static boolean isCleared;
    private Entity player;

    /**
     * Creates a new StaticBossRoom for the room where the static boss spawns.
     *
     * @param terrainFactory  TerrainFactory used to create the terrain for the
     *                        GameArea (required).
     * @param cameraComponent Camera helper supplying an OrthographicCamera
     *                        (optional but used here).
     * @requires terrainFactory not null
     */
    public StaticBossRoom(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);

        this.getEvents().addListener("room cleared", StaticBossRoom::clearRoom);
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
        StaticBossRoom.playerSpawn = newSpawn;
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
        displayUIEntity("Static Boss Room", null);

        player = spawnPlayer();

        spawnObjectDoors(new GridPoint2(0, 7), new GridPoint2(28, 7));

        if (!StaticBossRoom.isCleared) {
            spawnBoss();
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.bossmap());
        }

        spawnVisibleFloor();
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    private void spawnBoss() {
        GridPoint2 pos = new GridPoint2(25, 12);

        Entity boss = BossFactory.createBoss3(player);

        // Delay keycard spawn after boss death
        boss.getEvents().addListener("death",
                () -> ServiceLocator.getTimeSource().delayKeycardSpawn(0.05f, () -> {
                    Entity keycard = KeycardFactory.createKeycard(3);
                    keycard.setPosition(new Vector2(3f, 10f)); // adjust position if needed
                    spawnEntity(keycard);
                }));

        spawnEntityAt(boss, pos, true, true);
        registerEnemy(boss);
    }

    /**
     * Spawns the borders and doors of the room.
     */
    private void spawnBordersAndDoors() {
        if (cameraComponent == null) return;

        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallTop(b, WALL_WIDTH);
        addSolidWallRight(b, WALL_WIDTH);

        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY(); // ground level
        float leftTopSegHeight = Math.max(0f, b.topY() - (leftDoorY + leftDoorHeight));
        if (leftTopSegHeight > 0f) {
            Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
            leftTop.setPosition(b.leftX(), leftDoorY + leftDoorHeight);
            spawnEntity(leftTop);
        }
        

        Entity leftDoorWall = ObstacleFactory.createWall(WALL_WIDTH, leftDoorHeight);
        leftDoorWall.setPosition(b.leftX(), leftDoorY);
        spawnEntity(leftDoorWall);
        
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        leftDoor.addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE));
        leftDoor.addComponent(new DoorComponent(this::loadTunnel));
        spawnEntity(leftDoor);


        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new ColliderComponent().setLayer(PhysicsLayer.OBSTACLE));
        rightDoor.addComponent(new HitboxComponent().setLayer(PhysicsLayer.OBSTACLE));
        rightDoor.addComponent(new KeycardGateComponent(3, () -> {
            ColliderComponent collider = rightDoor.getComponent(ColliderComponent.class);
            if (collider != null) collider.setEnabled(false);
            loadSecretRoom();
        }));
        spawnEntity(rightDoor);

        if (!StaticBossRoom.isCleared) registerDoors(new Entity[]{leftDoor});
    }

    public void loadSecretRoom() {
        clearAndLoad(() -> new SecretRoomGameArea(terrainFactory, cameraComponent));
    }


    public Entity getPlayer() {
        return player;
    }

    public void loadTunnel() {
        TunnelGameArea.setRoomSpawn(new GridPoint2(26, 8));
        clearAndLoad(() -> new TunnelGameArea(terrainFactory, cameraComponent));
    }


    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        StaticBossRoom.isCleared = true;
        logger.debug("Static Boss Room is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        StaticBossRoom.isCleared = false;
        logger.debug("Static Boss Room is uncleared");
    }
}