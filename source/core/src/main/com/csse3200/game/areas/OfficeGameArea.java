package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.LightFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.services.ServiceLocator;

/**
 * Office room: minimal walls and two doors (left--Security, right--Elevator).
 */
public class OfficeGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);

    public OfficeGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public static OfficeGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new OfficeGameArea(terrainFactory, camera));
    }

    // Assets ensured via GenericLayout

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
        OfficeGameArea.playerSpawn = newSpawn;
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.OFFICE,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));
        ensureTextures(new String[]{
                "images/Office and elevator/Office Background.png",
                "images/Office and elevator/Office stuff.png",
                "images/Office and elevator/table chair ceo.png",
                "foreg_sprites/general/ThinFloor3.png",
                "images/Office and elevator/Platform for elevator.png"
        });

        //Checks to see if the lighting service is not null and then sets the ambient light and turns on shadows for the room.
        var ls = ServiceLocator.getLightingService();
        if (ls != null && ls.getEngine() != null) {
            ls.getEngine().setAmbientLight(0.65f);
            ls.getEngine().getRayHandler().setShadows(true);
        }

        spawnCeilingCones();
        spawnBordersAndDoors();
        spawnPlayer();
        spawnFloor();
        spawnObjectDoors(new GridPoint2(0, 14), new GridPoint2(28, 20));
        spawnPlatforms();
        spawnOfficeProps();
        spawnEnemies();
        spawnTeleporter();

        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Office"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Floor 5"));
        spawnEntity(ui);
    }

    /**
     * Creates and spawns the lighting effects at the designated locations.
     */
    private void spawnCeilingCones() {
        // Warm-ish cone spotlights from ceiling pointing straight down (-90 degrees)
        var warm = new Color(0.37f, 0.82f, 0.9f, 0.95f); // tweak alpha for brightness
        int rays = 96;
        float dist = 7f;    // reach of the cone
        boolean xray = true; // true = no hard shadows (so it stays “clean”)

        // positions above your play areas (Y slightly below top wall so the hotspot hits tables)
        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(4, 21), true, true);

        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(12, 21), true, true);

        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(20, 21), true, true);

        spawnEntityAt(
                LightFactory.createConeLightEntity(rays, warm, dist, -90f, xray, new Vector2(0f, 0f)),
                new GridPoint2(27, 21), true, true);
    }

    private void spawnBordersAndDoors() {
        Bounds b = getCameraBounds(cameraComponent);

        addVerticalDoorLeft(b, WALL_WIDTH, this::loadMovingBossRoom);
        // Raise the right door higher than center
        addSolidWallTop(b, WALL_WIDTH);
        addSolidWallBottom(b, WALL_WIDTH);


        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY() + 7.0f; // higher placement
        float rightTopSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (rightTopSegHeight > 0f) {
            Entity rightTop = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }
        // Add right wall below the door
        float rightBottomSegHeight = Math.max(0f, rightDoorY - b.bottomY());
        if (rightBottomSegHeight > 0f) {
            Entity rightBottom = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, rightBottomSegHeight);
            rightBottom.setPosition(b.rightX() - WALL_WIDTH, b.bottomY());
            spawnEntity(rightBottom);
        }
        Entity rightDoor = com.csse3200.game.entities.factories.system.ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadElevator));
        spawnEntity(rightDoor);


    }

    private Entity spawnPlayer() {
        Entity player = spawnOrRepositionPlayer(playerSpawn);
        // Set higher z-index to ensure player renders in front of sprites
        if (player != null) {
            var renderComponent = player.getComponent(com.csse3200.game.rendering.AnimationRenderComponent.class);
            if (renderComponent != null) {
                renderComponent.setZIndex(10f); // Higher z-index to render in front
            }
        }
        return player;
    }

    private void spawnOfficeProps() {
        // Simple: render two office sprites via TextureRenderComponent-only entities
        Entity decor = new Entity()
                .addComponent(new com.csse3200.game.rendering.TextureRenderComponent("images/Office and elevator/Office stuff.png"));
        decor.getComponent(com.csse3200.game.rendering.TextureRenderComponent.class).scaleEntity();
        decor.scaleHeight(3.2f);
        decor.setPosition(12.0f, 3.2f);
        spawnEntity(decor);

        Entity ceoChair = new Entity()
                .addComponent(new com.csse3200.game.rendering.TextureRenderComponent("images/Office and elevator/table chair ceo.png"));
        ceoChair.getComponent(com.csse3200.game.rendering.TextureRenderComponent.class).scaleEntity();
        ceoChair.scaleHeight(3.0f);
        ceoChair.setPosition(2f, 3.0f);
        spawnEntity(ceoChair);
    }

    /** Teleporter bottom-left */
    private void spawnTeleporter() {
        Entity tp = TeleporterFactory.createTeleporter(new Vector2(5f, 3f));
        spawnEntity(tp);
    }

    /**
     * Spawn platforms
     */
    private void spawnPlatforms() {
        float p1x = 5f, p1y = 4f;
        float p2x = -1f, p2y = 4f;
        float p3x = 8f, p3y = 6f;
        float p4x = 13f, p4y = 8f;

        Entity platform1 = com.csse3200.game.entities.factories.system.ObstacleFactory.createOfficeElevatorPlatform();
        platform1.setPosition(p1x, p1y);
        spawnEntity(platform1);

        Entity platform2 = com.csse3200.game.entities.factories.system.ObstacleFactory.createOfficeElevatorPlatform();
        platform2.setPosition(p2x, p2y);
        spawnEntity(platform2);

        Entity platform3 = com.csse3200.game.entities.factories.system.ObstacleFactory.createOfficeElevatorPlatform();
        platform3.setPosition(p3x, p3y);
        spawnEntity(platform3);

        Entity platform4 = com.csse3200.game.entities.factories.system.ObstacleFactory.createOfficeElevatorPlatform();
        platform4.setPosition(p4x, p4y);
        spawnEntity(platform4);
    }

    /**
     * Add a thin floor
     */
    @Override
    protected void spawnFloor() {
        for (int i = 0; i < 25; i += 4) {
            GridPoint2 floorspawn = new GridPoint2(i, 6);
            Entity floor = com.csse3200.game.entities.factories.system.ObstacleFactory.createThinFloor();
            spawnEntityAt(floor, floorspawn, false, false);
            floor.setPosition(floor.getPosition().x, floor.getPosition().y - 0.3f);
        }
    }

    /**
     * Spawn 3 enemies in the office room
     */
    private void spawnEnemies() {
        Entity player = ServiceLocator.getPlayer();
        if (player == null) return;

        // Enemy 1: GhostGPT
        Entity ghostGPT = com.csse3200.game.entities.factories.characters.NPCFactory.createGhostGPT(player, this, 1.0f);
        spawnEntityAt(ghostGPT, new GridPoint2(15, 8), true, false);

        // Enemy 2: Deepspin
        Entity deepspin = com.csse3200.game.entities.factories.characters.NPCFactory.createDeepspin(player, this, 1.0f);
        spawnEntityAt(deepspin, new GridPoint2(20, 20), true, false);

       }

    private void loadMovingBossRoom() {
        MovingBossRoom.setRoomSpawn(new GridPoint2(24, 8));
        clearAndLoad(() -> new MovingBossRoom(terrainFactory, cameraComponent));
    }

    private void loadElevator() {
        ElevatorGameArea.setRoomSpawn(new GridPoint2(6, 8));
        clearAndLoad(() -> new ElevatorGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Office";
    }

    @Override
    public Entity getPlayer() {
        //placeholder see previous
        return null;
    }
}
