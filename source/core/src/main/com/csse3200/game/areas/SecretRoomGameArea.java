package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;

import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.components.stations.StationComponent;

import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.PhysicsComponent;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.physics.components.ColliderComponent;
import com.badlogic.gdx.math.Vector2;

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
        ensureTextures(new String[]{
                "images/Office and elevator/Office Background.png",
                "foreg_sprites/general/ThinFloor3.png",
                "images/OrangeButton.png"
        });

        // Use the Office terrain as the background of this room
        terrain = terrainFactory.createTerrain(TerrainType.OFFICE);
        spawnEntity(new Entity().addComponent(terrain));


        Entity ui = new Entity();
        ui.addComponent(new com.csse3200.game.components.gamearea.GameAreaDisplay("Secret Room"));

        spawnFloor();
        spawnPlayer();
        spawnBorders();

        addOrangeImageButton(new GridPoint2(14, 7));
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
        spawnOrRepositionPlayer(PLAYER_SPAWN);
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

    private void addOrangeImageButton(GridPoint2 pos) {
        Entity buttom = ObstacleFactory.createButtonSystem();
        spawnEntityAt(buttom, pos, true, false);
    }

}


