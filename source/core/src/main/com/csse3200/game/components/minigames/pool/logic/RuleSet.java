package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.minigames.pool.physics.BallFactory;

public interface RuleSet {
    void onNewRack(BallFactory balls);           // place balls/cue for a new rack
    void onShoot(Body cue, float dx, float dy, float power);
    void onBallPotted(int ballId, int pocketIndex);
    void onScratch(int pocketIndex);
    void updateTurn();                            // e.g., detect end-of-motion and switch players
}

