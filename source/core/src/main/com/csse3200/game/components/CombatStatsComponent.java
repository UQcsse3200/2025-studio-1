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
  private int health;
  private int maxHealth;
  private int baseAttack;
  private int thresholdForBuff = 20;
  private float coolDown;

  public CombatStatsComponent(int health, int baseAttack) {
    setHealth(health);
    setBaseAttack(baseAttack);
    this.coolDown = 0;
    this.maxHealth = health;
  }

  /**
   * Returns true if the entity's has 0 health, otherwise false.
   *
   * @return is player dead
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
   * Sets the entity's health. Health has a minimum bound of 0.
   *
   * @param health health
   */
  public void setHealth(int health) {
    int prevHealth = this.health;
    if (health >= 0) {
      this.health = health;
    } else {
      this.health = 0;
    }

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
    if (maxHealth >= 0) {
      this.maxHealth = health;
    } else {
      this.maxHealth = 0;
    }
    if (entity != null) {
      entity.getEvents().trigger("updateMaxHealth", this.maxHealth);
    }
  }

  /**
   * Return entity's maximum health
   */
  public int getMaxHealth() {
     return maxHealth;
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
   * Allows the entity to be hit by some attacker and deal some damage, if they have waited
   * for the designated time between attacks.
   * @param attacker the entity attacking
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
   * @param damage Damage amount (must >= 0)
   */

  private void applyDamage(int damage) {
    if (damage <= 0 || isDead()) {
        return;
    }
    setHealth(this.health - damage);
  }

  /**
   * Deal direct damage as an integer.
   * <p>
   * This is intended for non-entity sources of damage, such as traps,
   * projectiles, or weapons. At this stage, the weapon's output power
   * (damage) is represented as a simple {@code int}. In future, this
   * can be extended to use a WeaponStatsComponent or DamageInfo object
   * for more complex calculations (crit, resistances, etc.).
   */

  public void hit(int damage) {
    applyDamage(damage);
  }

}
