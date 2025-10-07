package com.csse3200.game.components.minigames.robotFighting;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.awt.*;

public class RobotFightingGame {
    private final RobotFightingText encouragingMessages;
    private final Entity gameEntity;
    private final RobotFightingDisplay gameDisplay;

    private boolean gameDisplayed = false;
    private Robot selectedRobot = null;
    private int chosenFighterHp = 100;
    private int otherFighterHp = 100;
    private int encourageMult

    public RobotFightingGame() {
        encouragingMessages = FileLoader.readClass(RobotFightingText.class, "games/robot-fighting.json");

        gameEntity = initGameEntity();
        gameDisplay = gameEntity.getComponent(RobotFightingDisplay.class);

        gameEntity.getEvents().addListener("interact", this::handleInteract);
        gameEntity.getEvents().addListener("robotFighting:choose", this::selectFighter);
        gameEntity.getEvents().addListener("robotFighting:startFight", this::startFight);
    }

    private void handleInteract() {
        if (gameDisplayed) {
            gameDisplay.hide();
            ServiceLocator.getTimeSource().setPaused(false);
            gameDisplayed = false;
        } else {
            gameDisplay.show();
            ServiceLocator.getTimeSource().setPaused(true);
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

    private void selectFighter(Robot fighter) {
        selectedRobot = fighter;
    }

    private void startFight() {
        // Cancel any old timers (in case the fight restarts)
        Timer.instance().clear();

        // --- Player-controlled robot attack loop ---
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (chosenFighterHp <= 0 || otherFighterHp <= 0) {
                    cancel();
                    return;
                }

                Actor attacked = gameDisplay.getOtherActor(gameDisplay.getChosenActor());
                if (attacked == null) return;

                gameDisplay.playAttackAnimation(attacked);

                int damage = (int) (Math.random() * 5 * encourageMult + 5);
                otherFighterHp -= damage;
                gameDisplay.setHealthFighter(attacked, otherFighterHp);

                // Debug print (optional)
                System.out.println("Player attacks! Enemy HP: " + otherFighterHp);
            }
        }, 1f, 1.5f + (float) Math.random());


        // --- Enemy robot attack loop ---
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (chosenFighterHp <= 0 || otherFighterHp <= 0) {
                    cancel();
                    return;
                }

                Actor attacked = gameDisplay.getChosenActor();
                if (attacked == null) return;

                gameDisplay.playAttackAnimation(attacked);

                int damage = (int) (Math.random() * 5 + 10);
                chosenFighterHp -= damage;
                gameDisplay.setHealthFighter(attacked, chosenFighterHp);

                // Debug print (optional)
                System.out.println("Enemy attacks! Player HP: " + chosenFighterHp);
            }
        }, 1.3f, 1.5f + (float) Math.random());
    }


    public Entity getGameEntity() {
        return gameEntity;
    }
}
