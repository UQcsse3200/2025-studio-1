package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.csse3200.game.components.minigames.pool.logic.*;
import com.csse3200.game.components.minigames.pool.physics.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Manages the complete pool minigame system, including physics, rules,
 * rendering, and UI synchronization.
 * <p>
 * The {@code PoolGame} coordinates the {@link TableBuilder}, {@link BallFactory},
 * {@link PocketContactSystem}, and {@link RuleSet} to simulate pool gameplay.
 * It also bridges updates between the physics world and the {@link PoolGameDisplay} UI.
 */
public class PoolGame {
    private static final Logger logger = LoggerFactory.getLogger(PoolGame.class);

    /**
     * Spawn offsets expressed as fractions of the table width.
     */
    private static final float CUE_X_OFFSET_FRACTION = 0.30f; // cue ball (left of center)
    private static final float RACK_X_OFFSET_FRACTION = 0.25f; // triangle rack (right of center)

    // Core systems
    private final PhysicsEngine engine;
    private final PoolWorld world;
    private final TableConfig config;
    private final TableBuilder tableBuilder;
    private final BallFactory ballFactory;
    private final PocketContactSystem pockets;
    private final RuleSet rules;

    // UI bridge
    private final Entity gameEntity;
    private final PoolGameDisplay display;
    private com.badlogic.gdx.utils.Timer.Task syncTask;
    private boolean uiVisible = false;

    /**
     * Constructs the pool minigame with all required systems and event wiring.
     * <p>
     * Initializes a dedicated physics world, configures the table, and links
     * game rules and display components.
     */
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
                .pocketFunnelScale(0.9f)
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

    /**
     * Connects rule events (turns, scores, fouls) to game-level event triggers.
     */
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

    /**
     * Initializes the main {@link Entity} representing the pool game UI and logic station.
     *
     * @return the initialized entity containing UI and interaction components
     */
    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        PoolGameDisplay disp = new PoolGameDisplay();
        game.addComponent(disp);
        game.addComponent(new TextureRenderComponent("images/pool/table.png"));
        game.setInteractable(true);

        // Hook display controller to pool logic
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
             * Clamps the guide line length based on raycasting against table rails or balls.
             * <p>
             * Converts from pixels to meters using {@link TableConfig} scale,
             * performs a world-space raycast, then converts back to pixels.
             *
             * @param cuePosNorm    cue ball position (normalized [0..1])
             * @param dirNorm       normalized aim direction
             * @param desiredLenPx  desired guide length in pixels
             * @param ballPx        cue ball radius in pixels
             * @return the clamped guide length in pixels
             */
            @Override
            public float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx) {
                float pxPerMeter = ballPx / config.ballR();
                float desiredLenMeters = desiredLenPx / pxPerMeter;

                Vector2 cuePosWorld = TableSpace.fromNorm(cuePosNorm, config);
                Vector2 dirWorld = new Vector2(dirNorm).nor();

                final float[] minHitFraction = {1f};
                RayCastCallback cb = (fixture, point, normal, fraction) -> {
                    if (fixture.getBody() == ballFactory.getCueBody()) return -1f; // ignore cue
                    if (fraction < minHitFraction[0]) minHitFraction[0] = fraction;
                    return fraction;
                };

                Vector2 end = new Vector2(cuePosWorld).mulAdd(dirWorld, desiredLenMeters);
                world.raw().rayCast(cb, cuePosWorld, end);

                float clampedMeters = desiredLenMeters * minHitFraction[0];
                return clampedMeters * pxPerMeter;
            }

            @Override
            public boolean isShotActive() {
                return rules.isShotActive();
            }
        });

        return game;
    }

    /**
     * Connects UI input events and pocket contact events to game logic.
     */
    private void wireUiEvents() {
        gameEntity.getEvents().addListener(GameEvents.INTERACT, this::onInteract);
        gameEntity.getEvents().addListener(GameEvents.START, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.RESET, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.STOP, this::onStop);

        // Bridge physics → rules → UI
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


    /**
     * Handles interactions from the game world (toggle show/hide of UI).
     */
    private void onInteract() {
        if (uiVisible) {
            onStop();
            display.hide();
            uiVisible = false;
            return;
        }
        beginSession(true);
    }

    /** Starts or resets the game session. */
    private void onStart() {
        beginSession(false);
    }

    /** Stops the running pool game simulation. */
    private void onStop() {
        stopSync();
    }

    /**
     * Common setup for both start and interact flows.
     *
     * @param showUi whether to show the pool UI after initialization
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

    /**
     * Ensures the table and balls are built before the game starts.
     * Builds missing components such as rails, pockets, and the ball rack.
     */
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

    /** Resets the current rack, cue, and turn state. */
    private void resetRack() {
        rules.onNewRack(ballFactory);
        pushPositionsToUI();
    }

    /**
     * Starts the scheduled physics update loop.
     * <p>
     * Steps the Box2D world, processes pocket contacts,
     * and updates both rules and UI state at a fixed rate.
     */
    private void startSync() {
        stopSync();
        syncTask = com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
            @Override
            public void run() {
                world.raw().step(GameTuning.SYNC_PERIOD, 6, 2);
                pockets.processDeferred();
                pushPositionsToUI();
                rules.updateTurn();
            }
        }, 0f, GameTuning.SYNC_PERIOD);
    }

    /** Stops the scheduled simulation update task. */
    private void stopSync() {
        if (syncTask != null) {
            syncTask.cancel();
            syncTask = null;
        }
    }

    /**
     * Updates the UI positions for all balls and the cue ball.
     * Converts world coordinates to normalized table coordinates for display.
     */
    private void pushPositionsToUI() {
        if (!uiVisible || display == null || ballFactory.getCueBody() == null) return;

        Vector2 cueNorm = TableSpace.toNorm(ballFactory.getCueBody().getPosition(), config);
        display.setCueBall(cueNorm);

        Vector2[] byIdWorld = ballFactory.getObjectBallPositionsById();
        Vector2[] byIdNorm  = new Vector2[byIdWorld.length];
        for (int i = 0; i < byIdWorld.length; i++) {
            byIdNorm[i] = (byIdWorld[i] == null) ? null : TableSpace.toNorm(byIdWorld[i], config);
        }
        display.setBalls(byIdNorm);
    }

    /**
     * Returns the entity representing this pool game instance.
     *
     * @return the root {@link Entity} containing the pool game components
     */
    public Entity getGameEntity() {
        return gameEntity;
    }
}