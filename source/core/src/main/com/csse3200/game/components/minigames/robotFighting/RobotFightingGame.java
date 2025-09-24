package com.csse3200.game.components.minigames.robotFighting;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader;

import java.awt.*;

public class RobotFightingGame {
    private final RobotFightingText encouragingMessages;
    private final Entity gameEntity;
    private final RobotFightingDisplay gameDisplay;

    private boolean gameDisplayed = false;

    public RobotFightingGame() {
        encouragingMessages = FileLoader.readClass(RobotFightingText.class, "games/robot-fighting.json");

        gameEntity = initGameEntity();
        gameDisplay = gameEntity.getComponent(RobotFightingDisplay.class);

        gameEntity.getEvents().addListener("interact", this::handleInteract);
    }

    private void handleInteract() {
        if (gameDisplayed) {
            gameDisplay.hide();
            gameDisplayed = false;
        } else {
            gameDisplay.show();
            gameDisplayed = true;
        }

    }

    private Entity initGameEntity() {
        Entity game = new Entity();
        game.addComponent(new RobotFightingDisplay());

        game.setInteractable(true);

        return game;
    }

    public Entity getGameEntity() {
        return gameEntity;
    }

    public void startGame() {
        
    }
}
