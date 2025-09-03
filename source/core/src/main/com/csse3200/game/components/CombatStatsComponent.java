package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.enemy.LowHealthAttackBuff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

  private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);

  /** Current health points (HP). Never negative. */
  private int health;

  /** Maximum health points. Caller-controlled; not enforced as an upper bound on {@link #setHealth(int)}. */
  private int maxHealth;

  /** Base attack damage used when this component attacks another. Non-negative */
  private int baseAttack;
  private int thresholdForBuff = 20;
  private float coolDown;

  /**
   * Construct a combat Stats Component (Health + Attack System)
   *
   * @param health     initial health (values {@code < 0} are clamped to {@code 0})
   * @param baseAttack base attack damage (must be {@code >= 0})
   */
  public CombatStatsComponent(int health, int baseAttack) {
    setMaxHealth(health);
    setHealth(health);
    setBaseAttack(baseAttack);
    this.coolDown = 0;
  }

  /**
   * Checks whether this entity is dead.
   * <p>
   * An entity is considered dead if its health is less than or equal to zero.
   *
   * @return {@code true} if the entity has 0 or less health, {@code false} otherwise
   */
  public Boolean isDead() {
    return this.health <= 0;
  }

  /**
   * Returns the entity's health.
   *
   * @return entity's health
   */
  public int getHealth() {
    return this.health;
  }

  /**
   * Sets the entity's health. Health is always clamped between 0 and maxHealth.
   *
   * @param health new health value
   */
  public void setHealth(int health) {
    int prevHealth = this.health;
    this.health = Math.max(0, Math.min(health, this.maxHealth));
    if (entity != null) {
      entity.getEvents().trigger("updateHealth", this.health);

      // Apply attack buff on low health if the entity has that component
      if (this.health <= thresholdForBuff && (!isDead())) {
          if (entity.getComponent(LowHealthAttackBuff.class) != null) {
              entity.getEvents().trigger("buff");
          }
      }

      if (prevHealth > 0 && this.health == 0) {
        entity.getEvents().trigger("death");
      }
    }
  }

    /**
     * Set the entity's hit cooldown (seconds)
     *
     * @param coolDown coolDown
     */
    public void setCoolDown(float coolDown) {
        if (coolDown >= 0) {
            this.coolDown = coolDown;
        } else {
            this.coolDown = 0;
        }
    }

  /**
   * gets the entity's cooldown between attacks (seconds).
   *
   */

  public float getCoolDown() {
    return this.coolDown;
  }


  /**
   * Adds to the player's health. The amount added can be negative.
   *
   * @param health health to add
   */
  public void addHealth(int health) {
    setHealth(this.health + health);
  }

  /**
   * Sets the entity's Max Health. Max Health has a minimum bound of 0.
   *
   * @param maxHealth the maximum health that a entity can have
   */
  public void setMaxHealth(int maxHealth) {
    this.maxHealth = Math.max(maxHealth, 0);
    // If current health is above the new cap, clamp it (and emit updateHealth)
    if (this.health > this.maxHealth) {
      setHealth(this.maxHealth);
    }
    if (entity != null) {
      entity.getEvents().trigger("updateMaxHealth", this.maxHealth);
    }
  }

  /**
   * Sets the entity's maximum health
   *
   * @return the entity's maximum health (never negative)
   */
  public int getMaxHealth() {
     return this.maxHealth;
  }

  /**
   * Returns the entity's base attack damage.
   *
   * @return base attack damage
   */
  public int getBaseAttack() {
    return this.baseAttack;
  }

  /**
   * Sets the entity's attack damage. Attack damage has a minimum bound of 0.
   *
   * @param attack Attack damage
   */
  public void setBaseAttack(int attack) {
    if (attack >= 0) {
      this.baseAttack = attack;
    } else {
      logger.error("Can not set base attack to a negative attack value");
    }
  }

  /**
   * Apply damage to this entity from another attacking entity.
   *
   * <p>If {@code attacker} is {@code null}, an error is logged and the call is a no-op.</p>
   * <p>If this entity is already dead or the attacker's base attack is {@code <= 0}, no damage is applied.</p>
   *
   * <p>Allows the entity to be hit by some attacker and deal damage, provided they have
   * waited for the designated time between attacks.</p>
   *
   * @param attacker the attacking entity providing {@linkplain #getBaseAttack() base attack} damage; may be {@code null}
   * @see #applyDamage(int)
   */
  public void hit(CombatStatsComponent attacker) {
    if (attacker == null) {
        logger.error("hit(attacker) called with null attacker");
        return;
    }
    applyDamage(attacker.getBaseAttack());
  }

  /**
   * Apply damage to this entity.
   *
   * <p>Damage {@code <= 0} is ignored. If the entity is dead, the call is a no-op.</p>
   *
   * @param damage damage amount (must be {@code >= 0} to have any effect)
   * @implSpec Implementations must not reduce health below {@code 0}.
   * @see #setHealth(int)
   */
  private void applyDamage(int damage) {
    if (damage <= 0 || isDead()) {
        return;
    }
    setHealth(this.health - damage);
  }

  /**
   * Deal direct damage as an integer.
   *
   * <p>
   *  Intended for non-entity sources (e.g., traps, projectiles, environmental hazards).
   *  This delegates to {@link #applyDamage(int)} and therefore follows the same validation and dead-entity behavior.
   * </p>
   *
   * @param damage raw damage amount (non-negative to take effect)
   * projectiles, or weapons.
   */
  // Note: At this stage, the weapon's output power (damage) is represented as a simple {@code int}.
  //  In future, this can be extended to use a {@code WeaponStatsComponent} or {@code DamageInfo}
  //  for features like critical hits or resistances.
  public void hit(int damage) {
    applyDamage(damage);
  }
}
