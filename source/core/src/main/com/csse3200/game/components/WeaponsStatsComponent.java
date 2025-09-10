package com.csse3200.game.components;

import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store information related to combat such as health, attack, etc. Any entities
 * which engage it combat should have an instance of this class registered. This class can be
 * extended for more specific combat needs.
 */
public class WeaponsStatsComponent extends Component {

    private static final Logger logger = LoggerFactory.getLogger(WeaponsStatsComponent.class);

    private static final int DEFAULT_COOLDOWN = 0;
    private static final boolean DEFAULT_DISABLE_DAMAGE = false;

    /** Base attack damage used when this component attacks another. Non-negative */
    private int baseAttack;
    private float coolDown;
    private boolean disableDamage;
    private int upgradeStage = 1;
    private int maxUpgradeStage = 4;

    /**
     * Construct a Weapons Stats Component (Attack System)
     *
     * @param config attack damage (must be {@code >= 0})
     */
    public WeaponsStatsComponent(WeaponConfig config) {
        setBaseAttack(config.damage);
        setDisableDamage(DEFAULT_DISABLE_DAMAGE);
        setCoolDown(DEFAULT_COOLDOWN);
    }

    public WeaponsStatsComponent(int baseAttack) {
        setBaseAttack(baseAttack);
        setDisableDamage(DEFAULT_DISABLE_DAMAGE);
        setCoolDown(DEFAULT_COOLDOWN);
    }

    /**
     * Set the entity's hit cooldown (seconds)
     *
     * @param coolDown coolDown
     */
    public void setCoolDown(float coolDown) {
        this.coolDown = Math.max(0, coolDown);
    }

    /**
     * gets the entity's cooldown between attacks (seconds).
     *
     */

    public float getCoolDown() {
        return this.coolDown;
    }

    /**
     * Returns the entity's base attack damage.
     *
     * @return base attack damage
     */
    public int getBaseAttack() {
        return baseAttack;
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

    private boolean canAttack() {
        return (!disableDamage);
    }

    public int getUpgradeStage() {
        return this.upgradeStage;
    }

    public void upgrade() {
        System.out.println(this.baseAttack);
        if (this.upgradeStage < this.maxUpgradeStage) {
            this.upgradeStage++;
            this.baseAttack *= 2;
        } else {
            System.out.println("Item already fully upgraded");
        }
        System.out.println(this.baseAttack);
    }

    /**
     * Sets whether the player can receive any damage. True means no damage received
     * and false means damage can be received.
     *
     * @param status Status of whether entity can be damaged
     */
    public void setDisableDamage(boolean status) {
        this.disableDamage = status;
    }
}
