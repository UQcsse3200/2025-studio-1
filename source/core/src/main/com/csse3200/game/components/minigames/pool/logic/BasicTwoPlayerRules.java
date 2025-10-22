package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.minigames.pool.physics.BallFactory;
import com.csse3200.game.components.minigames.pool.physics.GameTuning;
import com.csse3200.game.components.minigames.pool.physics.TableConfig;

import java.util.List;

/**
 * Implements a simple two-player pool rule set.
 * <p>
 * Tracks scores, turns, fouls, and determines when to switch players.
 * Handles turn updates once all motion has stopped.
 */
public class BasicTwoPlayerRules implements RuleSet {
    private final TableConfig cfg;
    private BallFactory balls;
    private RulesEvents events;

    private int currentPlayer = 1;     // 1 or 2
    private int p1Score = 0, p2Score = 0;

    // per-turn flags
    private boolean pottedThisTurn = false;
    private boolean foulThisTurn = false;
    private boolean shotActive = false;
    private Integer lastTurnNotified = null;

    /**
     * Creates a new two-player rule set.
     *
     * @param cfg table configuration containing size and positioning data
     */
    public BasicTwoPlayerRules(TableConfig cfg) {
        this.cfg = cfg;
    }

    /**
     * Sets the listener for rule-related game events.
     *
     * @param listener event listener instance
     */
    @Override
    public void setListener(RulesEvents listener) {
        this.events = listener;
    }

    /**
     * Called when a new rack begins.
     * Resets cue and object ball positions, scores, and player state.
     *
     * @param balls the {@link BallFactory} managing pool balls
     */
    @Override
    public void onNewRack(BallFactory balls) {
        this.balls = balls;
        balls.resetCue(new Vector2(-cfg.tableW() * 0.30f, 0f));
        balls.resetRack(new Vector2(cfg.tableW() * 0.25f, 0f));

        p1Score = 0;
        p2Score = 0;
        currentPlayer = 1;
        pottedThisTurn = false;
        foulThisTurn = false;
        shotActive = false;
        lastTurnNotified = null;
        notifyTurnChangedOnce();
    }

    /**
     * Applies an impulse to the cue ball to simulate a shot.
     *
     * @param cue   the cue ball body
     * @param dx    x-direction component of aim
     * @param dy    y-direction component of aim
     * @param power shot power in range [0..1]
     */
    @Override
    public void onShoot(Body cue, float dx, float dy, float power) {
        if (cue == null) return;
        if (shotActive) return;
        if (Math.abs(dx) < 1e-4 && Math.abs(dy) < 1e-4) return;

        float p = MathUtils.clamp(power, 0f, 1f);
        Vector2 dir = new Vector2(dx, dy).nor();
        Vector2 impulse = dir.scl(GameTuning.MAX_IMPULSE * p * cue.getMass());
        cue.applyLinearImpulse(impulse, cue.getWorldCenter(), true);

        pottedThisTurn = false;
        foulThisTurn = false;
        shotActive = true;
    }

    /**
     * Handles a potted ball.
     * In this simple rule set, every object ball is worth one point.
     *
     * @param ballId      ID of the ball that was potted (0 = cue ball)
     * @param pocketIndex index of the pocket the ball entered
     */
    @Override
    public void onBallPotted(int ballId, int pocketIndex) {
        if (ballId > 0) {
            if (currentPlayer == 1) {
                p1Score++;
            } else {
                p2Score++;
            }
            pottedThisTurn = true;
        }
        if (events != null) {
            events.onScoreUpdated(currentPlayer, p1Score, p2Score);
        }
    }

    /**
     * Handles a cue ball scratch event.
     *
     * @param pocketIndex index of the pocket where the cue ball fell
     */
    @Override
    public void onScratch(int pocketIndex) {
        foulThisTurn = true;
    }

    /**
     * Updates turn and foul logic once all balls have stopped moving.
     * Switches players when appropriate and notifies listeners.
     */
    @Override
    public void updateTurn() {
        if (balls == null) return;
        if (!shotActive) return;
        if (!motionStopped()) return;

        if (foulThisTurn) {
            switchTurn();
            balls.resetCue(new Vector2(-cfg.tableW() * 0.30f, 0f));
            foulThisTurn = false;
            pottedThisTurn = false;
            if (events != null) events.onFoul(otherPlayer(), "scratch");
            shotActive = false;
            notifyTurnChangedOnce();
            return;
        }

        if (!pottedThisTurn) {
            switchTurn();
            if (events != null) events.onTurnChanged(currentPlayer, p1Score, p2Score);
        }
        pottedThisTurn = false;
        shotActive = false;
    }

    /**
     * @return true if a shot is currently in progress
     */
    @Override
    public boolean isShotActive() {
        return shotActive;
    }

    /**
     * Switches to the other player's turn.
     */
    private void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    /**
     * @return the ID of the other player (1 or 2)
     */
    private int otherPlayer() {
        return currentPlayer == 1 ? 2 : 1;
    }

    /**
     * Checks if all balls have stopped moving.
     *
     * @return true if no balls are moving
     */
    private boolean motionStopped() {
        Body cue = balls.getCueBody();
        if (cue != null && cue.getLinearVelocity().len2() > 1e-4f) return false;

        List<Body> objs = balls.getObjectBodies();
        for (Body b : objs) {
            if (b.getLinearVelocity().len2() > 1e-4f) return false;
        }
        return true;
    }

    /**
     * Notifies the listener that the current player's turn has started,
     * but only once per turn.
     */
    private void notifyTurnChangedOnce() {
        if (events == null) return;
        if (lastTurnNotified != null && lastTurnNotified == currentPlayer) return;
        lastTurnNotified = currentPlayer;
        events.onTurnChanged(currentPlayer, p1Score, p2Score);
    }
}