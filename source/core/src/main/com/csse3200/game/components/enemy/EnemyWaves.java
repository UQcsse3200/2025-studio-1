package com.csse3200.game.components.enemy;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles spawning enemies over multiple waves. Simplified: a fixed number of waves per trigger.
 */
public class EnemyWaves extends Component {
    private static final Logger logger = LoggerFactory.getLogger(EnemyWaves.class);

    private final Entity player;
    private final GameArea gameArea;

    private int maxWaves;      // total waves per session
    private int waveNumber = 0;      // waves spawned so far
    private float scalingFactor = 1f; // difficulty scaling
    private final int baseEnemies = 3;

    private static final long WAVE_DELAY_MS = 5000; // delay between waves after all enemies dead
    private static final float TICK_SEC = 0.1f;     // poll cadence

    private Timer.Task task;
    private long waveEndTime = 0; // timestamp when last enemy of a wave died

    public EnemyWaves(int maxWaves, GameArea area, Entity player) {
        this.maxWaves = Math.max(1, maxWaves);
        this.gameArea = area;
        this.player = player;
    }

    public EnemyWaves(GameArea area, Entity player) {
        this(1, area, player); // default to 1 wave if not specified
    }

    /**
     * Start or resume wave logic. If all waves previously finished, this restarts from wave 0.
     */
    public void startWave() {
        if (allWavesFinished()) {
            logger.info("EnemyWaves: restarting wave session");
            resetSession();
        }
        if (task != null) {
            task.cancel();
        }
        task = Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                tick();
            }
        }, TICK_SEC, TICK_SEC);
        // If no waves spawned yet, spawn first immediately
        if (waveNumber == 0) {
            spawnWave();
        }
    }

    /**
     * Resets the wave parameters to their default values.
     */
    private void resetSession() {
        waveNumber = 0;
        scalingFactor = 1f;
        waveEndTime = 0;
    }

    /**
     * Spawns the enemies on the map based on the member variables of {@link EnemyWaves} object.
     */
    private void spawnWave() {
        if (waveNumber >= maxWaves) {
            logger.info("EnemyWaves: all {} waves already spawned", maxWaves);
            return;
        }
        float baseScale = 1f;
        try {
            baseScale = gameArea.getBaseDifficultyScale();
        } catch (Exception e) {
            // fallback to 1 if area not ready
        }
        float effectiveScale = scalingFactor * baseScale;
        logger.info("EnemyWaves: spawning wave {} of {} (waveScale={}, baseScale={}, effective={})", waveNumber + 1, maxWaves, scalingFactor, baseScale, effectiveScale);

        // Change gameArea.getRoomNumber() to any number between 2 and 7 to get different enemies.
        gameArea.spawnEnemies(gameArea.getRoomNumber(), baseEnemies, effectiveScale, player);

        waveNumber++;
        scalingFactor += 0.25f; // incremental per-wave multiplier
    }

    /**
     * Checks if a wave is completed at every tick. The tick interval being 0.1f.
     */
    void tick() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) return;
        boolean anyAlive = false;
        for (Entity e : es.getEntities()) {
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
            if (stats == null || !isEnemy(e)) continue;
            if (stats.getHealth() > 0) {
                anyAlive = true;
                break;
            }
        }

        long now = System.currentTimeMillis();
        if (!anyAlive) {
            // start delay gate once per cleared wave while waves remain
            if (waveNumber < maxWaves) {
                if (waveEndTime == 0) {
                    waveEndTime = now;
                    logger.info("EnemyWaves: wave {} cleared; next in {} ms", waveNumber, WAVE_DELAY_MS);
                } else if (now - waveEndTime >= WAVE_DELAY_MS) {
                    waveEndTime = 0;
                    spawnWave();
                }
            } else {
                // finished all waves
                if (task != null) {
                    task.cancel();
                    task = null; // stop ticking until restart
                    logger.info("EnemyWaves: session complete ({} waves)", maxWaves);
                }
            }
        } else {
            waveEndTime = 0; // enemies alive -> reset delay gate
        }
    }

    /**
     * Checks if the given Entity is an enemy.
     *
     * @param entity The entity that needs to be checked.
     * @return True if the entity is an enemy, false otherwise.
     */
    private boolean isEnemy(Entity entity) {
        return entity.getComponent(GhostAnimationController.class) != null;
    }

    /**
     * Checks if all the waves in the current room have finished.
     *
     * @return True if all waves have finished, false otherwise.
     */
    public boolean allWavesFinished() {
        return waveNumber >= maxWaves;
    }

    /**
     * Checks if the current wave has finished.
     *
     * @return True if the current wave has finished, false otherwise.
     */
    public boolean isCurrentWaveFinished() {
        return waveEndTime > 0;
    }

    /**
     * Returns the maximum number of waves in the current room.
     *
     * @return The maxWaves as an int.
     */
    public int getMaxWaves() {
        return maxWaves;
    }

    /**
     * Returns the scaling factor of the enemies in the next wave.
     *
     * @return The scaling factor as a float.
     */
    public float getScalingFactor() {
        return scalingFactor;
    }

    /**
     * Returns the wave number of the next wave in the current room.
     *
     * @return The wave number as an int.
     */
    public int getWaveNumber() {
        return waveNumber;
    }

    /**
     * Returns the time stamp of when the previous wave ended in the current room if no enemies are alive
     * otherwise returns 0.
     *
     * @return The wave end time in milliseconds as a long.
     */
    public long getWaveEndTime() {
        return waveEndTime;
    }

    /**
     * Sets the maximum number of waves in the current room.
     *
     * @param maxWaves The maximum number of waves as an int.
     */
    public void setMaxWaves(int maxWaves) {
        this.maxWaves = maxWaves;
    }

    /**
     * Sets the scaling factor of the enemies in the next wave.
     *
     * @param scalingFactor The scaling factor as a float.
     */
    public void setScalingFactor(float scalingFactor) {
        this.scalingFactor = scalingFactor;
    }

    /**
     * Sets the wave number of the next wave in the current room.
     *
     * @param waveNumber The wave number as an int.
     */
    public void setWaveNumber(int waveNumber) {
        this.waveNumber = waveNumber;
    }

    /**
     * Sets the time stamp of when the previous wave ended in the current room
     *
     * @param waveEndTime The wave end time in milliseconds as a long .
     */
    public void setWaveEndTime(long waveEndTime) {
        this.waveEndTime = waveEndTime;
    }
}