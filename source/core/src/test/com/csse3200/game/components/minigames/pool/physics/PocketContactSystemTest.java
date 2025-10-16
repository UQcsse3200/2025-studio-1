package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PocketContactSystemTest {
    private final World world = new World(new Vector2(0, 0), true);
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
    void scratchAndPotAreDetectedAndProcessed() {
        // Build rails + pockets
        TableBuilder tb = new TableBuilder(poolWorld, cfg);
        tb.buildRails();
        tb.buildPocketSensors();

        // Create cue and one object ball
        BallFactory balls = new BallFactory(poolWorld, cfg);
        balls.spawnCue(new Vector2(0, 0));
        Body cue = balls.getCueBody();
        Body obj = world.createBody(new BodyDef() {{ type = BodyType.DynamicBody; position.set(0.1f, 0.1f); }});
        CircleShape sh = new CircleShape(); sh.setRadius(cfg.ballR());
        FixtureDef fd = new FixtureDef(); fd.shape = sh;
        fd.filter.categoryBits = TableBuilder.LAYER_BALL;
        fd.filter.maskBits = (short)(TableBuilder.LAYER_RAIL | TableBuilder.LAYER_POCKET | TableBuilder.LAYER_BALL);
        obj.createFixture(fd); sh.dispose();

        // Fake id map/list for PocketContactSystem to manage
        Map<Integer, Body> idMap = new HashMap<>();
        idMap.put(0, cue);
        idMap.put(1, obj);
        List<Body> objects = new ArrayList<>();
        objects.add(obj);

        PocketContactSystem pcs = new PocketContactSystem(poolWorld, cfg);
        pcs.bindBallRefs(cue, idMap, objects);

        AtomicBoolean scratched = new AtomicBoolean(false);
        AtomicInteger pottedId = new AtomicInteger(-1);
        AtomicInteger pottedPocket = new AtomicInteger(-1);

        pcs.setListener(new PocketContactSystem.Listener() {
            @Override public void onScratch(int pocketIndex) { scratched.set(true); }
            @Override public void onPotted(int ballId, int pocketIndex) {
                pottedId.set(ballId); pottedPocket.set(pocketIndex);
            }
        });

        pcs.install();

        // Drop cue into top-center pocket (approx)
        // Pocket centers are computed from cfg; easiest is to place near +Y edge with X~0
        cue.setTransform(new Vector2(0f, cfg.tableH()/2f - cfg.pocketInsetY() + cfg.pocketFunnel() - 0.01f), 0f);
        // tiny velocity to trigger contact processing
        cue.setLinearVelocity(0, 0.1f);

        // Drop object into bottom-center pocket
        obj.setTransform(new Vector2(0f, -cfg.tableH()/2f + cfg.pocketInsetY() - cfg.pocketFunnel() + 0.01f), 0f);
        obj.setLinearVelocity(0, -0.1f);

        // step to generate contacts
        for (int i = 0; i < 120; i++) world.step(1/120f, 6, 2);

        // process deferred (world must be unlocked)
        pcs.processDeferred();

        // cue scratched
        assertTrue(scratched.get(), "Expected scratch event");
        // object potted & removed
        assertEquals(1, pottedId.get(), "Expected ball id 1 to be potted");
        assertFalse(objects.contains(obj), "Object ball list should remove potted ball");
        assertFalse(idMap.containsKey(1), "Id map should remove potted ball");
    }
}