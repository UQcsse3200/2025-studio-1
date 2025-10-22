package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.stations.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemSpawnConfig;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.entities.spawner.ItemSpawner;
import com.csse3200.game.lighting.LightSpawner;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;

/**
 * Tunnel room: minimal walls with left door back to Server Room.
 */
public class TunnelGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(5, 7);
    private static boolean isCleared = false;

    public static volatile DoorComponent exposedRightDoor;

    public TunnelGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);

        this.getEvents().addListener("room cleared", TunnelGameArea::clearRoom);
    }

    public static TunnelGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new TunnelGameArea(terrainFactory, camera));
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

        //Checks to see if the lighting service is not null and then sets the ambient light and turns on shadows for the room.
        var ls = ServiceLocator.getLightingService();
        if (ls != null && ls.getEngine() != null) {
            ls.getEngine().setAmbientLight(0.65f);
            ls.getEngine().getRayHandler().setShadows(true);
        }

        LightSpawner.spawnCeilingCones(
                this,
                List.of(
                new GridPoint2(4,21),
                new GridPoint2(12,21),
                new GridPoint2(20,21),
                new GridPoint2(27,21)
                ),
                new Color(0.67f, 0.19f, 0.19f, 0.95f)
        );

        spawnBordersAndDoors();
        Entity player = spawnPlayer();
        spawnPlatforms();
        spawnSpawnPads();
        spawnTeleporter();
        spawnObjectDoors(new GridPoint2(0, 7), new GridPoint2(28, 7));
        spawnFloor();
        spawnPasswordTerminal(new GridPoint2(22, 17));
        spawnSpikes();
        spawnVisibleFloor();
        spawnNurse(player);

        if (!TunnelGameArea.isCleared) {
            startWaves(player);
            ItemSpawner itemSpawner = new ItemSpawner(this);
            itemSpawner.spawnItems(ItemSpawnConfig.tunnelmap());
        }

        displayUIEntity("Tunnel", "Floor 11");
    }

    /**
     * Spawns the borders and doors of the room.
     */
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
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadServer));
        spawnEntity(leftDoor);

        // Right wall with door: create wall segments above and below the door
        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        

        float rightTopSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (rightTopSegHeight > 0f) {
            Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }
        

        Entity rightDoorWall = ObstacleFactory.createWall(WALL_WIDTH, rightDoorHeight);
        rightDoorWall.setPosition(b.rightX() - WALL_WIDTH, rightDoorY);
        spawnEntity(rightDoorWall);
        
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBossRoom));
        spawnEntity(rightDoor);

        DoorComponent rightDoorComp = rightDoor.getComponent(DoorComponent.class);
        rightDoorComp.setLocked(true);
        TunnelGameArea.exposedRightDoor = rightDoorComp;

        if (!TunnelGameArea.isCleared) registerDoors(new Entity[]{leftDoor, rightDoor});
    }

    /**
     * Spawns the player at the designated spawn point playerSpawn and then
     * returns the player entity.
     *
     * @return the player entity
     */
    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(playerSpawn);
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
     * Spawn the spikes
     */
    private void spawnSpikes() {
        Entity spikes = ObstacleFactory.createSpikes();
        GridPoint2 spikesSpawn = new GridPoint2(15, 6);
        spawnEntityAt(spikes, spikesSpawn, true, false);
    }

    /**
     * Teleporter bottom-left
     */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(2f, 3f));
        spawnEntity(tp);
    }

    private void loadServer() {
        ServerGameArea.setRoomSpawn(new GridPoint2(25, 20));
        clearAndLoad(() -> new ServerGameArea(terrainFactory, cameraComponent));
    }

    private void loadBossRoom() {
        StaticBossRoom.setRoomSpawn(new GridPoint2(1, 7));
        clearAndLoad(() -> new StaticBossRoom(terrainFactory, cameraComponent));
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
        TunnelGameArea.playerSpawn = newSpawn;
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

    /**
     * Spawns a password terminal and a nearby hint station in the given position.
     */
    private void spawnPasswordTerminal(GridPoint2 pos) {
        Entity terminal = ObstacleFactory.createSecuritySystem();
        spawnEntityAt(terminal, pos, true, false);

        Entity hintStation = InteractableStationFactory.createBaseStation();
        hintStation.addComponent(new StationComponent(makeTerminalHintConfig()));

        PhysicsUtils.setScaledCollider(hintStation, 2.5f, 1.5f);
        hintStation.getComponent(ColliderComponent.class)
                .setAsBoxAligned(new Vector2(2.5f, 1.5f),
                        PhysicsComponent.AlignX.CENTER, PhysicsComponent.AlignY.CENTER);

        GridPoint2 hintPos = new GridPoint2(pos.x, pos.y + 2);
        spawnEntityAt(hintStation, hintPos, true, false);
    }

    /**
     * Creates a {@link BenchConfig} used for the password terminal's hint station.
     */
    private BenchConfig makeTerminalHintConfig() {
        BenchConfig bench = new BenchConfig() {
            @Override
            public int getPrice() {
                return 0;
            }

            @Override
            public void upgrade(boolean playerNear, com.csse3200.game.entities.Entity player, Label prompt) {
                // this method was intentionally left empty
            }
        };

        bench.texturePath = null;
        bench.promptText = "Press F1 to access terminal";

        return bench;
    }

    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        TunnelGameArea.isCleared = true;
        logger.debug("Reception is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        TunnelGameArea.isCleared = false;
        logger.debug("Tunnel is cleared");
    }

    private void spawnNurse(Entity player) {
        GridPoint2 pos = new GridPoint2(20, 8);

        Entity nurse = FriendlyNPCFactory.createNurseNpc(player);
        spawnEntityAt(nurse, pos, true, true);
    }

    /**
     * FOR TESTING PURPOSES
     */
    public static boolean getClearField() {
        return TunnelGameArea.isCleared;
    }
}