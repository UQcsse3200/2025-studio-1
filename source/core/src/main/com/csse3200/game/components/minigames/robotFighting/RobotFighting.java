package com.csse3200.game.components.minigames.robotFighting;

import com.csse3200.game.files.FileLoader;

public class RobotFighting {
    private final RobotFightingText encouragingMessages;

    public RobotFighting() {
        encouragingMessages = FileLoader.readClass(RobotFightingText.class, "games/robot-fighting.json");
    }

    public void startGame() {
        
    }
}
