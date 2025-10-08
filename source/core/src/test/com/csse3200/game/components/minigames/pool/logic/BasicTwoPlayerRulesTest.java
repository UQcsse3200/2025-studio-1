// src/test/java/com/csse3200/game/components/minigames/pool/logic/BasicTwoPlayerRulesTest.java
package com.csse3200.game.components.minigames.pool.logic;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.minigames.pool.physics.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BasicTwoPlayerRulesTest {
    private final World world = new World(new Vector2(0,0), true);
    private final PoolWorld poolWorld = new PoolWorld(world);
    private final TableConfig cfg = TableConfig.builder()
            .tableSize(2.24f, 1.12f)
            .railThickness(0.105f, 0.085f)
            .ballRadius(0.0285f)
            .pocketRadiusScale(2.5f)
            .pocketInsetScaleX(1f)
            .pocketInsetScaleY(1f)
            .pocketFunnelScale(0.9f)
            .build();

    @AfterEach
    void cleanup() { world.dispose(); }

    @Test
    void scoringAndTurnChanges() {
        BallFactory factory = new BallFactory(poolWorld, cfg);
        factory.spawnCue(new Vector2(0, 0));
        factory.spawnRackTriangle(new Vector2(0.2f, 0));
        BasicTwoPlayerRules rules = new BasicTwoPlayerRules(cfg);

        AtomicInteger lastTurn = new AtomicInteger(-1);
        AtomicInteger p1 = new AtomicInteger(0);
        AtomicInteger p2 = new AtomicInteger(0);

        rules.setListener(new RulesEvents() {
            @Override public void onTurnChanged(int current, int s1, int s2) { lastTurn.set(current); p1.set(s1); p2.set(s2); }
            @Override public void onScoreUpdated(int current, int s1, int s2) { p1.set(s1); p2.set(s2); }
            @Override public void onFoul(int foulingPlayer, String reason) {}
        });

        rules.onNewRack(factory); // this will set P1 turn

        // Simulate a shot by P1
        rules.onShoot(factory.getCueBody(), 1f, 0f, 0.6f);
        assertTrue(rules.isShotActive());

        // P1 pots a ball (e.g., id=3)
        rules.onBallPotted(3, 0);
        assertEquals(1, p1.get());
        assertEquals(0, p2.get());

        stopAllMotion(factory);


        // motion stopped, rules updateTurn() ends the shot, P1 keeps turn since potted
        rules.updateTurn();
        assertFalse(rules.isShotActive());
        // still player 1’s turn because they potted
        assertEquals(1, lastTurn.get());
    }

    @Test
    void foulSwitchesTurnAndResetsCue() {
        BallFactory factory = new BallFactory(poolWorld, cfg);
        factory.spawnCue(new Vector2(0, 0));
        factory.spawnRackTriangle(new Vector2(0.2f, 0));
        BasicTwoPlayerRules rules = new BasicTwoPlayerRules(cfg);

        AtomicInteger lastTurn = new AtomicInteger(-1);
        rules.setListener(new RulesEvents() {
            @Override public void onTurnChanged(int current, int p1, int p2) { lastTurn.set(current); }
            @Override public void onScoreUpdated(int current, int p1, int p2) {}
            @Override public void onFoul(int foulingPlayer, String reason) {}
        });

        rules.onNewRack(factory);
        rules.onShoot(factory.getCueBody(), 1f, 0f, 0.6f);
        rules.onScratch(0); // foul this turn

        stopAllMotion(factory);

        rules.updateTurn();

        assertEquals(2, lastTurn.get(), "Turn should pass to player 2 after scratch");
    }

    private static void stopAllMotion(BallFactory factory) {
        if (factory.getCueBody() != null) {
            factory.getCueBody().setLinearVelocity(0, 0);
            factory.getCueBody().setAngularVelocity(0f);
        }
        for (var b : factory.getObjectBodies()) {
            b.setLinearVelocity(0, 0);
            b.setAngularVelocity(0f);
        }
    }
}