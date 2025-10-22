package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;


/**
 * Office room: minimal walls and two doors (left--Security, right--Elevator).
 */
public class OfficeGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private static boolean isCleared;

    public OfficeGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
        
        this.getEvents().addListener("room cleared", OfficeGameArea::clearRoom);
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
        ensureTextures(new String[]{
                "images/Office and elevator/Office Background.png",
                "images/Office and elevator/Office stuff.png",
                "images/Office and elevator/table chair ceo.png",
                "foreg_sprites/general/ThinFloor3.png",
                "images/Office and elevator/Platform for elevator.png"
        });
        // Use dedicated office background
        terrain = terrainFactory.createTerrain(TerrainType.OFFICE);
        spawnEntity(new Entity().addComponent(terrain));

        spawnBordersAndDoors();
        Entity player = spawnPlayer();
        spawnFloor();
        spawnObjectDoors(new GridPoint2(0, 14), new GridPoint2(28, 20));
        spawnPlatforms();
        spawnOfficeProps();
        spawnTeleporter();
        spawnAssistor();

        if (!OfficeGameArea.isCleared) {
            startWaves(player);
        }

        displayUIEntity("Office", "Floor 5");
    }

    private void spawnBordersAndDoors() {
        Bounds b = getCameraBounds(cameraComponent);


        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = (b.bottomY() + b.topY()) / 2f - leftDoorHeight / 2f;
        float leftTopSegHeight = Math.max(0f, b.topY() - (leftDoorY + leftDoorHeight));
        if (leftTopSegHeight > 0f) {
            Entity leftTop = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
            leftTop.setPosition(b.leftX(), leftDoorY + leftDoorHeight);
            spawnEntity(leftTop);
        }
        float leftBottomSegHeight = Math.max(0f, leftDoorY - b.bottomY());
        if (leftBottomSegHeight > 0f) {
            Entity leftBottom = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, leftBottomSegHeight);
            leftBottom.setPosition(b.leftX(), b.bottomY());
            spawnEntity(leftBottom);
        }
        

        Entity leftDoorWall = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, leftDoorHeight);
        leftDoorWall.setPosition(b.leftX(), leftDoorY);
        spawnEntity(leftDoorWall);
        
        Entity leftDoor = com.csse3200.game.entities.factories.system.ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadMovingBossRoom));
        spawnEntity(leftDoor);
        
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

        Entity rightDoorWall = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(WALL_WIDTH, rightDoorHeight);
        rightDoorWall.setPosition(b.rightX() - WALL_WIDTH, rightDoorY);
        spawnEntity(rightDoorWall);
        
        Entity rightDoor = com.csse3200.game.entities.factories.system.ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadElevator));
        spawnEntity(rightDoor);

        if (!OfficeGameArea.isCleared) registerDoors(new Entity[]{leftDoor, rightDoor});
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
        float p1x = 5f;
        float p1y = 4f;
        float p2x = -1f;
        float p2y = 4f;
        float p3x = 8f;
        float p3y = 6f;
        float p4x = 13f;
        float p4y = 8f;

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
    private void spawnAssistor() {
        Entity player = ServiceLocator.getPlayer();
        GridPoint2 pos = new GridPoint2(3, 8);

        Entity assistor = FriendlyNPCFactory.createAssisterNpc(player);
        spawnEntityAt(assistor, pos, true, true);
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

    /**
     * Clear room, set this room's static
     * boolean isCleared variable to true
     */
    public static void clearRoom() {
        OfficeGameArea.isCleared = true;
        logger.debug("Office is cleared");
    }

    /**
     * Unclear room, set this room's static
     * boolean isCleared variable to false
     */
    public static void unclearRoom() {
        OfficeGameArea.isCleared = false;
        logger.debug("Office is uncleared");
    }
}
