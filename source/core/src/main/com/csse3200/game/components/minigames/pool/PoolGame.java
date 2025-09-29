package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal pool setup:
 * - Zero-gravity Box2D world (through PhysicsEngine)
 * - Rectangular rails
 * - 1 cue ball + a simple triangle rack of object balls
 * - Shoot the cue ball using the EightBallPoolDisplay's "Shoot" (dir+power)
 * - Periodic UI sync (positions normalized to [0..1])
 *
 * No turns, fouls, pockets, or win logic.
 */
public class PoolGame {
    // World/table scale (Box2D units = meters-ish). Keep values reasonable for stability.
    private static final float TABLE_W = 2.24f;   // ~8ft table-ish playfield width
    private static final float TABLE_H = 1.12f;   // ~half the width
    private static final float RAIL_THICK = 0.05f;

    // Ball sizing/physics
    private static final float BALL_R = 0.0285f;  // ~57mm diameter → 0.057m radius ≈ 0.0285
    private static final float BALL_DENSITY = 0.8f;
    private static final float BALL_FRICTION = 0.2f;
    private static final float BALL_RESTITUTION = 0.95f;
    private static final float LIN_DAMP = 0.90f;     // “table roll” damping
    private static final float ANG_DAMP = 0.40f;

    // Shooting
    private static final float MAX_IMPULSE = 2.8f; // tune to taste

    // UI sync rate
    private static final float SYNC_PERIOD = 0.033f;

    private final Entity gameEntity;
    private final PoolGameDisplay display;

    private final PhysicsEngine engine;
    private final World world;

    private Body cueBall;
    private final List<Body> objectBalls = new ArrayList<>();
    private Body rails; // static body holding the chain loop

    private boolean uiShown = false;
    private Timer.Task syncTask;

    // ---- Construction ----
    public PoolGame() {
        // Make our own zero-gravity physics if none supplied
        this(new PhysicsEngine(new World(new Vector2(0f, 0f), true), ServiceLocator.getTimeSource()));
    }

    public PoolGame(PhysicsEngine engine) {
        this.engine = engine;
        this.world = engine.getWorld();
        // Ensure zero gravity for a top-down table
        world.setGravity(new Vector2(0f, 0f));

        gameEntity = initGameEntity();
        display = gameEntity.getComponent(PoolGameDisplay.class);

        // Wire up minimal events
        gameEntity.getEvents().addListener("interact", this::onInteract);
        gameEntity.getEvents().addListener("pool:start", this::onStart);
        gameEntity.getEvents().addListener("pool:reset", this::onStart);
        gameEntity.getEvents().addListener("pool:stop", this::onStop);
        gameEntity.getEvents().addListener("pool:shoot", this::onShoot);
    }

    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new PoolGameDisplay());
        game.addComponent(new TextureRenderComponent("images/pool/table.png"));
        game.setInteractable(true);
        return game;
    }

    // ---- UI Handlers ----
    private void onInteract() {
        if (uiShown) {
            onStop();
            display.hide();
            uiShown = false;
        } else {
            buildTableIfNeeded();
            rackBallsSimple();
            startSync();
            display.show();
            uiShown = true;
        }
    }

    private void onStart() {
        buildTableIfNeeded();
        rackBallsSimple();
        startSync();
    }

    private void onStop() {
        stopSync();
        // Keep the world alive; just stop syncing / UI closed.
    }

    // ---- Build world ----
    private void buildTableIfNeeded() {
        if (rails != null && cueBall != null && !objectBalls.isEmpty()) return;

        // Rails as a static chain loop slightly inset from the table extents
        BodyDef railDef = new BodyDef();
        rails = world.createBody(railDef);
        ChainShape loop = new ChainShape();
        float halfW = TABLE_W / 2f, halfH = TABLE_H / 2f, inset = RAIL_THICK;
        Vector2[] verts = new Vector2[] {
                new Vector2(-halfW + inset, -halfH + inset),
                new Vector2( halfW - inset, -halfH + inset),
                new Vector2( halfW - inset,  halfH - inset),
                new Vector2(-halfW + inset,  halfH - inset)
        };
        loop.createLoop(verts);

        FixtureDef railFx = new FixtureDef();
        railFx.shape = loop;
        railFx.friction = 0.0f;
        railFx.restitution = 0.98f; // lively rails
        rails.createFixture(railFx);
        loop.dispose();

        // Create balls if missing
        if (cueBall == null) cueBall = createBall(new Vector2(-TABLE_W * 0.30f, 0f), true);

        if (objectBalls.isEmpty()) {
            // Simple tight triangle near the right side
            Vector2 rackOrigin = new Vector2(TABLE_W * 0.25f, 0f);
            float gap = BALL_R * 2.02f; // slight spacing
            int idx = 0;
            for (int row = 0; row < 5; row++) { // 5-row triangle = 15 balls
                float yStart = -row * BALL_R;
                float x = rackOrigin.x + row * (gap * 0.87f); // horizontal offset
                for (int i = 0; i <= row; i++) {
                    float y = yStart + i * (2f * BALL_R);
                    objectBalls.add(createBall(new Vector2(x, y), false));
                    idx++;
                    if (idx == 15) break;
                }
                if (idx == 15) break;
            }
        }
    }

    private Body createBall(Vector2 pos, boolean isCue) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(pos);
        bd.bullet = true; // continuous collision

        Body body = world.createBody(bd);

        CircleShape circle = new CircleShape();
        circle.setRadius(BALL_R);

        FixtureDef fd = new FixtureDef();
        fd.shape = circle;
        fd.density = BALL_DENSITY;
        fd.friction = BALL_FRICTION;
        fd.restitution = BALL_RESTITUTION;

        body.createFixture(fd);
        circle.dispose();

        body.setLinearDamping(LIN_DAMP);
        body.setAngularDamping(ANG_DAMP);

        // Tiny random jitter so they don't perfectly overlap on spawn
        if (!isCue) {
            body.setTransform(body.getPosition().x, body.getPosition().y + MathUtils.random(-0.001f, 0.001f), body.getAngle());
        }

        return body;
    }

    private void rackBallsSimple() {
        // Reset velocities and place balls
        cueBall.setTransform(new Vector2(-TABLE_W * 0.30f, 0f), 0f);
        cueBall.setLinearVelocity(Vector2.Zero);
        cueBall.setAngularVelocity(0f);

        Vector2 rackOrigin = new Vector2(TABLE_W * 0.25f, 0f);
        float gap = BALL_R * 2.02f;
        int idx = 0;
        for (Body b : objectBalls) {
            int row = rowOf(idx);
            int pos = posInRow(idx);
            float x = rackOrigin.x + row * (gap * 0.87f);
            float yStart = -row * BALL_R;
            float y = yStart + pos * (2f * BALL_R);

            b.setTransform(new Vector2(x, y), 0f);
            b.setLinearVelocity(Vector2.Zero);
            b.setAngularVelocity(0f);

            idx++;
        }

        // Immediate UI sync
        pushPositionsToUI();
    }

    // Helpers to map idx → triangle position
    private static int rowOf(int idx) {
        // rows: 0..4, counts: 1,2,3,4,5 (sum 15)
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

    // ---- Shooting ----
    private void onShoot(float dirX, float dirY, float power) {
        if (cueBall == null) return;
        Vector2 dir = new Vector2(dirX, dirY);
        if (dir.isZero(1e-4f)) return;

        dir.nor();
        float p = MathUtils.clamp(power, 0f, 1f);

        // Apply an impulse at cue-ball center
        Vector2 impulse = dir.scl(MAX_IMPULSE * p * cueBall.getMass());
        cueBall.applyLinearImpulse(impulse, cueBall.getWorldCenter(), true);
    }

    // ---- UI sync ----
    private void startSync() {
        stopSync();
        syncTask = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                pushPositionsToUI();
            }
        }, 0f, SYNC_PERIOD);
    }

    private void stopSync() {
        if (syncTask != null) { syncTask.cancel(); syncTask = null; }
    }

    private void pushPositionsToUI() {
        if (!uiShown || display == null || cueBall == null) return;

        // Normalize from world coords (centered at 0,0) to [0..1] UI coordinates.
        // Our table spans [-W/2..+W/2] x [-H/2..+H/2]
        Vector2 cb = cueBall.getPosition();
        Vector2 cbNorm = toNorm(cb);

        Array<Vector2> arr = new Array<>(objectBalls.size());
        for (Body b : objectBalls) arr.add(toNorm(b.getPosition()));

        display.setCueBall(cbNorm);
        display.setBalls(arr.toArray(Vector2.class));
    }

    private Vector2 toNorm(Vector2 worldPos) {
        float nx = (worldPos.x + TABLE_W / 2f) / TABLE_W;
        float ny = (worldPos.y + TABLE_H / 2f) / TABLE_H;
        return new Vector2(MathUtils.clamp(nx, 0f, 1f), MathUtils.clamp(ny, 0f, 1f));
    }

    // ---- Public access like your other games ----
    public Entity getGameEntity() { return gameEntity; }
}