package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.csse3200.game.components.minigames.pool.logic.BasicTwoPlayerRules;
import com.csse3200.game.components.minigames.pool.logic.GameEvents;
import com.csse3200.game.components.minigames.pool.logic.RuleSet;
import com.csse3200.game.components.minigames.pool.logic.RulesEvents;
import com.csse3200.game.components.minigames.pool.physics.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PoolGame {
    private static final Logger logger = LoggerFactory.getLogger(PoolGame.class);
    /**
     * Spawn offsets expressed as fractions of table width for readability.
     */
    private static final float CUE_X_OFFSET_FRACTION = 0.30f; // left of center
    private static final float RACK_X_OFFSET_FRACTION = 0.25f; // right of center
    // Core systems
    private final PhysicsEngine engine;
    private final PoolWorld world;
    private final TableConfig config;
    private final TableBuilder tableBuilder;
    private final BallFactory ballFactory;
    private final PocketContactSystem pockets;
    private final RuleSet rules;
    // UI
    private final Entity gameEntity;
    private final PoolGameDisplay display;
    private com.badlogic.gdx.utils.Timer.Task syncTask;
    private boolean uiVisible = false;

    public PoolGame() {
        World poolBox2d = new World(new Vector2(0, 0), true);
        this.engine = new PhysicsEngine(poolBox2d, ServiceLocator.getTimeSource());
        this.world = new PoolWorld(poolBox2d);
        this.config = TableConfig.builder()
                .tableSize(2.24f, 1.12f)
                .railThickness(0.105f, 0.085f)
                .ballRadius(0.0285f)
                .pocketRadiusScale(2.5f)
                .pocketInsetScaleX(1.0f)
                .pocketInsetScaleY(1.0f)
                .pocketFunnelScale(1.0f)
                .build();

        this.tableBuilder = new TableBuilder(world, config);
        this.ballFactory = new BallFactory(world, config);
        this.pockets = new PocketContactSystem(world, config);
        this.rules = new BasicTwoPlayerRules(config);

        wireRuleEvents();

        // Root UI/interaction entity
        this.gameEntity = initGameEntity();
        this.display = gameEntity.getComponent(PoolGameDisplay.class);

        wireUiEvents();
    }


    private void wireRuleEvents() {
        this.rules.setListener(new RulesEvents() {
            @Override
            public void onTurnChanged(int current, int p1, int p2) {
                gameEntity.getEvents().trigger(GameEvents.TURN, current, p1, p2);
            }

            @Override
            public void onScoreUpdated(int current, int p1, int p2) {
                gameEntity.getEvents().trigger(GameEvents.SCORE, current, p1, p2);
            }

            @Override
            public void onFoul(int foulingPlayer, String reason) {
                gameEntity.getEvents().trigger(GameEvents.FOUL, foulingPlayer, reason);
            }
        });
    }

    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        PoolGameDisplay disp = new PoolGameDisplay();
        game.addComponent(disp);
        game.addComponent(new TextureRenderComponent("images/pool/table.png"));
        game.setInteractable(true);

        disp.setController(new PoolGameDisplay.Controller() {
            @Override
            public void onStart() {
                PoolGame.this.onStart();
            }

            @Override
            public void onShoot(float dx, float dy, float p) {
                rules.onShoot(ballFactory.getCueBody(), dx, dy, p);
            }

            @Override
            public void onReset() {
                PoolGame.this.onStart();
            }

            @Override
            public void onStop() {
                PoolGame.this.onStop();
            }

            /**
             * Clamp the guide length (in pixels) based on world raycast against table/balls.
             * Converts px → meters using config & UI ball px radius, does a world-space ray,
             * then converts the clamped length back to pixels.
             */
            @Override
            public float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx) {
                // px↔meters conversion using config + the UI’s current ball pixel radius
                float pxPerMeter = ballPx / config.ballR();
                float desiredLenMeters = desiredLenPx / pxPerMeter;

                Vector2 cuePosWorld = TableSpace.fromNorm(cuePosNorm, config);
                Vector2 dirWorld = new Vector2(dirNorm).nor();

                final float[] minHitFraction = {1f};
                RayCastCallback cb = (fixture, point, normal, fraction) -> {
                    if (fixture.getBody() == ballFactory.getCueBody()) return -1f; // ignore self
                    if (fraction < minHitFraction[0]) minHitFraction[0] = fraction;
                    return fraction;
                };

                Vector2 end = new Vector2(cuePosWorld).mulAdd(dirWorld, desiredLenMeters);
                world.raw().rayCast(cb, cuePosWorld, end);

                float clampedMeters = desiredLenMeters * minHitFraction[0];
                return clampedMeters * pxPerMeter; // back to pixels for the UI
            }

            @Override
            public boolean isShotActive() {
                return rules.isShotActive();
            }

        });

        return game;
    }

    private void wireUiEvents() {
        gameEntity.getEvents().addListener(GameEvents.INTERACT, this::onInteract);
        gameEntity.getEvents().addListener(GameEvents.START, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.RESET, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.STOP, this::onStop);

        // bridge physics → rules → UI
        pockets.setListener(new PocketContactSystem.Listener() {
            @Override
            public void onScratch(int pocketIndex) {
                rules.onScratch(pocketIndex);
                pushPositionsToUI();
            }

            @Override
            public void onPotted(int ballId, int pocketIndex) {
                rules.onBallPotted(ballId, pocketIndex);
                pushPositionsToUI();
            }
        });
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    private void onInteract() {
        if (uiVisible) {
            onStop();
            display.hide();
            uiVisible = false;
            return;
        }
        beginSession(true);
    }

    private void onStart() {
        beginSession(false);
    }

    private void onStop() {
        stopSync();
    }

    /**
     * Common flow for both interact (show UI) and start/reset (optionally show UI).
     */
    private void beginSession(boolean showUi) {
        ensureBuilt();
        resetRack();
        startSync();
        pushPositionsToUI();
        if (showUi) {
            display.show();
            uiVisible = true;
        }
    }

    private void ensureBuilt() {
        if (!tableBuilder.isBuilt()) {
            tableBuilder.buildRails();
            tableBuilder.buildPocketSensors();
            pockets.install();
        }
        if (!ballFactory.isBuilt()) {
            ballFactory.spawnCue(new Vector2(-config.tableW() * CUE_X_OFFSET_FRACTION, 0f));
            ballFactory.spawnRackTriangle(new Vector2(config.tableW() * RACK_X_OFFSET_FRACTION, 0f));
            pockets.bindBallRefs(
                    ballFactory.getCueBody(),
                    ballFactory.getIdMap(),
                    ballFactory.getObjectBodies()
            );
        }
        ServiceLocator.getRenderService().getDebug().renderPhysicsWorld(world.raw());
    }

    private void resetRack() {
        rules.onNewRack(ballFactory);
        pushPositionsToUI();
    }

    // ------------------------------------------------------------------
    // UI sync
    // ------------------------------------------------------------------
    private void startSync() {
        stopSync();
        syncTask = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                world.raw().step(GameTuning.SYNC_PERIOD, 6, 2);

                pockets.processDeferred();
                pushPositionsToUI();
                // Tick-based turn update; rules decide when balls are settled.
                rules.updateTurn();
            }
        }, 0f, GameTuning.SYNC_PERIOD);
    }

    private void stopSync() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    private void pushPositionsToUI() {
        if (!uiVisible || display == null || ballFactory.getCueBody() == null) return;

        Vector2 cue = TableSpace.toNorm(ballFactory.getCueBody().getPosition(), config);
        List<Vector2> objs = TableSpace.toNorm(ballFactory.getObjectBallPositions(), config);

        display.setCueBall(cue);
        // Avoid extra libGDX Array allocation: convert List -> array directly
        display.setBalls(objs.toArray(new Vector2[0]));
    }

    public Entity getGameEntity() {
        return gameEntity;
    }
}