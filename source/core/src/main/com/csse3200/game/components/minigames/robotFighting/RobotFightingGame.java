package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.minigames.BettingComponent;
import com.csse3200.game.components.player.InventoryComponent;
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
        gameEntity.getEvents().addListener("betPlaced", this::startGame);
        gameEntity.getEvents().addListener("robotFighting:startFight", this::startFight);
        gameEntity.getEvents().addListener("robotFighting:encourage", this::encourageFighter);
    }

    /**
     * Testing constructor that skips LibGDX or entity initialization.
     */
    protected RobotFightingGame(RobotFightingText text) {
        this.encouragingMessages = text;
        this.gameEntity = new Entity();
        this.gameDisplay = null;
    }

    /**
     * Handles player interaction to toggle the minigame screen.
     * <p>
     * When visible, the game is paused; when hidden, gameplay resumes.
     * </p>
     */
    void handleInteract() {
        if (gameDisplayed) {
            gameDisplay.hide();
            gameDisplayed = false;
        }
    }

    void startGame() {
        if (!gameDisplayed) {
            gameDisplay.show();
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
        InventoryComponent inventory = ServiceLocator.getPlayer().getComponent(InventoryComponent.class);
        game.addComponent(new BettingComponent(2, inventory));
        game.addComponent(new TextureRenderComponent("images/tree.png"));
        return game;
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

                int damage = (int) (Math.random() * 5 * encourageMult + 8);
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
    void encourageFighter() {
        gameDisplay.encourageFighter(encouragingMessages.getRandom());
        boolean successfulEncourage = Math.random() < 0.5;

        if (!successfulEncourage) return;

        double baseStrength = 0.1 + Math.random() * 0.2;
        encourageMult += (1.5 - encourageMult) * baseStrength;
        encourageMult = Math.min(encourageMult, 1.5);
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
                gameDisplay.playExplosionEffect(gameDisplay.getChosenActor());
                gameDisplay.playExplosionEffect(gameDisplay.getOtherActor(gameDisplay.getChosenActor()));
            } else {
                gameDisplay.playExplosionEffect(gameDisplay.getOtherActor(gameDisplay.getChosenActor()));
            }
        } else {
            gameDisplay.playExplosionEffect(gameDisplay.getChosenActor());
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (chosenFighterHp <= 0) {
                    if (otherFighterHp <= 0) {
                        gameEntity.getEvents().trigger("draw");
                    } else {
                        gameEntity.getEvents().trigger("lose");
                    }
                } else {
                    gameEntity.getEvents().trigger("win");
                }
                gameEntity.getEvents().trigger("interact");
            }
        }, 1f);
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