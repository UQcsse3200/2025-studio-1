package com.csse3200.game.components;

import com.csse3200.game.components.boss.DamageReductionComponent;
import com.csse3200.game.components.enemy.LowHealthAttackBuffComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class CombatStatsComponent extends Component {

    private static final Logger logger = LoggerFactory.getLogger(CombatStatsComponent.class);

    /**
     * Current health points (HP). Never negative.
     */
    private int health;

    /**
     * Maximum health points. Caller-controlled; not enforced as an upper bound on {@link #setHealth(int)}.
     */
    private int maxHealth;


    private final int thresholdForBuff = 20;

    private boolean healthUpgraded;


    /**
     * Construct a combat Stats Component (Health + Attack System)
     *
     * @param health initial health (values {@code < 0} are clamped to {@code 0})
     */
    public CombatStatsComponent(int health) {
        setMaxHealth(health);
        setHealth(health);
        healthUpgraded = false;
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
        return health;
    }

    public void takeDamage(int damage) {
        damage = reduceIncomingDamage(damage);
        applyDamage(damage);
        entity.getEvents().trigger("damageTaken");
    }

    /**
     * Sets the entity's health. Health is always clamped between 0 and maxHealth.
     *
     * @param health new health value
     */
    public void setHealth(int health) {
        int prevHealth = this.health;
        this.health = Math.clamp(health, 0, this.maxHealth);
        if (entity != null) {
            entity.getEvents().trigger("updateHealth", this.health);

            // Apply attack buff on low health if the entity has that component
            if (this.health <= thresholdForBuff && (!isDead())) {
                if (entity.getComponent(LowHealthAttackBuffComponent.class) != null) {
                    entity.getEvents().trigger("buff");
                }
            }

            if (prevHealth > 0 && this.health == 0) {
                entity.getEvents().trigger("death");
            }
        }
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
     * @param maxHealth the maximum health that an entity can have
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
     * <p>Doubles player maximum health when player interacts with a HealthBench. Only applied once.</p>
     */
    public void upgradeMaxHealth() {
        if (!healthUpgraded) {
            healthUpgraded = true;
            this.setMaxHealth(maxHealth * 2);
            this.setHealth(maxHealth); // regenerate player
        }
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
        if (damage <= 0 || Boolean.TRUE.equals(isDead())) {
            return;
        }
        setHealth(this.health - damage);
    }

    /**
     * pre-processing before health reduction: If the entity has {@link DamageReductionComponent} attached,
     * reduce/avoid incoming damage according to its rules; otherwise return it as is
     *
     * @param damage initial damage value
     * @return Damage value after processing
     */
    private int reduceIncomingDamage(int damage) {
        if (damage <= 0) {
            return damage;
        }

        if (entity == null) {
            return damage;
        }

        DamageReductionComponent dr = entity.getComponent(DamageReductionComponent.class);
        if (dr != null) {
            damage = dr.apply(damage);
        }
        return damage;
    }
}
