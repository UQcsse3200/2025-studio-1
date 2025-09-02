package com.csse3200.game.areas;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.badlogic.gdx.math.MathUtils;

public class KeycardSpawnerSystem {

    public static void spawnKeycards(GameArea area) {
//spawns keycards in the rooms and can define what level of keycard needs to be spawned
        String areaName = area.getClass().getSimpleName();

        int keycardLevel = -1;

        switch (areaName) {
            case "ForestGameArea":
                keycardLevel = 1;
                break;
            case "Floor2GameArea":
                keycardLevel = 2;
                break;
            case "Floor3GameArea":
                keycardLevel = 3;
                break;
            case "Floor4GameArea":
                keycardLevel = 4;
                break;
            default:
                // No keycard in other areas
                return;
        }

        // Create the keycard for this area
        Entity keycard = KeycardFactory.createKeycard(keycardLevel);

        // Position can be adjusted to match your room layout
        float x = MathUtils.random(2f, 10f);
        float y = MathUtils.random(2f, 6f);
        keycard.setPosition(x, y);

        // Spawn in the current area (no random room selection)
        area.spawnEntity(keycard);
    }
}