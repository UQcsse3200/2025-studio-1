package com.csse3200.game.components.minigames.whackamole;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.badlogic.gdx.utils.Timer;
import java.util.Random;

/**
 * Whack-A-Mole game logic:
 * - Owns run state (running, misses, current mole)
 * - Shows random moles on a timer, records hits/misses
 * - Triggers win (20 points) or lose (2 misses) via the display
 * - UI is handled by {@link WhackAMoleDisplay}
 */
public class WhackAMoleGame {
    private static final int TARGET_SCORE = 20;
    private static final int MAX_MISSES = 2;
    private static final float SPAWN_PERIOD = 0.8f;
    private static final float UP_DURATION  = 0.6f;

    private final Entity gameEntity;
    private final WhackAMoleDisplay display;

    private boolean betPlaced = false;
    private boolean uiShown = false;
    private boolean running = false;

    private final Random rng = new Random();
    private Timer.Task loopTask;
    private Timer.Task hideTask;

    private int currentIdx = -1;
    private boolean currentHit = false;
    private int misses = 0;
    private int roundToken = 0;

    public WhackAMoleGame() {
        gameEntity = initGameEntity();
        display = gameEntity.getComponent(WhackAMoleDisplay.class);
        gameEntity.getEvents().addListener("interact", this::onInteract);
        gameEntity.getEvents().addListener("wm:start", this::onStart);
        gameEntity.getEvents().addListener("wm:stop", this::onStop);
        gameEntity.getEvents().addListener("wm:hit", this::onHit);
        gameEntity.getEvents().addListener("betPlaced", this::onBetPlaced);
    }

    /**
     * Build the in-world station:
     * - Base interactable
     * - Display for the UI
     * - Simple texture so it's visible in the room
     */
    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new WhackAMoleDisplay());
        game.addComponent(new TextureRenderComponent("images/mole.png"));
        game.setInteractable(true);
        return game;
    }

    /**
     * Toggle the modal UI:
     * - If open: hide + stop loop
     * - If closed: reset + prepare UI + show
     */
    private void onInteract() {
        if (uiShown) {
            display.hide();
            onStop();
            uiShown = false;
        } else {
            resetRuntime();
            display.prepareToPlay();
            display.show();
            uiShown = true;
        }
    }

    /**
     * Start the loop if not already running:
     * - Reset state + score
     * - Schedule periodic rounds
     */
    private void onStart() {
        if (running) return;

        if (!betPlaced) {
            display.showEnd("Place a Bet", "Set a bet before starting the round.");
            display.setRunning(false);
            return;
        }

        running = true;
        resetRuntime();
        display.resetScore();
        loopTask = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override public void run() { startRound(); }
        }, 0f, 0.8f);
    }

    /**
     * Stop the loop and clean up:
     * - Cancel tasks
     * - Update button label
     * - Hide any visible mole
     */
    private void onStop() {
        running = false;
        if (loopTask != null) { loopTask.cancel(); loopTask = null; }
        if (hideTask != null) { hideTask.cancel(); hideTask = null; }
        display.setRunning(false);
        display.hideAllMoles();
        currentIdx = -1;
    }

    /**
     * Reset per-run state and invalidate late tasks.
     */
    private void resetRuntime() {
        misses = 0;
        currentIdx = -1;
        currentHit = false;
        roundToken++;
        if (hideTask != null) { hideTask.cancel(); hideTask = null; }
    }

    /**
     * One round:
     * - Hide all moles
     * - Pick a random hole and show mole
     * - Schedule hide; if not hit by then, count a miss
     */
    private void startRound() {
        if (!running) return;

        display.hideAllMoles();
        currentHit = false;

        currentIdx = rng.nextInt(9);
        int tokenThisRound = roundToken;
        display.showMoleAt(currentIdx);

        if (hideTask != null) hideTask.cancel();
        hideTask = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                // discard late tasks (if stopped or a new round started)
                if (!running || tokenThisRound != roundToken) return;

                display.hideMoleAt(currentIdx);
                if (!currentHit) handleMiss();
            }
        }, UP_DURATION);
    }

    /**
     * Record a miss; on 2nd miss show lose dialog and stop.
     */
    private void handleMiss() {
        misses++;
        if (misses >= MAX_MISSES) {
            onStop();
            display.resetScore();
            display.showEnd("You Lose", "You missed " + misses + " moles.\nTry again!");
            gameEntity.getEvents().trigger("lose");
            betPlaced = false;
        }
    }

    /**
     * Mole was hit this round; if score >= 20 show win dialog and stop.
     */
    private void onHit() {
        if (!running) return;
        currentHit = true;
        if (display.getScore() >= TARGET_SCORE) {
            onStop();
            display.resetScore();
            display.showEnd("You Win!", "Reached " + TARGET_SCORE + " points!");
            gameEntity.getEvents().trigger("win");
            betPlaced = false;
        }
    }

    private void onBetPlaced() {
        betPlaced = true;
    }

    /** Expose the in-world station entity so areas can place it. */
    public Entity getGameEntity() {
        return gameEntity;
    }
}