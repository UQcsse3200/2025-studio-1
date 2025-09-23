package com.csse3200.game.minigames.robotRacing;

import com.csse3200.game.files.FileLoader;

public class RobotRacing {
    private final RobotRacingText encouragingMessages;

    public RobotRacing() {
        encouragingMessages = FileLoader.readClass(RobotRacingText.class, "games/robot-racing.json");
    }


}
