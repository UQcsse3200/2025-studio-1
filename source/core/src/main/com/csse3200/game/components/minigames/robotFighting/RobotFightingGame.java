package com.csse3200.game.components.minigames.robotFighting;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.TextureRenderComponent;

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
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new RobotFightingDisplay());
        game.addComponent(new TextureRenderComponent("images/tree.png"));

        game.setInteractable(true);

        return game;
    }

    public Entity getGameEntity() {
        return gameEntity;
    }
}
