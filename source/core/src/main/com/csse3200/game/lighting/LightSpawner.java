package com.csse3200.game.lighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;

import java.util.List;

public class LightSpawner {
    /**
     * Method that creates new ConeLights, assigns them positions and spawns them at said positions.
     *
     * @param area The GameArea that the lights will be spawned in
     * @param positions The positions that the lights will spawn at
     * @param color The color of the lights
     */
    public static void spawnCeilingCones(GameArea area, List<GridPoint2> positions, Color color) {
        boolean xray = true;
        //Loops through all positions and creates a ConeLight at that position
        for (GridPoint2 pos : positions) {
            Entity CeilingLight = new Entity()
                    .addComponent(new ConeLightComponent(
                            color,
                            xray,
                            new Vector2(0f,0f)
                    ));
            area.spawnEntityPublic(CeilingLight, pos, true, true);
        }
    }
}
