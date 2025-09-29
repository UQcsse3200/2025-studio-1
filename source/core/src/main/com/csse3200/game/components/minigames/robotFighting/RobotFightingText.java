package com.csse3200.game.components.minigames.robotFighting;

import java.util.List;
import java.util.Random;

public class RobotFightingText {
    public List<String> encouragingMessages;

    private final Random random = new Random();

    public String getRandom() {
        if (encouragingMessages.isEmpty()) {
            return null;
        }
        return encouragingMessages.get(random.nextInt(encouragingMessages.size()));
    }
}
