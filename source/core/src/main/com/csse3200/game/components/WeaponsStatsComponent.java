package com.csse3200.game.components;

import com.csse3200.game.entities.configs.weapons.LauncherConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component used to store weapon-related combat statistics such as attack damage,
 * cooldown, upgrade stages, and cheat modifiers.
 * <p>
 * Any entity that uses weapons in combat should have an instance of this component.
 * </p>
 */
public class WeaponsStatsComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(WeaponsStatsComponent.class);

    /**
     * Default attack cooldown (seconds).
     */
    private static final float DEFAULT_COOLDOWN = 0.2f;

    /**
     * Default setting for whether this weapon deals damage.
     */
    private static final boolean DEFAULT_DISABLE_DAMAGE = false;
    /**
     * Maximum number of upgrades allowed.
     */
    private final int maxUpgradeStage = 4;
    /**
     * Base attack damage before multipliers are applied (must be non-negative).
     */
    private static int baseAttack;
    /**
     * Cooldown (in seconds) between attacks.
     */
    private float coolDown;
    /**
     * Path to the projectile's texture if this weapon fires projectiles.
     */
    private String projectileTexturePath;
    /**
     * Current weapon upgrade stage, starting from 1.
     */
    private int upgradeStage = 1;
    /**
     * Global damage multiplier applied to this weapon's base attack.
     * Default is 1.0 (no boost). Cheat codes or power-ups can increase this.
     */
    private float damageMultiplier = 1f;
    /**
     * Flag for if the weapon needs to make rocket bullets
     */
    private boolean rocket = false;

    /**
     * Constructs a {@code WeaponsStatsComponent} from a weapon configuration.
     *
     * @param config the weapon configuration (must specify non-negative damage)
     */
    public WeaponsStatsComponent(WeaponConfig config) {
        setBaseAttack(config.damage);
        setDisableDamage(DEFAULT_DISABLE_DAMAGE);
        setCoolDown(DEFAULT_COOLDOWN);

        if (config instanceof LauncherConfig) {
            rocket = true; //Used to make rocket bullets
        }
    }

    /**
     * Constructs a {@code WeaponsStatsComponent} with a base attack value.
     *
     * @param baseAttack the initial base attack damage (must be {@code >= 0})
     */
    public WeaponsStatsComponent(int baseAttack) {
        setBaseAttack(baseAttack);
        setDisableDamage(DEFAULT_DISABLE_DAMAGE);
        setCoolDown(DEFAULT_COOLDOWN);
    }

    /**
     * @return the flag for if this weapon needs rocket bullets
     */
    public boolean getRocket() {
        return this.rocket;
    }

    /**
     * Sets if this weapon needs rocket bullets
     *
     * @param rocket true for rockets
     */
    public void setRocket(boolean rocket) {
        this.rocket = rocket;
    }

    /**
     * Gets the cooldown time between attacks.
     *
     * @return cooldown in seconds
     */
    public float getCoolDown() {
        return this.coolDown;
    }

    /**
     * Sets the cooldown time between attacks.
     *
     * @param coolDown cooldown in seconds (clamped to minimum 0)
     */
    public void setCoolDown(float coolDown) {
        if (coolDown < 0f) {
            this.coolDown = Math.max(0.2f, coolDown);
        } else {
            this.coolDown = coolDown;
        }
    }

    /**
     * Gets the current effective attack damage for this weapon.
     * <p>
     * The effective damage is calculated as:
     * {@code baseAttack × damageMultiplier}.
     * </p>
     * The value is clamped to the range of an {@code int}.
     *
     * @return effective attack damage
     */
    public int getBaseAttack() {
        long scaled = Math.round(baseAttack * (double) damageMultiplier);
        if (scaled > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (scaled < 0) return 0;
        return (int) scaled;
    }

    /**
     * Sets the base attack damage (before multipliers).
     *
     * @param attack base attack damage (must be {@code >= 0})
     */
    public void setBaseAttack(int attack) {
        if (attack >= 0) {
            baseAttack = attack;
        } else {
            logger.error("Cannot set base attack to a negative value: {}", attack);
        }
    }

    /**
     * Gets the texture path for this weapon's projectile, if applicable.
     *
     * @return projectile texture path, or {@code null} if none
     */
    public String getProjectileTexturePath() {
        return this.projectileTexturePath;
    }

    /**
     * Sets the texture path for this weapon's projectile, if applicable.
     *
     * @param projectileTexturePath path to the projectile texture
     */
    public void setProjectileTexturePath(String projectileTexturePath) {
        this.projectileTexturePath = projectileTexturePath;
    }

    /**
     * Gets the current upgrade stage.
     *
     * @return current upgrade stage (starts at 1)
     */
    public int getUpgradeStage() {
        return this.upgradeStage;
    }

    /**
     * Gets the maximum upgrade stage.
     *
     * @return maximum upgrade stage
     */
    public int getMaxUpgradeStage() {
        return this.maxUpgradeStage;
    }

    /**
     * Checks if this weapon is already fully upgraded.
     *
     * @return {@code true} if the upgrade stage is at maximum, {@code false} otherwise
     */
    public boolean isMaxUpgraded() {
        return this.upgradeStage >= this.maxUpgradeStage;
    }

    /**
     * Upgrades this weapon to the next stage, if not already maxed out.
     * <p>
     * Each upgrade doubles the base attack damage.
     * </p>
     */
    public void upgrade() {
        if (this.upgradeStage < maxUpgradeStage) {
            this.upgradeStage++;
            baseAttack *= 2;
        }
    }

    /**
     * Enables or disables damage output for this weapon.
     *
     * @param status {@code true} to disable damage, {@code false} to allow damage
     */
    public void setDisableDamage(boolean status) {
        this.disableDamage = status;
    }

    /**
     * Gets the current damage multiplier applied to this weapon.
     *
     * @return current damage multiplier (default {@code 1.0})
     */
    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    /**
     * Sets the damage multiplier for this weapon.
     *
     * <p>The multiplier must be non-negative and finite. For example:</p>
     * <ul>
     *   <li>{@code 1.0} = normal damage (default)</li>
     *   <li>{@code 2.0} = double damage</li>
     *   <li>{@code 0.5} = half damage</li>
     * </ul>
     *
     * @param multiplier the new damage multiplier
     */
    public void setDamageMultiplier(float multiplier) {
        if (Float.isFinite(multiplier) && multiplier >= 0f) {
            this.damageMultiplier = multiplier;
        } else {
            logger.warn("Ignoring invalid damage multiplier: {}", multiplier);
        }
    }
}
