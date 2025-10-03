package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;

/**
 * Secret room: A minimal area with only background, a floor,
 * and side walls to prevent the player from leaving the scene
 **/
public class SecretRoomGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);

    public SecretRoomGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);

        // Use the Office terrain as the background of this room
        terrain = terrainFactory.createTerrain(TerrainType.OFFICE);
        spawnEntity(new Entity().addComponent(terrain));


        spawnFloor();
        spawnPlayer();
        spawnBorders();
    }

    /**
     * Spawns invisible border walls on the left and right edges of the screen
     * to prevent the player from walking out of bounds.
     */
    private void spawnBorders() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);

        // Left border wall
        Entity leftWall = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(
                WALL_WIDTH, b.viewHeight());
        leftWall.setPosition(b.leftX(), b.bottomY());
        spawnEntity(leftWall);

        // Right border wall
        Entity rightWall = com.csse3200.game.entities.factories.system.ObstacleFactory.createWall(
                WALL_WIDTH, b.viewHeight());
        rightWall.setPosition(b.rightX() - WALL_WIDTH, b.bottomY());
        spawnEntity(rightWall);
    }

    private void spawnPlayer() {
        Entity player = com.csse3200.game.entities.factories.characters.PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
    }

    @Override
    public String toString() {
        return "SecretRoom";
    }

    @Override
    public Entity getPlayer() {
        // placeholder for errors
        return null;
    }

    public static SecretRoomGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new SecretRoomGameArea(terrainFactory, camera));
    }

}


