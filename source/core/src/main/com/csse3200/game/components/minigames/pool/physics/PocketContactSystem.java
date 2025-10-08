package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Detects and handles collisions between pool balls and pocket sensors.
 * <p>
 * The system installs a custom Box2D {@link ContactListener} to listen for
 * pocket–ball collisions, queues them, and processes them safely when the
 * world is not locked. It notifies registered listeners when balls are potted
 * or the cue ball is scratched.
 */
public class PocketContactSystem {

    /**
     * Listener for pocket events.
     * <p>
     * Implementations handle scratch and pot notifications.
     */
    public interface Listener {
        /**
         * Called when the cue ball falls into a pocket.
         *
         * @param pocketIndex index of the pocket where the scratch occurred
         */
        void onScratch(int pocketIndex);

        /**
         * Called when an object ball is potted.
         *
         * @param ballId      ID of the ball that was potted
         * @param pocketIndex index of the pocket where it was potted
         */
        void onPotted(int ballId, int pocketIndex);
    }

    /**
     * Represents a single queued potting event for deferred processing.
     */
    private static final class PotEvent {
        final Body body;
        final int pocket;

        PotEvent(Body b, int p) {
            body = b;
            pocket = p;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(PocketContactSystem.class);
    private final PoolWorld world;
    private final TableConfig cfg;
    private final Deque<PotEvent> queue = new ArrayDeque<>();
    private final Set<Body> dedup = new HashSet<>();
    private Listener listener;
    private boolean installed = false;
    private Body cueRef;
    private Map<Integer, Body> idMapRef;
    private List<Body> objRef;

    /**
     * Constructs a {@code PocketContactSystem}.
     *
     * @param world the {@link PoolWorld} instance used for collision detection
     * @param cfg   the {@link TableConfig} describing table layout
     */
    public PocketContactSystem(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    /**
     * Binds references to the current ball bodies for later lookup.
     *
     * @param cue     the cue ball {@link Body}
     * @param idMap   a map linking ball IDs to their bodies
     * @param objects a list of all object ball bodies
     */
    public void bindBallRefs(Body cue, Map<Integer, Body> idMap, List<Body> objects) {
        this.cueRef = cue;
        this.idMapRef = idMap;
        this.objRef = objects;
    }

    /**
     * Registers a {@link Listener} to receive pocket and scratch events.
     *
     * @param l the listener to register
     */
    public void setListener(Listener l) {
        this.listener = l;
    }

    /**
     * Installs the contact listener into the Box2D world.
     * <p>
     * This method only installs the listener once. It detects when balls
     * enter a pocket sensor and queues corresponding pot events.
     */
    public void install() {
        if (installed) return;

        world.raw().setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact c) {
                Fixture fa = c.getFixtureA(), fb = c.getFixtureB();
                boolean aPocket = fa.isSensor() && (fa.getFilterData().categoryBits & TableBuilder.LAYER_POCKET) != 0;
                boolean bPocket = fb.isSensor() && (fb.getFilterData().categoryBits & TableBuilder.LAYER_POCKET) != 0;

                if (aPocket == bPocket) return; // both pockets or neither

                Fixture pocketFx = aPocket ? fa : fb;
                Fixture ballFx = aPocket ? fb : fa;

                if ((ballFx.getFilterData().categoryBits & TableBuilder.LAYER_BALL) == 0) return;

                int pocketIndex = pocketFx.getUserData() instanceof Integer ? (Integer) pocketFx.getUserData() : -1;
                Body ballBody = ballFx.getBody();

                log.debug("Pocket contact: pocket={}, ballBody={}", pocketIndex, ballBody);

                if (ballBody == cueRef) {
                    queue.addLast(new PotEvent(ballBody, pocketIndex));
                    return;
                }

                if (!dedup.contains(ballBody)) {
                    dedup.add(ballBody);
                    queue.addLast(new PotEvent(ballBody, pocketIndex));
                }
            }

            @Override
            public void endContact(Contact c) {
            }

            @Override
            public void preSolve(Contact c, Manifold m) {
            }

            @Override
            public void postSolve(Contact c, ContactImpulse i) {
            }
        });

        installed = true;
        log.info("Pocket ContactListener installed.");
    }

    /**
     * Processes all queued pocket events once the world is unlocked.
     * <p>
     * Destroys potted balls and notifies the registered listener.
     * The cue ball is deactivated and triggers a scratch event instead.
     */
    public void processDeferred() {
        if (world.isLocked()) return;

        while (!queue.isEmpty()) {
            PotEvent ev = queue.pollFirst();

            // Cue ball scratched
            if (ev.body == cueRef) {
                cueRef.setLinearVelocity(Vector2.Zero);
                cueRef.setAngularVelocity(0f);
                cueRef.setActive(false);
                if (listener != null) listener.onScratch(ev.pocket);
                continue;
            }

            // Object ball potted
            if (idMapRef != null && objRef != null) {
                Integer ballId = null;
                for (Map.Entry<Integer, Body> e : idMapRef.entrySet()) {
                    if (e.getValue() == ev.body) {
                        ballId = e.getKey();
                        break;
                    }
                }

                if (ballId != null) {
                    idMapRef.remove(ballId);
                    objRef.remove(ev.body);
                    world.raw().destroyBody(ev.body);
                    if (listener != null) listener.onPotted(ballId, ev.pocket);
                }
            }

            dedup.remove(ev.body);
        }
    }
}