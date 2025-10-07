package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.minigames.BettingComponent;
import com.csse3200.game.components.minigames.BlackJackGame;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.screens.BlackjackScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.components.minigames.slots.SlotsGame;

/**
 * Minimal generic Casino room: walls, a single right-side door, and a subtle background overlay.
 * Right door -> Spawn Room
 */
public class CasinoGameArea extends GameArea {
    private static final float WALL_WIDTH = 0.1f;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(25, 10);
    private Entity player;

    public CasinoGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
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

        spawnBordersAndDoors();
        spawnFloor();

        player = spawnPlayer();
        spawnBlackjack();

        spawnSlotsGame();
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
        Entity newPlayer = PlayerFactory.createPlayer();
        spawnEntityAt(newPlayer, PLAYER_SPAWN, true, true);
        return newPlayer;
    }

    /**
     * Disposes current entities and switches to ForestGameArea.
     */
    private void loadSpawnFromCasino() {

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

    public static CasinoGameArea load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new CasinoGameArea(terrainFactory, camera));
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
