package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Makes Vroomba behave like a suicide bomber:
 * - When within triggerRadius of the player, start a short fuse.
 * - On detonation, if the player is within damageRadius, apply damage.
 * - Then kill self to trigger explosion particles via DeathParticleSpawnerComponent.
 */
public class VroombaSuicideComponent extends Component {
  private final Entity target;
  private final float triggerRadius;
  private final float damageRadius;
  private final int damage;
  private final float fuseSeconds;

  private boolean arming = false;
  private boolean exploded = false;
  private float timer = 0f;

  public VroombaSuicideComponent(Entity target, float triggerRadius, float damageRadius, int damage, float fuseSeconds) {
    this.target = target;
    this.triggerRadius = triggerRadius;
    this.damageRadius = damageRadius;
    this.damage = damage;
    this.fuseSeconds = fuseSeconds;
  }

  @Override
  public void update() {
    if (exploded || target == null || entity == null) return;

    float dist2 = entity.getCenterPosition().dst2(target.getCenterPosition());
    float trigger2 = triggerRadius * triggerRadius;
    if (!arming) {
      if (dist2 <= trigger2) {
        arming = true; // start fuse
        timer = 0f;
        entity.getEvents().trigger("chaseStart"); // optional cue
      } else {
        return;
      }
    }

    // Fuse ticking
    timer += ServiceLocator.getTimeSource().getDeltaTime();
    if (timer >= fuseSeconds) {
      detonate();
    }
  }

  private void detonate() {
    if (exploded) return;
    exploded = true;

    // Deal damage to target if within damage radius
    float dist2 = entity.getCenterPosition().dst2(target.getCenterPosition());
    if (dist2 <= damageRadius * damageRadius) {
      CombatStatsComponent playerStats = target.getComponent(CombatStatsComponent.class);
      if (playerStats != null) {
        playerStats.takeDamage(damage);
      }
    }

    // Kill self to trigger death particles & cleanup
    CombatStatsComponent selfStats = entity.getComponent(CombatStatsComponent.class);
    if (selfStats != null && !selfStats.isDead()) {
      selfStats.setHealth(0);
    } else {
      // Fallback: emit death so DeathParticleSpawner can trigger even without stats
      entity.getEvents().trigger("death");
    }
  }
}

