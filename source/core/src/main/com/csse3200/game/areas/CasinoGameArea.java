package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.BettingComponent;
import com.csse3200.game.components.minigames.BlackJackGame;
import com.csse3200.game.components.minigames.pool.PoolGame;
import com.csse3200.game.components.minigames.robotFighting.RobotFightingGame;
import com.csse3200.game.components.minigames.slots.SlotsGame;
import com.csse3200.game.components.minigames.whackamole.WhackAMoleGame;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.screens.BlackjackScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.LightFactory;
import com.csse3200.game.components.lighting.PointLightFollowComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Minimal generic Casino room: walls, a single right-side door, and a subtle background overlay.
 * <p>
 * Right door -> Spawn Room
 */
public class CasinoGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(25, 10);
    private Entity player;
    private static final String[] CASINO_TEXTURES = {
            "images/mole.png",
            "images/hole.png",
            "images/pool/cue.png",
            "images/pool/cue_ball.png",
            "images/pool/table.png",
    };
    private static final String[] CASINO_ATLAS = {
            "images/pool/balls.atlas"
    };
    private static final String[] CASINO_SOUNDS = {
            "sounds/whack.mp3"
    };

    public CasinoGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public static CasinoGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new CasinoGameArea(terrainFactory, camera));
    }

    /**
     * Entry point for this room. This:
     * - Loads overlay
     * - Creates Walls, Doors and Floor
     * - Spawns player
     */
    @Override
    public void create() {
        GenericLayout.ensureGenericAssets(this);
        GenericLayout.setupTerrainWithOverlay(this, terrainFactory, TerrainType.CASINO,
                new Color(0.08f, 0.08f, 0.1f, 0.30f));

        ensureAssets();

        var ls = ServiceLocator.getLightingService();
        if (ls != null && ls.getEngine() != null) {
            ls.getEngine().setAmbientLight(0.65f); // 0.7–0.8 feels “casino”
            ls.getEngine().getRayHandler().setShadows(true);
            ls.getEngine().getRayHandler().removeAll();
        }

        spawnNeonRig();
        spawnBordersAndDoors();
        spawnFloor();
        spawnLight();
        player = spawnPlayer();

        if (player.getComponent(com.csse3200.game.components.lighting.PointLightComponent.class) == null &&
                player.getComponent(com.csse3200.game.components.lighting.PointLightFollowComponent.class) == null) {
            player.addComponent(new com.csse3200.game.components.lighting.PointLightFollowComponent(
                    32, new com.badlogic.gdx.graphics.Color(1f,1f,1f,0.35f), 6.5f, new com.badlogic.gdx.math.Vector2(0f, 1f)
            ));
        }

        spawnBlackjack();
        spawnSlotsGame();
        spawnWhackAMoleGame();
        spawnRobotFightingGame();
        spawnPoolGame();
    }

    private void ensureAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(CASINO_TEXTURES);
        rs.loadTextureAtlases(CASINO_ATLAS);
        rs.loadSounds(CASINO_SOUNDS);
        rs.loadAll();
    }

    private void unloadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(CASINO_TEXTURES);
        rs.unloadAssets(CASINO_ATLAS);
        rs.unloadAssets(CASINO_SOUNDS);

    }

    // Soft overall wash + ceiling bulbs above play areas
    private void spawnLight() {
        // Very faint white directional wash so backgrounds never go fully black
        spawnEntity(LightFactory.createDirectionalLightEntity(
                64, new Color(1f, 1f, 1f, 0.18f), 270f, true)); // from top, xray

        // Ceiling “bulbs” (xray point lights) — place above tables/machines
        Color bulb = new Color(1f, 0.95f, 0.85f, 0.34f);
        float bulbDist = 22f;

        spawnEntityAt(
                LightFactory.createPointLightEntity(64, bulb, bulbDist, true, new Vector2(0f, 0f)),
                new GridPoint2(11, 12), true, true);

        spawnEntityAt(
                LightFactory.createPointLightEntity(64, bulb, bulbDist, true, new Vector2(0f, 0f)),
                new GridPoint2(18, 12), true, true);

        spawnEntityAt(
                LightFactory.createPointLightEntity(64, bulb, bulbDist, true, new Vector2(0f, 0f)),
                new GridPoint2(24, 12), true, true);
    }

    private void spawnNeonRig() {
        // Use live camera bounds so strips always fit the room
        Bounds b = getCameraBounds(cameraComponent);
        float left  = b.leftX()   + 0.30f;
        float right = b.rightX()  - 0.30f;
        float topY  = b.topY()    - 0.30f;
        float botY  = b.bottomY() + 0.30f;

        // Softer directional washes (xray) to tint the room
        spawnEntity(LightFactory.createDirectionalLightEntity(
                96, new Color(1f, 0.25f, 0.85f, 0.14f), 315f, true)); // magenta from top-left
        spawnEntity(LightFactory.createDirectionalLightEntity(
                96, new Color(0.15f, 0.95f, 1f, 0.14f), 135f, true)); // cyan from top-right

        // Top & bottom neon strips (xray chain lights) spanning full width
        float stripDist = (right - left) * 1.1f; // reach; bump up/down to taste

        // Top strip (magenta)
        spawnEntity(LightFactory.createChainLightEntity(
                48, new Color(1f, 0.20f, 0.75f, 0.35f), stripDist,
                new float[]{ left, topY, right, topY }, true));

        // Bottom strip (cyan)
        spawnEntity(LightFactory.createChainLightEntity(
                48, new Color(0.10f, 0.95f, 1f, 0.35f), stripDist,
                new float[]{ left, botY, right, botY }, true));

        // Optional: vertical side strips to frame the room
        float sideDist = (topY - botY) * 1.1f;
        spawnEntity(LightFactory.createChainLightEntity(
                48, new Color(1f, 0.20f, 0.75f, 0.30f), sideDist,
                new float[]{ left, botY, left, topY }, true));
        spawnEntity(LightFactory.createChainLightEntity(
                48, new Color(0.10f, 0.95f, 1f, 0.30f), sideDist,
                new float[]{ right, botY, right, topY }, true));
    }

    /**
     * Spawns the borders and right door inside the room.
     * Right door -> Spawn Room
     * Uses a larger door to fit the casino png.
     */
    private void spawnBordersAndDoors() {
        if (cameraComponent == null) return;
        Bounds b = getCameraBounds(cameraComponent);
        addSolidWallLeft(b, WALL_WIDTH);
        addSolidWallTop(b, WALL_WIDTH);
        float rightDoorHeight = Math.max(1f, b.viewHeight() * 0.4f);
        float rightDoorY = b.bottomY();

        // Top segment of the right wall (from top of door to ceiling)
        float topSegHeight = Math.max(0f, b.topY() - (rightDoorY + rightDoorHeight));
        if (topSegHeight > 0f) {
            Entity rightTop = ObstacleFactory.createWall(WALL_WIDTH, topSegHeight);
            rightTop.setPosition(b.rightX() - WALL_WIDTH, rightDoorY + rightDoorHeight);
            spawnEntity(rightTop);
        }

        Entity rightDoor = ObstacleFactory.createDoorTrigger(WALL_WIDTH, rightDoorHeight);
        rightDoor.setPosition(b.rightX() - WALL_WIDTH - 0.001f, rightDoorY);
        rightDoor.addComponent(new com.csse3200.game.components.DoorComponent(this::loadSpawnFromCasino));
        spawnEntity(rightDoor);
    }

    /**
     * Spawns the player at PLAYER_SPAWN and returns the entity.
     */
    private Entity spawnPlayer() {
        return spawnOrRepositionPlayer(PLAYER_SPAWN);
    }

    private void spawnWhackAMoleGame() {
        GridPoint2 pos = new GridPoint2(5, 7);

        WhackAMoleGame game = new WhackAMoleGame();
        Entity station = game.getGameEntity();

        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        station.addComponent(new BettingComponent(2, inv));

        spawnEntityAt(station, pos, true, true);
    }

    private void spawnRobotFightingGame() {
        GridPoint2 pos = new GridPoint2(16, 7);
        spawnEntityAt(new RobotFightingGame().getGameEntity(), pos, true, true);
    }

    private void spawnPoolGame() {
        GridPoint2 pos = new GridPoint2(11, 7);
        spawnEntityAt(new PoolGame().getGameEntity(), pos, true, true);
    }


    /**
     * Disposes current entities and switches to ForestGameArea.
     */
    private void loadSpawnFromCasino() {
        unloadAssets();
        clearAndLoad(() -> new ForestGameArea(terrainFactory, cameraComponent));
    }

    @Override
    public String toString() {
        return "Casino";
    }

    @Override
    public Entity getPlayer() {
        return player;
    }

    private void spawnBlackjack() {
        Entity blackjack = InteractableStationFactory.createBaseStation();
        blackjack.addComponent(new TextureRenderComponent("images/blackjack_table.png"));
        blackjack.addComponent(new BettingComponent(2, player.getComponent(InventoryComponent.class)));
        blackjack.addComponent(new BlackJackGame());
        blackjack.addComponent(new BlackjackScreenDisplay());
        spawnEntityAt(blackjack, new GridPoint2(20, 7), true, true);
    }

    private void spawnSlotsGame() {
        GridPoint2 pos = new GridPoint2(23, 7);
        InventoryComponent inv = player.getComponent(InventoryComponent.class);
        spawnEntityAt(new SlotsGame(inv).getGameEntity(), pos, true, true);
    }
}
