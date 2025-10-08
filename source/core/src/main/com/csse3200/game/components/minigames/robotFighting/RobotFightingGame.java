package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.scenes.scene2d.Actor;

import java.awt.*;

/**
 * Core logic class for the "Clanker Royale" (Robot Fighting) minigame.
 * <p>
 * Handles setup, fight logic, UI toggling, encouragement mechanics,
 * and winner determination. This class interacts closely with
 * {@link RobotFightingDisplay} for visuals and user interactions.
 * </p>
 *
 * <h2>Main responsibilities:</h2>
 * <ul>
 *   <li>Initialises the minigame entity and UI display.</li>
 *   <li>Controls when the game UI is shown or hidden via {@code interact} events.</li>
 *   <li>Manages health values and combat timers between two fighters.</li>
 *   <li>Tracks player encouragement level, which modifies damage output.</li>
 *   <li>Determines the fight outcome and triggers UI updates.</li>
 * </ul>
 *
 * <p>
 * The class registers to the following entity events:
 * <ul>
 *   <li>{@code interact} — toggles the game screen open/close.</li>
 *   <li>{@code robotFighting:choose} — selects a fighter robot.</li>
 *   <li>{@code robotFighting:startFight} — begins the combat sequence.</li>
 *   <li>{@code robotFighting:encourage} — applies an encouragement effect.</li>
 * </ul>
 */
public class RobotFightingGame {
    /** Preloaded text data containing random encouragement messages. */
    private final RobotFightingText encouragingMessages;
    /** Root entity containing this minigame’s components and display. */
    private final Entity gameEntity;
    /** The display/UI component that visualises the minigame. */
    private final RobotFightingDisplay gameDisplay;

    /** Whether the game UI is currently visible to the player. */
    private boolean gameDisplayed = false;
    /** The player’s selected robot. */
    private Robot selectedRobot = null;
    /** Current HP for the player’s fighter. */
    private int chosenFighterHp = 100;
    /** Current HP for the opposing fighter. */
    private int otherFighterHp = 100;
    /** Encouragement multiplier for player attack power (1.0–2.0). */
    private double encourageMult = 1.0;

    /**
     * Default constructor for production use.
     * <p>
     * Loads encouragement text from {@code games/robot-fighting.json}
     * and creates all required LibGDX components via {@link InteractableStationFactory}.
     * </p>
     */
    public RobotFightingGame() {
        encouragingMessages = FileLoader.readClass(RobotFightingText.class, "games/robot-fighting.json");

        gameEntity = initGameEntity();
        gameDisplay = gameEntity.getComponent(RobotFightingDisplay.class);

        gameEntity.getEvents().addListener("interact", this::handleInteract);
        gameEntity.getEvents().addListener("robotFighting:choose", this::selectFighter);
        gameEntity.getEvents().addListener("robotFighting:startFight", this::startFight);
        gameEntity.getEvents().addListener("robotFighting:encourage", this::encourageFighter);
    }

    /**
     * Alternate constructor used for testing.
     * <p>
     * Accepts a custom {@link RobotFightingText} to bypass {@link FileLoader}
     * and avoid external asset loading.
     * </p>
     *
     * @param customText Preloaded text data containing encouragement strings.
     */
    public RobotFightingGame(RobotFightingText customText) {
        this.encouragingMessages = customText;

        gameEntity = initGameEntity();
        gameDisplay = gameEntity.getComponent(RobotFightingDisplay.class);

        gameEntity.getEvents().addListener("interact", this::handleInteract);
        gameEntity.getEvents().addListener("robotFighting:choose", this::selectFighter);
        gameEntity.getEvents().addListener("robotFighting:startFight", this::startFight);
        gameEntity.getEvents().addListener("robotFighting:encourage", this::encourageFighter);
    }

    /**
     * Handles player interaction to toggle the minigame screen.
     * <p>
     * When visible, the game is paused; when hidden, gameplay resumes.
     * </p>
     */
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

    /**
     * Creates and initialises the entity and components for this minigame.
     * <p>
     * Subclasses (e.g., in tests) can override this method to return
     * a lightweight mock entity without requiring LibGDX assets.
     * </p>
     *
     * @return a new {@link Entity} configured for the Robot Fighting game.
     */
    protected Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new RobotFightingDisplay());
        game.addComponent(new TextureRenderComponent("images/tree.png"));
        return game;
    }

    /**
     * Stores the player’s chosen robot type for the fight.
     *
     * @param fighter the selected {@link Robot}.
     */
    private void selectFighter(Robot fighter) {
        selectedRobot = fighter;
    }

    /**
     * Starts the combat loop between the two robots.
     * <p>
     * Sets HP back to 100 for both fighters and begins two repeating timers:
     * <ul>
     *   <li>Player attack loop (damage scaled by {@code encourageMult})</li>
     *   <li>Enemy attack loop (constant damage, gradually reduces encouragement)</li>
     * </ul>
     * </p>
     */
    private void startFight() {
        Timer.instance().clear(); // Reset timers if fight restarts
        chosenFighterHp = 100;
        otherFighterHp = 100;

        // Player-controlled fighter attacks periodically
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (chosenFighterHp <= 0 || otherFighterHp <= 0) {
                    cancel();
                    determineWinner();
                    return;
                }

                Actor attacked = gameDisplay.getOtherActor(gameDisplay.getChosenActor());
                if (attacked == null) return;

                gameDisplay.playAttackAnimation(attacked);

                int damage = (int) (Math.random() * 10 * encourageMult + 5);
                otherFighterHp -= damage;
                gameDisplay.setHealthFighter(attacked, otherFighterHp);
            }
        }, 1f, 1.5f + (float) Math.random());

        // Enemy attacks periodically
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

                loseCourage();
            }
        }, 1.3f, 1.5f + (float) Math.random());
    }

    /**
     * Handles an encouragement action from the player.
     * <p>
     * Displays a random encouragement line, and if successful,
     * increases {@link #encourageMult} logarithmically toward 2.0.
     * The closer it gets to 2, the smaller the increase.
     * </p>
     */
    private void encourageFighter() {
        gameDisplay.encourageFighter(encouragingMessages.getRandom());
        boolean successfulEncourage = Math.random() < 0.5;

        if (!successfulEncourage) return;

        double baseStrength = 0.1 + Math.random() * 0.2;
        encourageMult += (2 - encourageMult) * baseStrength;
        encourageMult = Math.min(encourageMult, 2);
    }

    /**
     * Gradually reduces the encouragement multiplier over time,
     * simulating loss of morale during combat.
     * <p>
     * Multiplier never drops below 1.0.
     * </p>
     */
    private void loseCourage() {
        encourageMult = Math.max(encourageMult - 0.05, 1.0);
    }

    /**
     * Determines the outcome of the fight and notifies the UI.
     * <ul>
     *   <li>If both HP ≤ 0 → draw</li>
     *   <li>If player HP ≤ 0 → lose</li>
     *   <li>Otherwise → win</li>
     * </ul>
     */
    private void determineWinner() {
        if (chosenFighterHp <= 0) {
            if (otherFighterHp <= 0) {
                gameDisplay.fightOver("drew");
            } else {
                gameDisplay.fightOver("lost");
            }
        } else {
            gameDisplay.fightOver("won");
        }
    }

    /**
     * Returns the root {@link Entity} containing this minigame’s components.
     * <p>
     * Used for event registration and testing.
     * </p>
     *
     * @return the game entity instance.
     */
    public Entity getGameEntity() {
        return gameEntity;
    }
}