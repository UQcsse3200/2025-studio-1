package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BallFactory {
    // materials
    private static final float BALL_DENSITY = 0.8f, BALL_FRICTION = 0.2f, BALL_RESTITUTION = 0.95f;
    private static final float LIN_DAMP = 0.90f, ANG_DAMP = 0.40f;
    private final PoolWorld world;
    private final TableConfig cfg;
    private final List<Body> objects = new ArrayList<>();
    private final Map<Integer, Body> idToBody = new HashMap<>();
    private Body cue;

    public BallFactory(PoolWorld w, TableConfig c) {
        this.world = w;
        this.cfg = c;
    }

    private static int rowOf(int idx) {
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int c = r + 1;
            if (rem < c) return r;
            rem -= c;
        }
        return 4;
    }

    private static int posInRow(int idx) {
        int rem = idx;
        for (int r = 0; r < 5; r++) {
            int c = r + 1;
            if (rem < c) return rem;
            rem -= c;
        }
        return 0;
    }

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

    public void spawnCue(Vector2 pos) {
        cue = createBall(pos);
        idToBody.put(0, cue);
    }

    public void spawnRackTriangle(Vector2 rackOrigin) {
        if (!objects.isEmpty()) return;
        float gap = cfg.ballR() * 2.02f;
        int spawned = 0;
        for (int row = 0; row < 5; row++) {
            float x = rackOrigin.x + row * (gap * 0.87f);
            float yStart = -row * cfg.ballR();
            for (int i = 0; i <= row; i++) {
                float y = yStart + i * (2f * cfg.ballR());
                Body b = createBall(new Vector2(x, y));
                objects.add(b);
                idToBody.put(objects.size(), b);
                if (++spawned == 15) return;
            }
        }
    }

    public void resetCue(Vector2 pos) {
        if (cue != null) {
            cue.setTransform(pos, 0f);
            cue.setLinearVelocity(Vector2.Zero);
            cue.setAngularVelocity(0f);
        }
    }

    public void resetRack(Vector2 rackOrigin) {
        int idx = 0;
        float gap = cfg.ballR() * 2.02f;
        for (Body b : objects) {
            int row = rowOf(idx);
            int p = posInRow(idx);
            float x = rackOrigin.x + row * (gap * 0.87f);
            float y = -row * cfg.ballR() + p * (2f * cfg.ballR());
            b.setTransform(new Vector2(x, y), 0f);
            b.setLinearVelocity(Vector2.Zero);
            b.setAngularVelocity(0f);
            idx++;
        }
    }

    public List<Vector2> getObjectBallPositions() {
        List<Vector2> out = new ArrayList<>(objects.size());
        for (Body b : objects) out.add(b.getPosition());
        return out;
    }

    private Body createBall(Vector2 pos) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(pos);
        Body body = world.raw().createBody(bd);
        CircleShape circle = new CircleShape();
        circle.setRadius(cfg.ballR());
        FixtureDef fd = new FixtureDef();
        fd.shape = circle;
        fd.density = BALL_DENSITY;
        fd.friction = BALL_FRICTION;
        fd.restitution = BALL_RESTITUTION;
        Filter filter = new Filter();
        filter.categoryBits = TableBuilder.LAYER_BALL;
        filter.maskBits = (short)(
                TableBuilder.LAYER_BALL   |  // ball–ball
                        TableBuilder.LAYER_RAIL   |  // rails
                        TableBuilder.LAYER_POCKET    // pockets (sensors)
        );
        body.createFixture(fd).setFilterData(filter);
        circle.dispose();
        body.setLinearDamping(LIN_DAMP);
        body.setAngularDamping(ANG_DAMP);
        // jitter
        body.setTransform(body.getPosition().x, body.getPosition().y + MathUtils.random(-0.001f, 0.001f), 0f);
        return body;
    }
}
