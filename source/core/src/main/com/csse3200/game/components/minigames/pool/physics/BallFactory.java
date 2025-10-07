package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates and manages the cue ball and object balls for the pool table.
 * IDs: cue = 0, objects = 1..15 in rack order.
 */
public class BallFactory {
    // --- Physical material & damping ---
    private static final float BALL_DENSITY = 0.8f;
    private static final float BALL_FRICTION = 0.2f;
    private static final float BALL_RESTITUTION = 0.95f;
    private static final float LIN_DAMP = 0.90f;
    private static final float ANG_DAMP = 0.40f;

    // --- Rack geometry (expressed relative to ball radius) ---
    /**
     * Center-to-center gap between adjacent balls in the rack (in radii).
     */
    private static final float RACK_GAP_IN_RADII = 2.02f;
    /**
     * How much each row advances in X vs. the nominal gap (slight compaction).
     */
    private static final float ROW_X_SPACING_SCALE = 0.87f;

    // --- Jitter to avoid perfect overlaps at spawn ---
    private static final float SPAWN_Y_JITTER = 0.001f;

    private final PoolWorld world;
    private final TableConfig cfg;

    private final List<Body> objects = new ArrayList<>(15);
    private final Map<Integer, Body> idToBody = new HashMap<>(16);

    private Body cue;

    public BallFactory(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    // --- Rack indexing helpers (0..14 flattened across rows of 1..5) ---
    private static int rowOf(int idx) {
        // Rows have 1,2,3,4,5 balls (sum = 15). Find which row the index falls in.
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int count = r + 1;
            if (rem < count) return r;
            rem -= count;
        }
        return 4;
    }

    private static int posInRow(int idx) {
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int count = r + 1;
            if (rem < count) return rem;
            rem -= count;
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------------
    public boolean isBuilt() {
        return cue != null && !objects.isEmpty();
    }

    public Body getCueBody() {
        return cue;
    }

    public List<Body> getObjectBodies() {
        return objects;
    }

    public Map<Integer, Body> getIdMap() {
        return idToBody;
    }

    // ------------------------------------------------------------------------
    // Spawning
    // ------------------------------------------------------------------------
    public void spawnCue(Vector2 pos) {
        cue = createBall(pos.x, pos.y);
        idToBody.put(0, cue);
    }

    /**
     * Spawns a standard 5-row triangle (15 balls) if not already present.
     *
     * @param rackOrigin world-space origin; row 0 starts here.
     */
    public void spawnRackTriangle(Vector2 rackOrigin) {
        if (!objects.isEmpty()) return;

        final float r = cfg.ballR();
        final float gap = r * RACK_GAP_IN_RADII;

        int spawned = 0;
        for (int row = 0; row < 5; row++) {
            final float x = rackOrigin.x + row * (gap * ROW_X_SPACING_SCALE);
            final float yStart = rackOrigin.y - row * r;
            for (int i = 0; i <= row; i++) {
                final float y = yStart + i * (2f * r);
                Body b = createBall(x, y);
                objects.add(b);
                idToBody.put(objects.size(), b); // IDs 1..15
                if (++spawned == 15) return;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Resets
    // ------------------------------------------------------------------------
    public void resetCue(Vector2 pos) {
        if (cue != null) {
            cue.setActive(true);
            cue.setTransform(pos.x, pos.y, 0f);
            cue.setLinearVelocity(Vector2.Zero);
            cue.setAngularVelocity(0f);
        }
    }

    /**
     * Repositions all object balls into a triangle at the given origin.
     */
    public void resetRack(Vector2 rackOrigin) {
        if (objects.isEmpty()) return;

        final float r = cfg.ballR();
        final float gap = r * RACK_GAP_IN_RADII;

        int idx = 0;
        for (Body b : objects) {
            int row = rowOf(idx);
            int p = posInRow(idx);

            final float x = rackOrigin.x + row * (gap * ROW_X_SPACING_SCALE);
            final float y = rackOrigin.y - row * r + p * (2f * r);

            b.setTransform(x, y, 0f);
            b.setLinearVelocity(Vector2.Zero);
            b.setAngularVelocity(0f);
            idx++;
        }
    }

    public List<Vector2> getObjectBallPositions() {
        List<Vector2> out = new ArrayList<>(objects.size());
        for (Body b : objects) {
            out.add(b.getPosition());
        }
        return out;
    }

    // ------------------------------------------------------------------------
    // Internals
    // ------------------------------------------------------------------------
    private Body createBall(float x, float y) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(x, y);

        Body body = world.raw().createBody(bd);

        CircleShape circle = new CircleShape();
        circle.setRadius(cfg.ballR());

        try {
            FixtureDef fd = new FixtureDef();
            fd.shape = circle;
            fd.density = BALL_DENSITY;
            fd.friction = BALL_FRICTION;
            fd.restitution = BALL_RESTITUTION;

            Fixture fixture = body.createFixture(fd);
            Filter filter = new Filter();
            filter.categoryBits = TableBuilder.LAYER_BALL;
            filter.maskBits = (short) (
                    TableBuilder.LAYER_BALL | // ball–ball
                            TableBuilder.LAYER_RAIL | // rails
                            TableBuilder.LAYER_POCKET   // pockets (sensors)
            );
            fixture.setFilterData(filter);
        } finally {
            circle.dispose();
        }

        body.setLinearDamping(LIN_DAMP);
        body.setAngularDamping(ANG_DAMP);

        // Small vertical jitter to prevent perfect overlaps on spawn.
        body.setTransform(
                body.getPosition().x,
                body.getPosition().y + MathUtils.random(-SPAWN_Y_JITTER, SPAWN_Y_JITTER),
                0f
        );
        return body;
    }
}