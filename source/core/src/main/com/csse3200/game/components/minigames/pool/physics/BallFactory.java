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
 * <p>
 * Handles spawning, resetting, and tracking all balls in the pool world.
 * IDs are assigned as follows:
 * <ul>
 *     <li>Cue ball = 0</li>
 *     <li>Object balls = 1..15 (in rack order)</li>
 * </ul>
 */
public class BallFactory {
    private static final float BALL_DENSITY = 0.8f;
    private static final float BALL_FRICTION = 0.2f;
    private static final float BALL_RESTITUTION = 0.95f;
    private static final float LIN_DAMP = 0.90f;
    private static final float ANG_DAMP = 0.40f;

    /**
     * Center-to-center gap between adjacent balls in the rack (in radii).
     */
    private static final float RACK_GAP_IN_RADII = 2.02f;

    /**
     * How much each row advances in X vs. the nominal gap (slight compaction).
     */
    private static final float ROW_X_SPACING_SCALE = 0.87f;

    /**
     * Small random offset in Y to avoid perfect stacking overlaps.
     */
    private static final float SPAWN_Y_JITTER = 0.001f;

    private final PoolWorld world;
    private final TableConfig cfg;

    private final List<Body> objects = new ArrayList<>(15);
    private final Map<Integer, Body> idToBody = new HashMap<>(16);
    private Body cue;

    /**
     * Constructs a {@code BallFactory}.
     *
     * @param world the {@link PoolWorld} containing the Box2D physics world
     * @param cfg   table configuration defining ball size and layout
     */
    public BallFactory(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    /**
     * Returns whether both cue and object balls are currently built.
     *
     * @return {@code true} if the cue and all balls exist, otherwise {@code false}
     */
    public boolean isBuilt() {
        return cue != null && !objects.isEmpty();
    }

    /**
     * Returns the cue ball body.
     *
     * @return the cue ball {@link Body}, or {@code null} if not spawned
     */
    public Body getCueBody() {
        return cue;
    }

    /**
     * Returns a list of all object ball bodies.
     *
     * @return list of object {@link Body} instances
     */
    public List<Body> getObjectBodies() {
        return objects;
    }

    /**
     * Returns the mapping of ball IDs to their corresponding physics bodies.
     *
     * @return map of ball IDs to {@link Body} instances
     */
    public Map<Integer, Body> getIdMap() {
        return idToBody;
    }

    /**
     * Spawns the cue ball at the given position.
     *
     * @param pos world-space position of the cue ball
     */
    public void spawnCue(Vector2 pos) {
        cue = createBall(pos.x, pos.y);
        idToBody.put(0, cue);
    }

    /**
     * Spawns a standard 5-row triangle rack of 15 object balls if not already present.
     *
     * @param rackOrigin world-space origin where the first (top) ball will be placed
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

    /**
     * Resets the cue ball to a given position, stopping its motion.
     *
     * @param pos world-space position to place the cue ball
     */
    public void resetCue(Vector2 pos) {
        if (cue != null) {
            cue.setActive(true);
            cue.setTransform(pos.x, pos.y, 0f);
            cue.setLinearVelocity(Vector2.Zero);
            cue.setAngularVelocity(0f);
        }
    }

    /**
     * Resets all object balls into a triangular rack layout at the given origin.
     *
     * @param rackOrigin world-space position of the top ball in the rack
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

    /**
     * Returns a list of the current world positions of all object balls.
     *
     * @return list of {@link Vector2} positions
     */
    public List<Vector2> getObjectBallPositions() {
        List<Vector2> out = new ArrayList<>(objects.size());
        for (Body b : objects) {
            out.add(b.getPosition());
        }
        return out;
    }

    public Vector2[] getObjectBallPositionsById() {
        Vector2[] byId = new Vector2[15];
        for (Map.Entry<Integer, Body> e : idToBody.entrySet()) {
            int id = e.getKey();
            if (id <= 0 || id > 15) continue; // skip cue / out of range
            Body b = e.getValue();
            if (b != null && b.isActive()) {
                byId[id - 1] = b.getPosition().cpy();
            } else {
                byId[id - 1] = null;
            }
        }
        return byId;
    }

    /**
     * Creates a dynamic circular body representing a single pool ball.
     *
     * @param x world-space x position
     * @param y world-space y position
     * @return a fully configured {@link Body} with appropriate filters and damping
     */
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
                    TableBuilder.LAYER_BALL |  // ball–ball
                            TableBuilder.LAYER_RAIL |  // rails
                            TableBuilder.LAYER_POCKET   // pockets (sensors)
            );
            fixture.setFilterData(filter);
        } finally {
            circle.dispose();
        }

        body.setLinearDamping(LIN_DAMP);
        body.setAngularDamping(ANG_DAMP);

        // Add a small random vertical offset to prevent perfect overlaps.
        body.setTransform(
                body.getPosition().x,
                body.getPosition().y + MathUtils.random(-SPAWN_Y_JITTER, SPAWN_Y_JITTER),
                0f
        );

        return body;
    }

    /**
     * Determines the row index (0–4) of a given ball index in the rack.
     *
     * @param idx the ball index (0–14)
     * @return the row number containing that ball
     */
    private static int rowOf(int idx) {
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int count = r + 1;
            if (rem < count) return r;
            rem -= count;
        }
        return 4;
    }

    /**
     * Determines the position index of a ball within its row.
     *
     * @param idx the ball index (0–14)
     * @return position within the row (0-based)
     */
    private static int posInRow(int idx) {
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int count = r + 1;
            if (rem < count) return rem;
            rem -= count;
        }
        return 0;
    }
}