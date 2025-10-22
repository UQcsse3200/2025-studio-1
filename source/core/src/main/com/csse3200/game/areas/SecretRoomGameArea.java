package com.csse3200.game.areas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.areas.cutscenes.BadWinAnimationScreen;
import com.csse3200.game.areas.cutscenes.GoodWinAnimationScreen;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.DoorComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.stations.StationComponent;
import com.csse3200.game.entities.Entity;

import com.csse3200.game.lighting.LightSpawner;
import com.csse3200.game.services.ServiceLocator;

import java.util.List;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.rendering.TextureRenderComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Secret room: A minimal area with only background, a floor,
 * and side walls to prevent the player from leaving the scene
 **/
public class SecretRoomGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(10, 10);
    private static final GridPoint2 BUTTON_SPAWN = new GridPoint2(10,10);

    /** Reference to the right exit door component (used for unlocking). */
    private DoorComponent rightExitDoorComp;

    public SecretRoomGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        ensureTextures(new String[]{
                "images/Office and elevator/Office Background.png",
                "foreg_sprites/general/ThinFloor3.png",
                "images/OrangeButton.png",
                "images/KeycardDoor.png"
        });
        ensureAssets();
        // Use the Office terrain as the background of this room
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.SECRET,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));

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
                new Color(0.37f, 0.82f, 0.9f, 0.8f)
        );

        spawnFloor();
        spawnPlayer();
        spawnBorders();
        spawnRightLockedDoor();
        addOrangeImageButton(new GridPoint2(14, 7));

        ServiceLocator.getGlobalEvents().addListener("badWin", this::loadBadWin);
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

    private void spawnPlayer() {
        spawnOrRepositionPlayer(PLAYER_SPAWN);
    }

    /**
     * Spawns a right-side exit door that starts in a locked state.
     * The door acts as a trigger (non-solid collider). Once the orange button is
     * pressed by the player, this door becomes unlocked. Interacting with the door
     * will then trigger a transition to the next area.
     */
    private void spawnRightLockedDoor() {
        if (cameraComponent == null) return;

        Bounds b = getCameraBounds(cameraComponent);
        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.2f);

        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH * 10, rightDoorHeight);
        rightDoor.setPosition(13.9f, 3.75f);

        // Add visible door texture
        TextureRenderComponent doorTex = new TextureRenderComponent("images/KeycardDoor.png");
        rightDoor.addComponent(doorTex);

        // Add interaction behavior
        DoorComponent doorComp = new DoorComponent(this::loadWin);
        rightDoor.addComponent(doorComp);
        spawnEntity(rightDoor);

        // Initially locked until the player activates the orange button
        doorComp.setLocked(true);
        this.rightExitDoorComp = doorComp;
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

    /**
     * Adds an interactable orange button in the room.
     * When the player approaches the button, a prompt will appear saying
     * <b>"Press E to destroy the factory"</b>. When the player presses E,
     * the {@link #makeButtonConfig()} logic is executed — unlocking the right door.
     *
     * @param pos The grid position to spawn the button at.
     */
    private void addOrangeImageButton(GridPoint2 pos) {
        Entity button = InteractableStationFactory.createBaseStation();
        button.addComponent(new TextureRenderComponent("images/OrangeButton.png"));
        button.getComponent(TextureRenderComponent.class).scaleEntity();
        button.scaleHeight(0.6f);
        PhysicsUtils.setScaledCollider(button, 1.2f, 1.2f);
        button.getComponent(ColliderComponent.class)
                .setAsBoxAligned(new Vector2(1.2f, 1.2f),
                        PhysicsComponent.AlignX.CENTER,
                        PhysicsComponent.AlignY.CENTER);
        button.addComponent(new StationComponent(makeButtonConfig()));

        spawnEntityAt(button, pos, true, false);
    }

    /**
     * Creates a {@link BenchConfig} used for the button hint station.
     * - Displays "Press E to destroy the factory" when near.<br>
     * When the player presses E, it unlocks the right exit door and updates the prompt message.<br>
     * If the player is too far, it instructs them to move closer.
     *
     * @return The configuration used by the button's {@link StationComponent}.
     */
    private BenchConfig makeButtonConfig() {
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
                if (!playerNear) {
                    prompt.setText("Move closer to use the button");
                    return;
                }
                if (rightExitDoorComp != null) {
                    rightExitDoorComp.setLocked(false);
                }
                prompt.setText("Factory Exploding! Escape Out The Door To The Right!");

                ServiceLocator.getGlobalEvents().trigger("escape timer");


            }
        };
    }


    @Override
    public void clearAndLoad(Supplier<GameArea> nextAreaSupplier) {
        if (!beginTransition()) return;

        for (Entity entity : areaEntities) {
            entity.setEnabled(false);
        }

        /** Ensure transition happens on the render thread to avoid race conditions **/
        Gdx.app.postRunnable(() -> {
            /** Phase 1: dispose all entities **/
            for (Entity entity : areaEntities) {
                // Dispose every entity
                entity.dispose();
            }
            areaEntities.clear();

            /* Phase 2: on the next frame, build the next area to avoid Box2D world-locked/native races */
            Gdx.app.postRunnable(() -> {
                try {
                    GameArea next = nextAreaSupplier.get();
                    ServiceLocator.registerGameArea(next);
                    next.create();
                    // mark next area as discovered when entered
                    DiscoveryService ds = ServiceLocator.getDiscoveryService();
                    if (ds != null) {
                        ds.discover(next.toString());
                    }
                } finally {
                    endTransition();
                }
            });
        });
    }

    private void loadWin() {
        clearAndLoad(() -> new GoodWinAnimationScreen(terrainFactory, cameraComponent));
    }

    private void loadBadWin() {
        clearAndLoad(() -> new BadWinAnimationScreen(terrainFactory, cameraComponent));
    }
}


