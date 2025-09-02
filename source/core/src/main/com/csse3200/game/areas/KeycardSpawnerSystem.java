package com.csse3200.game.areas;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.badlogic.gdx.math.MathUtils;

public class KeycardSpawnerSystem {

    public static void spawnKeycards(GameArea area) {

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
                // No keycard in other areas for now
                return;
        }
        Entity keycard = KeycardFactory.createKeycard(keycardLevel);

        float x = MathUtils.random(2f, 10f);
        float y = MathUtils.random(2f, 6f);
        keycard.setPosition(x, y);

        area.spawnEntity(keycard);
    }
}