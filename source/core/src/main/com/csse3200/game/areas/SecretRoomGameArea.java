package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.areas.cutscenes.GoodWinAnimationScreen;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.stations.StationComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Secret room: A minimal area with only background, a floor,
 * and side walls to prevent the player from leaving the scene
 **/
public class SecretRoomGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final GridPoint2 BUTTON_SPAWN = new GridPoint2(10,10);
    private DoorComponent rightDoorComp;
    public static volatile DoorComponent exposedRightDoor;

    public SecretRoomGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        ensureAssets();

        // Use the Office terrain as the background of this room
        terrain = terrainFactory.createTerrain(TerrainType.OFFICE);
        spawnEntity(new Entity().addComponent(terrain));


        spawnFloor();
        spawnDoors();
        spawnPlayer();
        spawnBorders();
        //spawnButton();
    }

    private void ensureAssets() {
        String[] textures = new String[]{
                "images/Office and elevator/Office Background.png",
                "foreg_sprites/general/ThinFloor3.png",
        };
        GenericLayout.ensureGenericAssets(this);
        ensureTextures(textures);
        ensurePlayerAtlas();
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

    private void spawnDoors() {
        if (cameraComponent == null)
            return;
        Bounds b = getCameraBounds(cameraComponent);
        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);
        float rightDoorY = b.bottomY();
        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadWin));
        spawnEntity(rightDoor);

        rightDoorComp = rightDoor.getComponent(DoorComponent.class);
        rightDoorComp.setLocked(false);
        exposedRightDoor = rightDoorComp;
    }

    private void spawnPlayer() {
        Entity player = com.csse3200.game.entities.factories.characters.PlayerFactory.createPlayer();
        spawnEntityAt(player, PLAYER_SPAWN, true, true);
    }

    /**
     * Spawns a button that can be pressed to beat the game
     */
    private void spawnButton() {
        Entity button = new Entity()
                .addComponent(new TextureRenderComponent("images/OrangeButton.png"));
        button.getEvents().addListener("interact",()-> rightDoorComp.setLocked(false));
        spawnEntityAt(button, BUTTON_SPAWN, true, false);

        Entity buttonText = new Entity()
                .addComponent(new TextureRenderComponent("images/OrangeButton.png"))
                .addComponent(new StationComponent(makeButtonHintConfig()));
        spawnEntityAt(buttonText, BUTTON_SPAWN, true, false);
    }

    /**
     * Creates a {@link BenchConfig} used for the button hint when the player is near.
     */
    private BenchConfig makeButtonHintConfig() {
        return new BenchConfig() {
            {
                this.texturePath = null;
                this.promptText = "Press E to destroy the AI factory";
            }
            @Override
            public int getPrice() {
                return 0;
            }
            @Override
            public void upgrade(boolean playerNear, com.csse3200.game.entities.Entity player, Label prompt) {
            }
        };
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

    private void loadWin() {
        clearAndLoad(() -> new GoodWinAnimationScreen(terrainFactory, cameraComponent));
    }
}


