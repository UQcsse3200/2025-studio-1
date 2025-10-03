package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.rendering.SolidColorRenderComponent;

/**
 * Room 5 with its own background styling.
 */
public class MainHall extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

    public MainHall(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        ensureAssets();
        terrain = terrainFactory.createTerrain(TerrainType.MAIN_HALL);
        spawnEntity(new Entity().addComponent(terrain));
        Entity overlay = new Entity();
        overlay.setScale(1000f, 1000f);
        overlay.setPosition(-500f, -500f);
        overlay.addComponent(new SolidColorRenderComponent(new Color(0.1f, 0.1f, 0.2f, 0.35f)));
        spawnEntity(overlay);
        spawnplatform3();
        spawnsofa();
        spawnscreen();
        spawnholo();
        spawnObjectDoors(new GridPoint2(0, 6), new GridPoint2(28, 17));
        spawnWallsAndDoor();
        spawnPlayer();
        spawnFloor();
    }

    private void ensureAssets() {
        String[] textures = new String[]{
                "images/mainHall-background.png",
                "images/Mhall-sofa.png",
                "images/Mhall-screen.png",
                "images/Mhall-holo.png",
                "images/platform-3.png",
                "foreg_sprites/general/LongFloor.png",
                "foreg_sprites/general/ThickFloor.png",
                "foreg_sprites/general/SmallSquare.png",
                "foreg_sprites/general/SmallStair.png",
                "foreg_sprites/general/SquareTile.png"
        };
        ensureTextures(textures);
        ensurePlayerAtlas();
    }

    private void spawnWallsAndDoor() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        addSolidWallRight(b, WALL_WIDTH);
        float leftDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float leftDoorY = b.bottomY();
        float leftTopSegHeight = Math.max(0f, b.topY() - (leftDoorY + leftDoorHeight));
        if (leftTopSegHeight > 0f) {
            Entity leftTop = ObstacleFactory.createWall(WALL_WIDTH, leftTopSegHeight);
            leftTop.setPosition(b.leftX(), leftDoorY + leftDoorHeight);
            spawnEntity(leftTop);
        }
        Entity leftDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, leftDoorHeight);
        leftDoor.setPosition(b.leftX() + 0.001f, leftDoorY);
        leftDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadBackToFloor2));
        spawnEntity(leftDoor);


        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY(); // ground level
        float rightTopSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (rightTopSegHeight > 0f) {
            Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, rightTopSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY + 7f);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadSecurity));
        spawnEntity(rightDoor);
    }


    private void loadBackToFloor2() {
        roomNumber--;
        Reception.setRoomSpawn(new GridPoint2(24, 24));
        clearAndLoad(() -> new Reception(terrainFactory, cameraComponent));
    }

    private void loadSecurity() {
        roomNumber++;
        SecurityGameArea.setRoomSpawn(new GridPoint2(6, 8));
        clearAndLoad(() -> new SecurityGameArea(terrainFactory, cameraComponent));
    }

    private void spawnPlayer() {
        Entity player = PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
    }

    /**
     * Spawns 4 platforms for parkour
     **/
    private void spawnplatform3() {
        float PlatformX = 10.5f;
        float PlatformY = 7f;
        float PlatformX2 = 5f;
        float PlatformY2 = 9f;
        float PlatformX3 = 1.5f;
        float PlatformY3 = 5f;
        float PlatformX4 = 8f;
        float PlatformY4 = 3f;
        Entity Platform1 = ObstacleFactory.createplatform3();
        Platform1.setPosition(PlatformX, PlatformY);
        spawnEntity(Platform1);
        Entity Platform2 = ObstacleFactory.createplatform3();
        Platform2.setPosition(PlatformX2, PlatformY2);
        spawnEntity(Platform2);
        Entity Platform3 = ObstacleFactory.createplatform3();
        Platform3.setPosition(PlatformX3, PlatformY3);
        spawnEntity(Platform3);
        Entity Platform4 = ObstacleFactory.createplatform3();
        Platform4.setPosition(PlatformX4, PlatformY4);
        spawnEntity(Platform4);
    }

    /**
     * spawns Sofa in bottom left
     **/
    private void spawnsofa() {
        float PlatformX = 1f;
        float PlatformY = 3f;
        Entity sofa1 = ObstacleFactory.createMhall_sofa();
        sofa1.setPosition(PlatformX, PlatformY);
        spawnEntity(sofa1);
    }

    /**
     * spawns a screen
     **/
    private void spawnscreen() {
        float PlatformX = 2f;
        float PlatformY = 6.25f;
        Entity screen1 = ObstacleFactory.createMhall_screen();
        screen1.setPosition(PlatformX, PlatformY);
        spawnEntity(screen1);
    }

    /**
     * spawns a holographic decoration
     **/
    private void spawnholo() {
        float PlatformX = 11f;
        float PlatformY = 8.45f;
        Entity holo1 = ObstacleFactory.createMhall_holo();
        holo1.setPosition(PlatformX, PlatformY);
        spawnEntity(holo1);
    }

    public Entity getPlayer() {
        //tempoary placeholder return null to stop errors
        return null;
    }

    /**
     * Setter method for the player spawn point
     * should be used when the player is traversing through the rooms
     * 
     * @param newSpawn the new spawn point
     */
    public static void setRoomSpawn(GridPoint2 newSpawn) {
        MainHall.PLAYER_SPAWN = newSpawn;
    }

    @Override
    public String toString() {
        return "Mainhall";
    }

    public static MainHall load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new MainHall(terrainFactory, camera));
    }
}


