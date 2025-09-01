package com.csse3200.game.areas;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.badlogic.gdx.math.MathUtils;

import java.util.List;

public class KeycardSpawnerSystem {

    public static void spawnKeycards(GameArea area) {
        for (int level = 1; level <= 4; level++) {
            List<String> candidateRooms = com.csse3200.game.areas.RoomAccessRegistry.getRoomsAccessibleBy(level - 1);
            if (candidateRooms.isEmpty()) continue;

            String targetRoom = candidateRooms.get(MathUtils.random(candidateRooms.size() - 1));
            Entity keycard = KeycardFactory.createKeycard(level);

            float x = MathUtils.random(2f, 10f); // Adjust to room bounds
            float y = MathUtils.random(2f, 6f);
            keycard.setPosition(x, y);

            area.spawnEntityInRoom(targetRoom, keycard); // You’ll implement this method
        }
    }
}