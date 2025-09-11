package com.csse3200.game.components.enemy;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles spawning enemies over multiple waves. Simplified: a fixed number of waves per trigger.
 */
public class EnemyWaves {
  private static final Logger logger = LoggerFactory.getLogger(EnemyWaves.class);

  private final int maxWaves;      // total waves per session
  private final Entity player;
  private final GameArea gameArea;

  private int waveNumber = 0;      // waves spawned so far
  private float scalingFactor = 1f; // difficulty scaling
  private int baseGhosts = 1;
  private int baseVroombas = 2;

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

  /** Start or resume wave logic. If all waves previously finished, this restarts from wave 0. */
  public void startWave() {
    if (isFinished()) {
      logger.info("EnemyWaves: restarting wave session");
      resetSession();
    }
    if (task != null) {
      task.cancel();
    }
    task = Timer.schedule(new Timer.Task() { @Override public void run() { tick(); } }, TICK_SEC, TICK_SEC);
    // If no waves spawned yet, spawn first immediately
    if (waveNumber == 0) {
      spawnWave();
    }
  }

  private void resetSession() {
    waveNumber = 0;
    scalingFactor = 1f;
    waveEndTime = 0;
  }

  private void spawnWave() {
    if (waveNumber >= maxWaves) {
      logger.info("EnemyWaves: all {} waves already spawned", maxWaves);
      return;
    }
    logger.info("EnemyWaves: spawning wave {} of {} (scale={})", waveNumber + 1, maxWaves, scalingFactor);
    // Spawn pattern similar to earlier working version: Ghost + Vroomba combo for visibility
    gameArea.spawnGhostGPT(baseGhosts, scalingFactor, player);
    gameArea.spawnVroomba(baseVroombas, scalingFactor, player);

    waveNumber++;
    scalingFactor += 0.25f;
  }

  private void tick() {
    EntityService es = ServiceLocator.getEntityService();
    if (es == null) return;
    boolean anyAlive = false;
    for (Entity e : es.getEntities()) {
      CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
      if (stats == null || !isEnemy(e)) continue;
      if (stats.getHealth() > 0) { anyAlive = true; break; }
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

  private boolean isEnemy(Entity entity) {
    return entity.getComponent(GhostAnimationController.class) != null;
  }

  public boolean isFinished() { return waveNumber >= maxWaves; }
}