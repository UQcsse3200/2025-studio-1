
package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.minigames.pool.physics.*;
import com.csse3200.game.components.minigames.pool.logic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PoolGame {
    private static final Logger logger = LoggerFactory.getLogger(PoolGame.class);

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

    private Timer.Task syncTask;
    private boolean uiShown = false;

    public PoolGame() {
        this(ServiceLocator.getPhysicsService().getPhysics());
    }

    public PoolGame(PhysicsEngine engine) {
        this.engine = engine;
        this.world = new PoolWorld(engine.getWorld());

        this.config = TableConfig.builder()
                .tableSize(2.24f, 1.12f)
                .railThickness(0.105f, 0.085f)                 // X (left/right), Y (top/bottom)
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

        this.rules.setListener(new RulesEvents() {
            @Override public void onTurnChanged(int current, int p1, int p2) {
                gameEntity.getEvents().trigger("pool:turn", current, p1, p2);
            }
            @Override public void onScoreUpdated(int current, int p1, int p2) {
                gameEntity.getEvents().trigger("pool:score", current, p1, p2);
            }
            @Override public void onFoul(int foulingPlayer, String reason) {
                gameEntity.getEvents().trigger("pool:foul", foulingPlayer, reason);
            }
        });

        // Root UI/interaction entity
        this.gameEntity = initGameEntity();
        this.display = gameEntity.getComponent(PoolGameDisplay.class);

        wireUiEvents();
        gameEntity.getEvents().addListener("pool:turn",
                (Integer current, Integer p1, Integer p2) ->
                        logger.info("EVENT pool:turn -> P{}  score {}-{}", current, p1, p2));
        gameEntity.getEvents().addListener("pool:score",
                (Integer current, Integer p1, Integer p2) ->
                        logger.info("EVENT pool:score -> P{}  score {}-{}", current, p1, p2));
        gameEntity.getEvents().addListener("pool:foul",
                (Integer player, String reason) ->
                        logger.info("EVENT pool:foul -> P{}  {}", player, reason));
    }

    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        PoolGameDisplay disp = new PoolGameDisplay();
        game.addComponent(disp);
        game.addComponent(new TextureRenderComponent("images/pool/table.png"));
        game.setInteractable(true);

        disp.setController(new PoolGameDisplay.Controller() {
            @Override public void onStart() { PoolGame.this.onStart(); }
            @Override public void onShoot(float dx, float dy, float p) { rules.onShoot(ballFactory.getCueBody(), dx, dy, p); }
            @Override public void onReset() { PoolGame.this.onStart(); }
            @Override public void onStop()  { PoolGame.this.onStop(); }
            @Override public float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx) {
                // px↔meters conversion using config + the UI’s current ball pixel radius
                float pxPerMeter = ballPx / config.ballR();
                float desiredLenMeters = desiredLenPx / pxPerMeter;

                Vector2 cuePosWorld = TableSpace.toNorm(cuePosNorm, config);
                Vector2 dirWorld = new Vector2(dirNorm).nor();

                final float[] frac = {1f};
                RayCastCallback cb = (fixture, point, normal, f) -> {
                    if (fixture.getBody() == ballFactory.getCueBody()) return -1f; // ignore self
                    if (f < frac[0]) frac[0] = f;
                    return f;
                };

                Vector2 end = new Vector2(cuePosWorld).mulAdd(dirWorld, desiredLenMeters);
                world.raw().rayCast(cb, cuePosWorld, end);

                float clampedMeters = desiredLenMeters * frac[0];
                return clampedMeters * pxPerMeter; // back to pixels for the UI
            }
        });

        return game;
    }

    private void wireUiEvents() {
        gameEntity.getEvents().addListener("interact", this::onInteract);
        gameEntity.getEvents().addListener(GameEvents.START, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.RESET, this::onStart);
        gameEntity.getEvents().addListener(GameEvents.STOP,  this::onStop);

        // bridge physics → rules → UI
        pockets.setListener(new PocketContactSystem.Listener() {
            @Override public void onScratch(int pocketIndex) {
                rules.onScratch(pocketIndex);
                pushPositionsToUI();
            }
            @Override public void onPotted(int ballId, int pocketIndex) {
                rules.onBallPotted(ballId, pocketIndex);
                pushPositionsToUI();
            }
        });
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------
    private void onInteract() {
        if (uiShown) {
            onStop();
            display.hide();
            uiShown = false;
            return;
        }
        buildIfNeeded();
        setupNewRack();
        startSync();
        display.show();
        uiShown = true;
    }

    private void onStart() {
        buildIfNeeded();
        setupNewRack();
        startSync();
        pushPositionsToUI();
    }

    private void onStop() { stopSync(); }

    private void buildIfNeeded() {
        if (!tableBuilder.isBuilt()) {
            tableBuilder.buildRails();
            tableBuilder.buildPocketSensors();
            pockets.install();
        }
        if (!ballFactory.isBuilt()) {
            ballFactory.spawnCue(new Vector2(-config.tableW() * 0.30f, 0f));
            ballFactory.spawnRackTriangle(new Vector2(config.tableW() * 0.25f, 0f));
            pockets.bindBallRefs(
                    ballFactory.getCueBody(),
                    ballFactory.getIdMap(),
                    ballFactory.getObjectBodies()
            );
        }
    }

    private void setupNewRack() {
        rules.onNewRack(ballFactory);
        pushPositionsToUI();
    }

    // ------------------------------------------------------------------
    // UI sync
    // ------------------------------------------------------------------
    private void startSync() {
        stopSync();
        syncTask = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                pockets.processDeferred();   // safe world mutations
                pushPositionsToUI();
                rules.updateTurn();          // e.g., detect end-of-motion & advance turn
            }
        }, 0f, GameTuning.SYNC_PERIOD);
    }

    private void stopSync() {
        if (syncTask != null) { syncTask.cancel(); syncTask = null; }
    }

    private void pushPositionsToUI() {
        if (!uiShown || display == null || ballFactory.getCueBody() == null) return;
        Vector2 cue = TableSpace.toNorm(ballFactory.getCueBody().getPosition(), config);
        List<Vector2> objs = TableSpace.toNorm(ballFactory.getObjectBallPositions(), config);
        display.setCueBall(cue);
        Array<Vector2> arr = new Array<>(objs.toArray(new Vector2[0]));
        display.setBalls(arr.toArray(Vector2.class));
    }

    public Entity getGameEntity() { return gameEntity; }
}
