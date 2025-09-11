package com.csse3200.game.components.enemy;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles the spawning of enemy in waves.
 */
public class EnemyWaves {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final int maxWaves;
    private final int roomNumber;
    private final ForestGameArea gameArea;

    private static int maxEnemies = 4;
    private static float scalingFactor = 1f;

    private static final long WAVE_DELAY_MS = 5000;
    private static final float TICK_SEC = 0.1f;

    private Timer.Task task;
    private int waveNumber;
    private long waveEndTime;

    public EnemyWaves(int roomNumber, ForestGameArea gameArea) {
        this.roomNumber = roomNumber;
        this.gameArea = gameArea;
        this.waveNumber = 0;
        this.waveEndTime = 0;
        this.maxWaves = (this.roomNumber > 4) ? 2 : 1;
        maxEnemies = (this.roomNumber > 3) ? (maxEnemies + 1) : 4;
    }

    /**
     * Starts (or restarts) the repeating wave spawning task.
     * Uses libGDX Timer so that ticks are posted onto the main render thread.
     */
    public void startWave() {
        if (task != null) {
            task.cancel();
        }
        task = Timer.schedule(new Timer.Task() {
            @Override public void run() {
                tick();
            }
        }, TICK_SEC, TICK_SEC);
        spawnWave();
    }

    /**
     * Spawns the enemies in a wave on the {@link com.csse3200.game.areas.GameArea}.
     */
    public void spawnWave() {
        if (waveNumber == maxWaves) {
            return;
        }

        logger.debug("Spawning wave {}, with scaling factor {}", waveNumber, scalingFactor);

        if (roomNumber > 3) {
            gameArea.spawnGhostGPT(1, scalingFactor);
            gameArea.spawnVroomba(2, scalingFactor);
            gameArea.spawnTurret(3, scalingFactor);
        } else {
            gameArea.spawnDeepspin(maxEnemies, scalingFactor);
        }
        waveNumber++;
        scalingFactor += 0.25f;
        //Spawning logic
    }

    /**
     * One wave "tick". Called at a fixed cadence by {@link #startWave()}.
     * Checks if all the enemies are dead and if so, waits WAVE_DELAY_MS then calls {@link #spawnWave()}.
     */
    private void tick() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.debug("No EntityService registered; cannot kill enemy");
            return;
        }

        boolean anyEnemyAlive = false;
        for (Entity e : es.getEntities()) {
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
            if (stats == null || !isEnemy(e)) {
                continue;
            }
            if (stats.getHealth() > 0) {
                anyEnemyAlive = true;
                break;
            }
        }

        long now = System.currentTimeMillis();
        if (!anyEnemyAlive) {
            // Start delay once per wave end; don't reset each tick
            if (waveEndTime == 0) {
                waveEndTime = now;
            } else if (now - waveEndTime >= WAVE_DELAY_MS) {
                waveEndTime = 0; // reset timer gate
                spawnWave();
            }
        } else {
            // Enemies still alive; ensure timer gate is cleared
            waveEndTime = 0;
        }
    }

    /**
     * Checks if the entity is an enemy entity.
     * @param entity The entity that is to be checked whether it is an enemy entity or not.
     */
    private boolean isEnemy(Entity entity) {
        return entity.getComponent(GhostAnimationController.class) != null;
    }
}