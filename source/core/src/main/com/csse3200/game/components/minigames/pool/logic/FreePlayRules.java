package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.minigames.pool.physics.*;

public class FreePlayRules implements RuleSet {
    private final PoolWorld world; private final TableConfig cfg;
    private BallFactory balls;

    public FreePlayRules(PoolWorld w, TableConfig c){ this.world=w; this.cfg=c; }

    @Override public void onNewRack(BallFactory balls) {
        this.balls = balls;
        balls.resetCue(new Vector2(-cfg.tableW()*0.30f, 0f));
        balls.resetRack(new Vector2(cfg.tableW()*0.25f, 0f));
    }

    @Override public void onShoot(Body cue, float dx, float dy, float power) {
        if (cue==null) return; if (Math.abs(dx)<1e-4 && Math.abs(dy)<1e-4) return;
        float p = MathUtils.clamp(power, 0f, 1f);
        Vector2 dir = new Vector2(dx,dy).nor();
        Vector2 impulse = dir.scl(GameTuning.MAX_IMPULSE * p * cue.getMass());
        cue.applyLinearImpulse(impulse, cue.getWorldCenter(), true);
    }

    @Override public void onBallPotted(int ballId, int pocketIndex) {
        // In free play, nothing to do beyond removal (handled by PocketContactSystem)
    }

    @Override public void onScratch(int pocketIndex) {
        balls.resetCue(new Vector2(-cfg.tableW()*0.30f, 0f));
    }

    @Override public void updateTurn() {
        // Optional: detect motion stop and do something. Left empty for free play.
    }
}
