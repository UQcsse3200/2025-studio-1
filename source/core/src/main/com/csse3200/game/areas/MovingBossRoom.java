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
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.characters.BossFactory;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the room that holds the Ground Moving Boss Boss.
 * This boss is a small robot that moves towards the player and attacks
 * <p>
 * Room is empty except for boss and player
 */
public class MovingBossRoom extends GameArea {
    private static GridPoint2 playerSpawn = new GridPoint2(3, 10);

    private static final Logger logger = LoggerFactory.getLogger(MovingBossRoom.class);
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

        spawnBoss();
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 6));
        spawnAssistor();
        spawnNurse();
        spawnPlatforms();

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.bossmap());

        spawnVisibleFloor();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Moving Boss Room"));
        spawnEntity(ui);
    }

    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
    }

    private void spawnBoss() {
        GridPoint2 pos = new GridPoint2(15, 20);

        Entity boss = BossFactory.createRobot(player);

        boss.getEvents().addListener("death", () -> ServiceLocator.getTimeSource().delayKeycardSpawn(0.05f, () -> {
            Entity keycard = KeycardFactory.createKeycard(1);
            keycard.setPosition(new Vector2(3f, 7f));
            spawnEntity(keycard);
        }));

        spawnEntityAt(boss, pos, true, true);
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
        leftDoor.addComponent(new KeycardGateComponent(1, () -> {
            ColliderComponent collider = leftDoor.getComponent(ColliderComponent.class);
            if (collider != null) collider.setEnabled(false);
            loadSecurity();
        }));
        spawnEntity(leftDoor);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new KeycardGateComponent(1, () -> {
            ColliderComponent collider = rightDoor.getComponent(ColliderComponent.class);
            if (collider != null) collider.setEnabled(false);
            loadOffice();
        }));
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
        MovingBossRoom.playerSpawn = newSpawn;
    }
    private void spawnPlatforms() {
        Entity platform1 = ObstacleFactory.createThinFloor();
        GridPoint2 platform1Pos = new GridPoint2(4, 10);
        spawnEntityAt(platform1, platform1Pos, false, false);

        Entity platform3 = ObstacleFactory.createThinFloor();
        GridPoint2 platform3Pos = new GridPoint2(22, 10);
        spawnEntityAt(platform3, platform3Pos, false, false);
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
}
