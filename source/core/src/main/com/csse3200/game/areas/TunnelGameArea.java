package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.services.ServiceLocator;

/**
 * Tunnel room: minimal walls with left door back to Server Room.
 */
public class TunnelGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final float ROOM_DIFF_NUMBER = 10;

    private Entity player;

    public TunnelGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    /**
     * Creates the tunnel room with the following steps:
     * - Loads background with GenericLayout methods
     * - spawns the borders and doors
     * - spawns the player in and
     * - spawns the platforms and spawn pads
     * - spawns 2 grok droids as enemies
     * - spawns the floor overlay
     * - spawns items using the ItemSpawner and the tunnelmap configuration
     */
    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.TUNNEL_ROOM,
                new Color(0.08f, 0.08f, 0.12f, 0.28f));

        spawnBordersAndDoors();
        player = spawnPlayer();
        spawnPlatforms();
        spawnSpawnPads();
        spawnGrokDroids();
        spawnObjectDoors();
        spawnTeleporter();

        spawnFloor();

        ItemSpawner itemSpawner = new ItemSpawner(this);
        itemSpawner.spawnItems(ItemSpawnConfig.tunnelmap());

        Entity ui = new Entity();
        ui.addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Tunnel"));
        spawnEntity(ui);
    }

    /**
     * Spawns the borders and doors of the room.
     */
    private void spawnBordersAndDoors() {
        Bounds b = getCameraBounds(cameraComponent);
        addVerticalDoorLeft(b, WALL_WIDTH, this::loadServer);
        addSolidWallTop(b, WALL_WIDTH);
        addSolidWallBottom(b, WALL_WIDTH);
    }

    /**
     * Spawns the player at the designated spawn point PLAYER_SPAWN and then
     * returns the player entity.
     *
     * @return the player entity
     */
    private Entity spawnPlayer() {
        Entity player = PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
        return player;
    }

    /**
     * Two generic big thick platforms and a few small thin platforms above.
     * The big thick platforms can serve as 'cover' for the player,
     * and the thin platforms will require jumping to reach.
     */
    private void spawnPlatforms() {
        Entity platform1 = ObstacleFactory.createThickFloor();
        GridPoint2 platform1Pos = new GridPoint2(10, 6);
        spawnEntityAt(platform1, platform1Pos, true, false);
        Entity platform2 = ObstacleFactory.createThickFloor();
        GridPoint2 platform2Pos = new GridPoint2(20, 6);
        spawnEntityAt(platform2, platform2Pos, true, false);

        Entity thinPlatform1 = ObstacleFactory.createThinFloor();
        GridPoint2 thinPlatform1Pos = new GridPoint2(8, 16);
        spawnEntityAt(thinPlatform1, thinPlatform1Pos, true, false);
        Entity thinPlatform2 = ObstacleFactory.createThinFloor();
        GridPoint2 thinPlatform2Pos = new GridPoint2(22, 16);
        spawnEntityAt(thinPlatform2, thinPlatform2Pos, true, false);
    }

    /**
     * Create the spawn pads for the enemies and items.
     * Red spawn pad spawns enemies, on right side of room.
     * Purple spawn pad spawns items, on top left platform.
     */
    private void spawnSpawnPads() {
        Entity enemyPad = ObstacleFactory.createRedSpawnPad();
        GridPoint2 enemyPadPos = new GridPoint2(25, 6);
        spawnEntityAt(enemyPad, enemyPadPos, true, false);

        Entity itemPad = ObstacleFactory.createPurpleSpawnPad();
        GridPoint2 itemPadPos = new GridPoint2(8, 17);
        spawnEntityAt(itemPad, itemPadPos, true, false);
    }

    /**
     * Spawn 2 high-level grok droids in the room as enemies.
     */
    private void spawnGrokDroids() {
        Entity grok1 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(TunnelGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok1Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok1, grok1Pos, true, false);
        Entity grok2 = NPCFactory.createGrokDroid(player, this,
                ServiceLocator.getDifficulty().getRoomDifficulty(TunnelGameArea.ROOM_DIFF_NUMBER));
        GridPoint2 grok2Pos = new GridPoint2(25, 7);
        spawnEntityAt(grok2, grok2Pos, true, false);
    }

    /** Teleporter bottom-left */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(2f, 3f));
        spawnEntity(tp);
    }

    /**
     * Spawn entity door at the bottom left, and no door to the right
     * as this is the last room (currently).
     */
    private void spawnObjectDoors() {
        Entity leftDoor = ObstacleFactory.createDoor();
        GridPoint2 leftDoorSpawn = new GridPoint2(0, 7);
        spawnEntityAt(leftDoor, leftDoorSpawn, false, false);
    }

    private void loadServer() {
        clearAndLoad(() -> new ServerGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Tunnel";
    }

    @Override
    public Entity getPlayer() {
        // placeholder
        return null;
    }

    public static TunnelGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new TunnelGameArea(terrainFactory, camera));
    }

}
