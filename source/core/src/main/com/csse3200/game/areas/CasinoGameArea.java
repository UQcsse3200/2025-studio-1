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

/**
 * Minimal generic Casino room: walls, a single right-side door, and a subtle background overlay.
 */
public class CasinoGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private Entity player;

    public CasinoGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.CASINO,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));

        spawnBordersAndDoors();
        player = spawnPlayer();
        // spawnPlatforms();
        // spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 19));
        // spawnSecurityProps();
        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.securitymap());
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        // float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        // float leftDoorY = b.bottomY();
        // Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        // leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        // leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor5));
        // spawnEntity(leftDoor);

        // addSolidWallRight(b, WALL_WIDTH);
        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadSpawnFromCasino));
        spawnEntity(rightDoor);
    }

    private Entity spawnPlayer() {
        Entity player = PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
        return player;
    }

//    private void loadBackToFloor2() {
//        roomNumber--;
//        clearAndLoad(() -> new MainHall(terrainFactory, cameraComponent));
//    }

    private void loadSpawnFromCasino() {
        clearAndLoad(() -> new ForestGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Casino";
    }

    @Override
    public Entity getPlayer() {
        return player;
    }

    public static CasinoGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new CasinoGameArea(terrainFactory, camera));
    }
}
