package com.csse3200.game.areas;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.KeycardGateComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Elevator room: minimal walls and two doors (left--Office, right--Research).
 **/
public class ElevatorGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

    public ElevatorGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        /** Ensure the thin floor texture is available for the elevator room **/
        ensureTextures(new String[]{"foreg_sprites/general/ThinFloor3.png", "images/Elevator background.png", "images/keycard_lvl2.png", "images/KeycardDoor.png", "images/Office and elevator/Office platform.png", "images/Office and elevator/Office desk.png"});
        /** Use the dedicated elevator background **/
        terrain = terrainFactory.createTerrain(TerrainType.ELEVATOR);
        spawnEntity(new Entity().addComponent(terrain));
        float keycardX = 3f;
        float keycardY = 10f;
        Entity keycard = KeycardFactory.createKeycard(2);
        keycard.setPosition(new Vector2(keycardX, keycardY));
        spawnEntity(keycard);
        spawnBordersAndDoors();
        spawnPlayer();
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 19));
        spawnFloor();
        spawnPlatforms();
        spawnDesk();
    }

    private void spawnBordersAndDoors() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);

        addSolidWallTop(b, WALL_WIDTH);

        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY(); // ground level
        float leftTopSegHeight = Math.max(0f, b.topY() - (leftDoorY + leftDoorHeight));
        if (leftTopSegHeight > 0f) {
            Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
            leftTop.setPosition(b.leftX(), leftDoorY + leftDoorHeight);
            spawnEntity(leftTop);
        }
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadOffice));
        spawnEntity(leftDoor);


        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY() + 7.0f; // slightly above ground
        float rightTopSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (rightTopSegHeight > 0f) {
            Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new ColliderComponent());
        rightDoor.addComponent(new HitboxComponent().setLayer(PhysicsLayer.GATE));
        /**Add keycard logic **/
        rightDoor.addComponent(new KeycardGateComponent(2, () -> {
            ColliderComponent collider = rightDoor.getComponent(ColliderComponent.class);
            if (collider != null) collider.setEnabled(false);
            loadResearch();
        }));
        spawnEntity(rightDoor);

    }

    private void spawnPlayer() {
        Entity player = com.csse3200.game.entities.factories.characters.PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
    }

    /**
     * Place the desk in the elevator room
     */
    private void spawnDesk() {
        Entity desk = new Entity()
                .addComponent(new TextureRenderComponent("images/Office and elevator/Office desk.png"));
        desk.getComponent(TextureRenderComponent.class).scaleEntity();
        desk.scaleHeight(3.0f);
        desk.setPosition(7f, 3f);
        spawnEntity(desk);
    }

    /**
     * Spawn a few floating platforms
     */
    private void spawnPlatforms() {
        float p1x = 1f, p1y = 4f;
        float p2x = 5f, p2y = 6f;
        float p3x = 10f, p3y = 6f;

        Entity plat1 = com.csse3200.game.entities.factories.system.ObstacleFactory.createElevatorPlatform();
        plat1.setPosition(p1x, p1y);
        spawnEntity(plat1);

        Entity plat2 = com.csse3200.game.entities.factories.system.ObstacleFactory.createElevatorPlatform();
        plat2.setPosition(p2x, p2y);
        spawnEntity(plat2);

        Entity plat3 = com.csse3200.game.entities.factories.system.ObstacleFactory.createElevatorPlatform();
        plat3.setPosition(p3x, p3y);
        spawnEntity(plat3);
    }

    private void loadOffice() {
        roomNumber--;
        clearAndLoad(() -> new OfficeGameArea(terrainFactory, cameraComponent));
    }

    private void loadResearch() {
        roomNumber++;
        clearAndLoad(() -> new ResearchGameArea(terrainFactory, cameraComponent));
    }

    /**
     * Override default floor spawning to use the thin floor sprite in the elevator.
     */
    @Override
    protected void spawnFloor() {
        for (int i = 0; i < 25; i += 4) {
            GridPoint2 floorspawn = new GridPoint2(i, 6);
            Entity floor = ObstacleFactory.createThinFloor();
            spawnEntityAt(floor, floorspawn, false, false);
            floor.setPosition(floor.getPosition().x, floor.getPosition().y - 0.3f);
        }
    }

    /**
     * Setter method for the player spawn point
     * should be used when the player is traversing through the rooms
     * 
     * @param newSpawn the new spawn point
     */
    public static void setRoomSpawn(GridPoint2 newSpawn) {
        ElevatorGameArea.PLAYER_SPAWN = newSpawn;
    }

    @Override
    public String toString() {
        return "Elevator";
    }

    @Override
    public Entity getPlayer() {
        // placeholder for errors
        return null;
    }

    public static ElevatorGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new ElevatorGameArea(terrainFactory, camera));
    }

}


