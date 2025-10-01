package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Physics-backed pool mini-game:
 * <ul>
 *   <li>Rails as static chain fixtures.</li>
 *   <li>Cue + 15 object balls as dynamic circles.</li>
 *   <li>Six pockets checked by distance threshold.</li>
 *   <li>Event-driven ball movement and UI synchronization.</li>
 * </ul>
 */
public class PoolGame {
    // Table geometry (world units, meters)
    private static final float TABLE_W = 2.24f;
    private static final float TABLE_H = 1.12f;
    private static final float RAIL_THICK = 0.05f;

    // Ball physical properties
    private static final float BALL_R = 0.0285f;
    private static final float BALL_DENSITY = 0.8f;
    private static final float BALL_FRICTION = 0.2f;
    private static final float BALL_RESTITUTION = 0.95f;
    private static final float LIN_DAMP = 0.90f; // reserved for future tuning
    private static final float ANG_DAMP = 0.40f; // reserved for future tuning

    // Pocket parameters
    private static final float POCKET_R = BALL_R * 1.6f;      // capture radius
    private static final float POCKET_INSET = RAIL_THICK * 0.7f;

    // Shooting and UI sync
    private static final float MAX_IMPULSE = 2.8f;
    private static final float SYNC_PERIOD = 0.033f;          // ~30 Hz

    // Collision layers (adapt to project layers as needed)
    private static final short LAYER_BALL = com.csse3200.game.physics.PhysicsLayer.NPC;
    private static final short LAYER_RAIL = com.csse3200.game.physics.PhysicsLayer.OBSTACLE;
    private static final short MASK_BALL  = (short) (LAYER_BALL | LAYER_RAIL);
    private static final short MASK_RAIL  = LAYER_BALL;

    // Core systems
    private final PhysicsEngine engine;
    private final World world;

    // Root UI/interaction entity
    private final Entity gameEntity;
    private final PoolGameDisplay display;

    // World entities and bodies
    private Entity railsEntity;
    private Entity cueBallEntity;
    private final List<Entity> objectBallEntities = new ArrayList<>();

    private Body cueBallBody;
    private final List<Body> objectBallBodies = new ArrayList<>();

    // Ball id → body (cue = 0, objects = 1..15)
    private final Map<Integer, Body> idToBody = new HashMap<>();

    // Pocket centers in world coordinates: TL, TM, TR, BR, BM, BL
    private final Vector2[] pockets = new Vector2[6];

    private boolean uiShown = false;
    private Timer.Task syncTask;

    private static final Logger logger = LoggerFactory.getLogger(PoolGame.class);

    public PoolGame() {
        this(ServiceLocator.getPhysicsService().getPhysics());
    }

    public PoolGame(PhysicsEngine engine) {
        this.engine = engine;
        this.world  = engine.getWorld();
        world.setGravity(new Vector2(0f, 0f));

        gameEntity = initGameEntity();
        display = gameEntity.getComponent(PoolGameDisplay.class);

        // UI / control events
        gameEntity.getEvents().addListener("interact", this::onInteract);
        gameEntity.getEvents().addListener("pool:start", this::onStart);
        gameEntity.getEvents().addListener("pool:reset", this::onStart);
        gameEntity.getEvents().addListener("pool:stop", this::onStop);
        logger.info("Pool mini-game listeners registered.");

        // Shooting: dirX, dirY (normalized), power [0..1]
        gameEntity.getEvents().addListener("pool:shoot", this::onShoot);

        // Ball positioning by id (0 = cue, 1..15 = rack order)
        gameEntity.getEvents().addListener("pool:setBall", this::moveBallNorm);

        // Convenience: move cue only
        gameEntity.getEvents().addListener("pool:moveCue",
                (Float nx, Float ny) -> moveBallNorm(0, nx, ny));
    }

    /** Creates the root station/entity and wires the display controller. */
    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();

        PoolGameDisplay disp = new PoolGameDisplay();
        game.addComponent(disp);
        game.addComponent(new TextureRenderComponent("images/pool/table.png"));
        game.setInteractable(true);

        disp.setController(new PoolGameDisplay.Controller() {
            @Override public void onStart() { PoolGame.this.onStart(); }
            @Override public void onShoot(float dx, float dy, float p) { PoolGame.this.onShoot(dx, dy, p); }
            @Override public void onReset() { PoolGame.this.onStart(); }
            @Override public void onStop()  { PoolGame.this.onStop(); }
        });

        return game;
    }

    // ------------------------------------------------------------------------
    // UI Handlers
    // ------------------------------------------------------------------------

    private void onInteract() {
        if (uiShown) {
            onStop();
            display.hide();
            uiShown = false;
            return;
        }
        buildTableIfNeeded();
        rackBallsSimple();
        startSync();
        display.show();
        uiShown = true;
    }

    private void onStart() {
        buildTableIfNeeded();
        rackBallsSimple();
        startSync();
        pushPositionsToUI();
    }

    private void onStop() {
        stopSync();
    }

    // ------------------------------------------------------------------------
    // World / Entities
    // ------------------------------------------------------------------------

    /** Lazily creates rails, cue ball, and object balls. */
    private void buildTableIfNeeded() {
        if (railsEntity != null && cueBallEntity != null && !objectBallEntities.isEmpty()) return;

        if (railsEntity == null) {
            railsEntity = createRailsEntity();
            railsEntity.create();
            bakePocketCenters();
        }

        if (cueBallEntity == null) {
            cueBallEntity = createBallEntity(new Vector2(-TABLE_W * 0.30f, 0f));
            cueBallEntity.create();
            cueBallBody = cueBallEntity.getComponent(PhysicsComponent.class).getBody();
            idToBody.put(0, cueBallBody);
        }

        if (objectBallEntities.isEmpty()) {
            Vector2 rackOrigin = new Vector2(TABLE_W * 0.25f, 0f);
            float gap = BALL_R * 2.02f;
            int spawned = 0;

            for (int row = 0; row < 5; row++) {
                float x = rackOrigin.x + row * (gap * 0.87f);
                float yStart = -row * BALL_R;

                for (int i = 0; i <= row; i++) {
                    float y = yStart + i * (2f * BALL_R);
                    Entity ballE = createBallEntity(new Vector2(x, y));
                    ballE.create();
                    objectBallEntities.add(ballE);

                    Body b = ballE.getComponent(PhysicsComponent.class).getBody();
                    objectBallBodies.add(b);
                    idToBody.put(objectBallBodies.size(), b); // 1..15 in rack order

                    if (++spawned == 15) break;
                }
                if (spawned == 15) break;
            }
        }
    }

    /** Creates static rails as a looped chain with restitution for lively bounces. */
    private Entity createRailsEntity() {
        PhysicsComponent phys = new PhysicsComponent().setBodyType(BodyDef.BodyType.StaticBody);

        float halfW = TABLE_W / 2f, halfH = TABLE_H / 2f, inset = RAIL_THICK;
        Vector2[] verts = new Vector2[] {
                new Vector2(-halfW + inset, -halfH + inset),
                new Vector2( halfW - inset, -halfH + inset),
                new Vector2( halfW - inset,  halfH - inset),
                new Vector2(-halfW + inset,  halfH - inset)
        };
        ChainShape loop = new ChainShape();
        loop.createLoop(verts);

        ColliderComponent col = new ColliderComponent()
                .setShape(loop)
                .setFriction(0f)
                .setRestitution(0.98f)
                .setFilter(LAYER_RAIL, MASK_RAIL);

        return new Entity().addComponent(phys).addComponent(col);
    }

    /** Creates a dynamic ball at a world position. */
    private Entity createBallEntity(Vector2 pos) {
        PhysicsComponent phys = new PhysicsComponent()
                .setBodyType(BodyDef.BodyType.DynamicBody);

        CircleShape circle = new CircleShape();
        circle.setRadius(BALL_R);

        ColliderComponent col = new ColliderComponent()
                .setShape(circle)
                .setDensity(BALL_DENSITY)
                .setFriction(BALL_FRICTION)
                .setRestitution(BALL_RESTITUTION)
                .setFilter(LAYER_BALL, MASK_BALL);

        Entity e = new Entity().addComponent(phys).addComponent(col);
        e.addComponent(new com.csse3200.game.components.Component() {
            @Override public void create() {
                phys.getBody().setTransform(pos, 0f);
                // Slight vertical jitter to prevent perfect overlaps.
                Body b = phys.getBody();
                b.setTransform(b.getPosition().x, b.getPosition().y + MathUtils.random(-0.001f, 0.001f), 0f);
            }
        });
        return e;
    }

    // ------------------------------------------------------------------------
    // Rack & Shoot
    // ------------------------------------------------------------------------

    /** Simple triangle rack and cue placement. Resets velocities. */
    private void rackBallsSimple() {
        cueBallBody.setTransform(new Vector2(-TABLE_W * 0.30f, 0f), 0f);
        cueBallBody.setLinearVelocity(Vector2.Zero);
        cueBallBody.setAngularVelocity(0f);

        logger.debug("Cue ball at {}", cueBallBody.getPosition());

        Vector2 rackOrigin = new Vector2(TABLE_W * 0.25f, 0f);
        float gap = BALL_R * 2.02f;

        int idx = 0;
        for (Body b : objectBallBodies) {
            int row = rowOf(idx);
            int pos = posInRow(idx);
            float x = rackOrigin.x + row * (gap * 0.87f);
            float y = -row * BALL_R + pos * (2f * BALL_R);
            b.setTransform(new Vector2(x, y), 0f);
            b.setLinearVelocity(Vector2.Zero);
            b.setAngularVelocity(0f);
            idx++;
        }
        pushPositionsToUI();
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

    /** Applies an impulse to the cue ball in the given direction and power. */
    private void onShoot(float dirX, float dirY, float power) {
        if (cueBallBody == null) return;

        Vector2 dir = new Vector2(dirX, dirY);
        if (dir.isZero(1e-4f)) return;

        dir.nor();
        float p = MathUtils.clamp(power, 0f, 1f);
        Vector2 impulse = dir.scl(MAX_IMPULSE * p * cueBallBody.getMass());
        cueBallBody.applyLinearImpulse(impulse, cueBallBody.getWorldCenter(), true);
    }

    // ------------------------------------------------------------------------
    // Ball Movement (Events)
    // ------------------------------------------------------------------------

    /** Move a ball by id using normalised [0,1] table coordinates. */
    private void moveBallNorm(int id, float nx, float ny) {
        Body b = idToBody.get(id);
        if (b == null) return;

        Vector2 wp = fromNorm(nx, ny);
        b.setTransform(wp, 0f);
        b.setLinearVelocity(Vector2.Zero);
        b.setAngularVelocity(0f);
        pushPositionsToUI();
    }

    // ------------------------------------------------------------------------
    // Pockets
    // ------------------------------------------------------------------------

    /** Precomputes pocket centers. */
    private void bakePocketCenters() {
        float hx = TABLE_W / 2f, hy = TABLE_H / 2f;
        float ix = hx - POCKET_INSET, iy = hy - POCKET_INSET;
        pockets[0] = new Vector2(-ix,  iy); // TL
        pockets[1] = new Vector2( 0f,  iy); // TM
        pockets[2] = new Vector2( ix,  iy); // TR
        pockets[3] = new Vector2( ix, -iy); // BR
        pockets[4] = new Vector2( 0f, -iy); // BM
        pockets[5] = new Vector2(-ix, -iy); // BL
    }

    /** Checks pocket captures, respawns cue on scratch, removes potted object balls. */
    private void checkPocketsAndCull() {
        // Cue ball scratch → respawn in the kitchen
        if (cueBallBody != null && isInAnyPocket(cueBallBody.getPosition())) {
            cueBallBody.setTransform(new Vector2(-TABLE_W * 0.30f, 0f), 0f);
            cueBallBody.setLinearVelocity(Vector2.Zero);
            cueBallBody.setAngularVelocity(0f);
        }

        // Remove any object balls that were pocketed
        for (int i = objectBallBodies.size() - 1; i >= 0; i--) {
            Body b = objectBallBodies.get(i);
            if (isInAnyPocket(b.getPosition())) {
                world.destroyBody(b);
                objectBallBodies.remove(i);

                Integer hitId = null;
                for (Map.Entry<Integer, Body> e : idToBody.entrySet()) {
                    if (e.getValue() == b) { hitId = e.getKey(); break; }
                }
                if (hitId != null) idToBody.remove(hitId);
            }
        }
    }

    private boolean isInAnyPocket(Vector2 p) {
        float r2 = POCKET_R * POCKET_R;
        for (Vector2 c : pockets) {
            if (p.dst2(c) <= r2) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // UI Sync
    // ------------------------------------------------------------------------

    /** Begins periodic syncing of physics positions to the UI. */
    private void startSync() {
        stopSync();
        syncTask = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                checkPocketsAndCull();
                pushPositionsToUI();
            }
        }, 0f, SYNC_PERIOD);
    }

    /** Stops periodic syncing. */
    private void stopSync() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    /** Pushes current ball positions (normalised) to the display. */
    private void pushPositionsToUI() {
        if (!uiShown || display == null || cueBallBody == null) return;

        Vector2 cb = toNorm(cueBallBody.getPosition());
        Array<Vector2> arr = new Array<>(objectBallBodies.size());
        for (Body b : objectBallBodies) arr.add(toNorm(b.getPosition()));

        display.setCueBall(cb);
        display.setBalls(arr.toArray(Vector2.class));
    }

    // ------------------------------------------------------------------------
    // World <-> UI Coordinate Mapping
    // ------------------------------------------------------------------------

    /** World → normalised [0,1] on felt area. */
    private Vector2 toNorm(Vector2 wp) {
        float nx = (wp.x + TABLE_W / 2f) / TABLE_W;
        float ny = (wp.y + TABLE_H / 2f) / TABLE_H;
        return new Vector2(MathUtils.clamp(nx, 0f, 1f), MathUtils.clamp(ny, 0f, 1f));
    }

    /** Normalised [0,1] → world on felt area. */
    private Vector2 fromNorm(float nx, float ny) {
        float x = MathUtils.clamp(nx, 0f, 1f) * TABLE_W - TABLE_W / 2f;
        float y = MathUtils.clamp(ny, 0f, 1f) * TABLE_H - TABLE_H / 2f;
        return new Vector2(x, y);
    }

    public Entity getGameEntity() { return gameEntity; }
}