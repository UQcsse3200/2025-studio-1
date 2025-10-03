package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.services.ServiceLocator;

/**
 * Research room: futuristic laboratory with desks, pods, microscopes, and
 * screens.
 * Left door -> Elevator, Right door -> Storage.
 */
public class ResearchGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private int roomDiffNumber = 6;
    private Entity player;

    public ResearchGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.RESEARCH_ROOM,
                new Color(0.10f, 0.12f, 0.18f, 0.28f)); // subtle lab-themed overlay
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 20));
        spawnBordersAndDoors();
        player = spawnPlayer();
        spawnPlatforms();
        spawnResearchProps();
        spawnEnemies();
        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.researchmap());
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
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadElevator));
        spawnEntity(leftDoor);

        addSolidWallRight(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.topY() - rightDoorHeight;
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadFlyingBossRoom));
        spawnEntity(rightDoor);
    }

    private Entity spawnPlayer() {
        Entity player = PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
        return player;
    }

    private void spawnPlatforms() {
        for (int i = 0; i < 5; i++) {
            GridPoint2 platformPos = new GridPoint2(i * 6 + 3, 5);
            Entity platform = ObstacleFactory.createThinFloor();
            spawnEntityAt(platform, platformPos, true, false);
        }
        for (int i = 0; i < 2; i++) {
            GridPoint2 topRightPlatformPos = new GridPoint2(1 + i * 6, 11);
            Entity topRightPlatform = ObstacleFactory.createThinFloor();
            spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
        }
        for (int i = 0; i < 2; i++) {
            GridPoint2 topRightPlatformPos = new GridPoint2(27 - i * 6, 19);
            Entity topRightPlatform = ObstacleFactory.createThinFloor();
            spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
        }
        GridPoint2 topRightPlatformPos = new GridPoint2(27, 14);
        Entity topRightPlatform = ObstacleFactory.createThinFloor();
        spawnEntityAt(topRightPlatform, topRightPlatformPos, true, false);
    }

    /**
     * Spawns research-related props:
     * - Research Desk (collidable)
     * - Microscope (decorative)
     * - Research Pod (collidable)
     * - Research Screen (decorative)
     * - Laboratory main station (collidable)
     */
    private void spawnResearchProps() {
        // Desk
        GridPoint2 deskPos = new GridPoint2(5, 11);
        spawnEntityAt(ObstacleFactory.createResearchDesk(), deskPos, true, false);

        // Microscope
        GridPoint2 scopePos = new GridPoint2(27, 15);
        spawnEntityAt(ObstacleFactory.createMicroscope(), scopePos, false, false);

        // Research Pod
        GridPoint2 podPos = new GridPoint2(19, 6);
        spawnEntityAt(ObstacleFactory.createResearchPod(), podPos, true, false);

        // Laboratory main station
        GridPoint2 labPos = new GridPoint2(26, 6);
        spawnEntityAt(ObstacleFactory.createLaboratory(), labPos, true, false);

        for (int i = 0; i < 2; i++) {
            GridPoint2 platPos = new GridPoint2(14 + i * 5, 15 - i * 5);
            Entity plat = ObstacleFactory.createSecurityPlatform();
            spawnEntityAt(plat, platPos, true, false);
        }
    }

    /**
     * Spawn a pair of enemies to keep Research lively.
     */
    private void spawnEnemies() {
        if (player == null)
            return;

        // Vroomba near the bottom platforms
        Entity vroomba = com.csse3200.game.entities.factories.characters.NPCFactory.createVroomba(player,
                ServiceLocator.getDifficulty().getRoomDifficulty(this.roomDiffNumber));
        spawnEntityAt(vroomba, new GridPoint2(6, 6), true, false);

        // Deepspin near the top right area
        Entity deepspin = com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspin(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(this.roomDiffNumber));
        spawnEntityAt(deepspin, new GridPoint2(24, 15), true, false);
    }

    private void loadElevator() {
        ElevatorGameArea.setRoomSpawn(new GridPoint2(21, 20));
        clearAndLoad(() -> new ElevatorGameArea(terrainFactory, cameraComponent));
    }

    private void loadFlyingBossRoom() {
        FlyingBossRoom.setRoomSpawn(new GridPoint2(6, 8));
        clearAndLoad(() -> new FlyingBossRoom(terrainFactory, cameraComponent));
    }

    /**
     * Setter method for the player spawn point
     * should be used when the player is traversing through the rooms
     * 
     * @param newSpawn the new spawn point
     */
    public static void setRoomSpawn(GridPoint2 newSpawn) {
        ResearchGameArea.PLAYER_SPAWN = newSpawn;
    }

    @Override
    public String toString() {
        return "Research";
    }

    @Override
    public Entity getPlayer() {
        // placeholder
        return null;
    }

    public static ResearchGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new ResearchGameArea(terrainFactory, camera));
    }
}
