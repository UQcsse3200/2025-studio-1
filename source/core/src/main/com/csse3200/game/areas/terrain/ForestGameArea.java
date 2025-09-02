package com.csse3200.game.areas.terrain;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forest area for the demo game with trees, a player, and some enemies.
 */
public class ForestGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(ForestGameArea.class);
    private static final int NUM_TREES = 7;
    private static final int NUM_GHOSTS = 0;
    private static final GridPoint2 PLAYER_SPAWN = new GridPoint2(3, 7);
    private static final float WALL_WIDTH = 0.1f;
    private static final String[] forestTextures = {
            "images/box_boy_leaf.png",
            "images/tree.png",
            "images/ghost_king.png",
            "images/ghost_1.png",
            "images/grass_1.png",
            "images/grass_2.png",
            "images/grass_3.png",
            "images/hex_grass_1.png",
            "images/hex_grass_2.png",
            "images/hex_grass_3.png",
            "images/iso_grass_1.png",
            "images/iso_grass_2.png",
            "images/iso_grass_3.png",
            << << << < HEAD
          "images/door.png"
                  =======
                  "images/Spawn.png",
                  "images/SpawnResize.png",
                  "images/LobbyWIP.png"
                  >>>>>>>2b579234ec22113305eecb39948f0ff57f81a3e6
}
