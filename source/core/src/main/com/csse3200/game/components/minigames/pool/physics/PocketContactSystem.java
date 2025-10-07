package com.csse3200.game.components.minigames.pool.physics;

import com.badlogic.gdx.physics.box2d.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PocketContactSystem {
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

    public PocketContactSystem(PoolWorld world, TableConfig cfg) {
        this.world = world;
        this.cfg = cfg;
    }

    public void bindBallRefs(Body cue, Map<Integer, Body> idMap, List<Body> objects) {
        this.cueRef = cue;
        this.idMapRef = idMap;
        this.objRef = objects;
    }

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void install() {
        if (installed) return;
        world.raw().setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact c) {
                Fixture fa = c.getFixtureA(), fb = c.getFixtureB();
                boolean aPocket = fa.isSensor() && fa.getFilterData().categoryBits == TableBuilder.LAYER_POCKET;
                boolean bPocket = fb.isSensor() && fb.getFilterData().categoryBits == TableBuilder.LAYER_POCKET;
                if (aPocket == bPocket) return;
                Fixture pocketFx = aPocket ? fa : fb;
                Fixture ballFx = aPocket ? fb : fa;
                if ((ballFx.getFilterData().categoryBits & TableBuilder.LAYER_BALL) == 0) return;
                int pocketIndex = pocketFx.getUserData() instanceof Integer ? (Integer) pocketFx.getUserData() : -1;
                Body ballBody = ballFx.getBody();
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

    public void processDeferred() {
        if (world.isLocked()) return;
        while (!queue.isEmpty()) {
            PotEvent ev = queue.pollFirst();
            if (ev.body == cueRef) {
                if (listener != null) listener.onScratch(ev.pocket);
            } else if (idMapRef != null && objRef != null) {
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

    public interface Listener {
        void onScratch(int pocketIndex);

        void onPotted(int ballId, int pocketIndex);
    }

    private static final class PotEvent {
        final Body body;
        final int pocket;

        PotEvent(Body b, int p) {
            body = b;
            pocket = p;
        }
    }
}
