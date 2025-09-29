package com.csse3200.game.areas;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;

/**
 * Helper for setting up minimal generic rooms (terrain, overlay, walls, doors, player).
 * Kept in the same package to access {@link GameArea}'s protected helpers.
 */

public final class GenericLayout {
    private static final String[] GENERIC_TEXTURES = new String[]{
            // Terrain required by FOREST_DEMO
            "images/grass_1.png",
            "images/grass_2.png",
            "images/grass_3.png",
            // Generic props used by spawnFloor and misc
            "foreg_sprites/general/LongFloor.png",
            "foreg_sprites/general/ThickFloor.png",
            "foreg_sprites/general/SmallSquare.png",
            "foreg_sprites/general/SmallStair.png",
            "foreg_sprites/general/SquareTile.png"
    };

    private GenericLayout() {
    }

    public static void ensureGenericAssets(GameArea area) {
        area.ensureTextures(GENERIC_TEXTURES);
        area.ensurePlayerAtlas();
    }

    public static void setupTerrainWithOverlay(GameArea area,
                                               TerrainFactory factory,
                                               TerrainType type,
                                               Color overlayColor) {
        area.setupTerrainWithOverlay(factory, type, overlayColor);
    }

    public static void addLeftRightDoorsAndWalls(GameArea area,
                                                 CameraComponent cameraComponent,
                                                 float wallWidth,
                                                 Runnable onLeftDoor,
                                                 Runnable onRightDoor) {
        GameArea.Bounds b = area.getCameraBounds(cameraComponent);
        area.addVerticalDoorLeft(b, wallWidth, onLeftDoor);
        area.addVerticalDoorRight(b, wallWidth, onRightDoor);
        area.addSolidWallTop(b, wallWidth);
        area.addSolidWallBottom(b, wallWidth);
    }

    public static void spawnArrowPlayerAt(GameArea area, GridPoint2 spawnTile) {
        Entity player = PlayerFactory.createPlayerWithArrowKeys();
        area.spawnEntityAt(player, spawnTile, true, true);
    }
}


