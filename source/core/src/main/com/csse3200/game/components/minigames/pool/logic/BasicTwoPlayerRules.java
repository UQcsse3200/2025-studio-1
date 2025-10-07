package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.minigames.pool.physics.BallFactory;
import com.csse3200.game.components.minigames.pool.physics.GameTuning;
import com.csse3200.game.components.minigames.pool.physics.TableConfig;

import java.util.List;

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

    public BasicTwoPlayerRules(TableConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void setListener(RulesEvents listener) {
        this.events = listener;
    }

    @Override
    public void onNewRack(BallFactory balls) {
        this.balls = balls;
        // reset positions
        balls.resetCue(new Vector2(-cfg.tableW() * 0.30f, 0f));
        balls.resetRack(new Vector2(cfg.tableW() * 0.25f, 0f));
        // reset scores & state for a brand new game
        p1Score = 0;
        p2Score = 0;
        currentPlayer = 1;
        pottedThisTurn = false;
        foulThisTurn = false;
        shotActive = false;
        lastTurnNotified = null;
        notifyTurnChangedOnce();
    }

    @Override
    public void onShoot(Body cue, float dx, float dy, float power) {
        if (cue == null) return;
        if (Math.abs(dx) < 1e-4 && Math.abs(dy) < 1e-4) return;
        float p = MathUtils.clamp(power, 0f, 1f);
        Vector2 dir = new Vector2(dx, dy).nor();
        Vector2 impulse = dir.scl(GameTuning.MAX_IMPULSE * p * cue.getMass());
        cue.applyLinearImpulse(impulse, cue.getWorldCenter(), true);
        // starting a shot resets per-turn flags
        pottedThisTurn = false;
        foulThisTurn = false;
        shotActive = true;
    }

    @Override
    public void onBallPotted(int ballId, int pocketIndex) {
        // simple scoring: any object ball += 1 to current player
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

    @Override
    public void onScratch(int pocketIndex) {
        foulThisTurn = true;
    }

    @Override
    public void updateTurn() {
        if (balls == null) return;
        if (!shotActive) return;
        if (!motionStopped()) return;

        if (foulThisTurn) {
            switchTurn();
            // ball-in-hand: place cue for incoming player in the kitchen
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

    @Override
    public boolean isShotActive() {
        return shotActive;
    }

    private void switchTurn() {
        currentPlayer = (currentPlayer == 1) ? 2 : 1;
    }

    private int otherPlayer() {
        return currentPlayer == 1 ? 2 : 1;
    }

    private boolean motionStopped() {
        Body cue = balls.getCueBody();
        if (cue != null && cue.getLinearVelocity().len2() > 1e-4f) return false;
        List<Body> objs = balls.getObjectBodies();
        for (Body b : objs) if (b.getLinearVelocity().len2() > 1e-4f) return false;
        return true;
    }

    private void notifyTurnChangedOnce() {
        if (events == null) return;
        if (lastTurnNotified != null && lastTurnNotified == currentPlayer) return;
        lastTurnNotified = currentPlayer;
        events.onTurnChanged(currentPlayer, p1Score, p2Score);
    }
}