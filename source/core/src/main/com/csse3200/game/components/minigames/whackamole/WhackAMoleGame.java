package com.csse3200.game.components.minigames.whackamole;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.badlogic.gdx.utils.Timer;
import java.util.Random;

public class WhackAMoleGame {
    private static final int TARGET_SCORE = 20;
    private static final int MAX_MISSES = 2;
    private static final float SPAWN_PERIOD = 0.8f;
    private static final float UP_DURATION  = 0.6f;

    private final Entity gameEntity;
    private final WhackAMoleDisplay display;

    private boolean uiShown = false;
    private boolean running = false;

    private final Random rng = new Random();
    private Timer.Task loopTask;
    private Timer.Task hideTask;

    // Track whether the current mole was hit before it despawns
    private int currentIdx = -1;
    private boolean currentHit = false;
    private int misses = 0;
    private int roundToken = 0;

    public WhackAMoleGame() {
        gameEntity = initGameEntity();
        display = gameEntity.getComponent(WhackAMoleDisplay.class);
        gameEntity.getEvents().addListener("interact", this::onInteract);

        // Receive start/stop from the display’s Start button
        gameEntity.getEvents().addListener("wm:start", this::onStart);
        gameEntity.getEvents().addListener("wm:stop", this::onStop);

        // Receive on mole hit.
        gameEntity.getEvents().addListener("wm:hit", this::onHit);
    }

    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new WhackAMoleDisplay());
        game.addComponent(new TextureRenderComponent("images/mole.png"));
        game.setInteractable(true);
        return game;
    }

    // UI toggle
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

    // Game start/stop
    private void onStart() {
        if (running) return;
        running = true;
        resetRuntime();
        display.resetScore(); // ← add this so a fresh round starts at 0
        loopTask = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override public void run() { startRound(); }
        }, 0f, 0.8f);
    }

    private void onStop() {
        running = false;
        if (loopTask != null) { loopTask.cancel(); loopTask = null; }
        if (hideTask != null) { hideTask.cancel(); hideTask = null; }
        display.setRunning(false);
        display.hideAllMoles();
        currentIdx = -1;
    }

    private void resetRuntime() {
        misses = 0;
        currentIdx = -1;
        currentHit = false;
        roundToken++;
        if (hideTask != null) { hideTask.cancel(); hideTask = null; }
    }

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

    private void handleMiss() {
        misses++;
        if (misses >= MAX_MISSES) {
            onStop();
            display.resetScore();
            display.showEnd("You Lose", "You missed " + misses + " moles.\nTry again!");
        }
    }

    private void onHit() {
        if (!running) return;
        currentHit = true;
        if (display.getScore() >= TARGET_SCORE) {
            onStop();
            display.resetScore();
            display.showEnd("You Win!", "Reached " + TARGET_SCORE + " points!");
        }
    }

    public Entity getGameEntity() {
        return gameEntity;
    }
}