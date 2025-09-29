package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.pool.PoolGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.components.minigames.whackamole.WhackAMoleGame;

/**
 * Minimal generic Casino room: walls, a single right-side door, and a subtle background overlay.
 */
public class CasinoGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(25, 10);
    private Entity player;

    public CasinoGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.CASINO,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));

        ensureAssets();
        spawnBordersAndDoors();
        spawnFloor();
        player = spawnPlayer();
        spawnWhackAMoleGame();
        spawnPoolGame();
        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.securitymap());
    }

    private void ensureAssets() {
        String[] needed = new String[]{
                "images/mole.png",
                "images/hole.png",
                "images/pool/table.png",
                "images/pool/cueball.png",
                "images/pool/cue.png"
        };
        ensureTextures(needed);
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);

        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.4f);
        float rightDoorY = b.bottomY();

        // Top segment of the right wall (from top of door to ceiling)
        float topSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (topSegHeight > 0f) {
            Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, topSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }

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

    private void spawnWhackAMoleGame() {
        GridPoint2 pos = new GridPoint2(5, 7);
        spawnEntityAt(new WhackAMoleGame().getGameEntity(), pos, true, true);
    }

    private void spawnPoolGame() {
        GridPoint2 pos = new GridPoint2(10, 7);
        spawnEntityAt(new PoolGame().getGameEntity(), pos, true, true);
    }

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
