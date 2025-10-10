package com.csse3200.game.components.items;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.*;
import com.csse3200.game.effects.AimbotEffect;
import com.csse3200.game.effects.AreaEffect;
import com.csse3200.game.effects.Effect;
import com.csse3200.game.effects.UnlimitedAmmoEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component responsible for handling ranged item usage (e.g., guns, bombs).
 * <p>
 * When used, this component:
 * <ul>
 *   <li>Plays an attack sound</li>
 *   <li>Creates a projectile entity (bullet or bomb)</li>
 *   <li>Fires the projectile toward the mouse input</li>
 *   <li>If the item is a consumable bomb, schedules its timed behavior
 *       (rotation, pulsing, stopping, and explosion)</li>
 * </ul>
 */
public class RangedUseComponent extends ItemActionsComponent {
    private float timeSinceLastAttack = -9999f;
    private Entity player;

    /**
     * Uses the ranged weapon/item for the given player.
     * <p>
     * Spawns and fires a projectile toward the cursor position.
     * If the item is a consumable (e.g., bomb), schedules delayed
     * explosion behavior.
     *
     * @param player the entity representing the player using the item
     */
    public void use(Entity player) {
        this.player = player;

        WeaponsStatsComponent weaponsStats = entity.getComponent(WeaponsStatsComponent.class);
        String texturePath = weaponsStats.getProjectileTexturePath();
        if (!handleMagazine(weaponsStats)) { // dont run if active
            return;
        }
        playAttackSound();

        Entity bullet = createProjectileEntity(weaponsStats, texturePath, isPowerUpActive(AimbotEffect.class));

        Camera cam = ServiceLocator.getCamera();
        initializeBullet(bullet, cam);

        if (entity.hasComponent(ConsumableComponent.class)) {
            bombTimer(entity.getComponent(ConsumableComponent.class).getDuration(), bullet);
        } else {
            if (!isPowerUpActive(UnlimitedAmmoEffect.class)) {
                decrementMagazine();
            }
            player.getEvents().trigger("after shoot");
        }
        timeSinceLastAttack = ServiceLocator.getTimeSource().getTime();
    }

    private boolean handleMagazine(WeaponsStatsComponent weaponsStats) {
        if (entity.hasComponent(ConsumableComponent.class)) {
            return true;
        }
        MagazineComponent mag = entity.getComponent(MagazineComponent.class);
        mag.update();
        float coolDown = weaponsStats.getCoolDown();
        float curTime = ServiceLocator.getTimeSource().getTime();
        if ((curTime - timeSinceLastAttack) < coolDown * 1000) { // Converts to milliseconds
            return false;
        }
        if (mag.getCurrentAmmo() <= 0 && !isPowerUpActive(UnlimitedAmmoEffect.class)) {
            Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/shot_failed.mp3", Sound.class);
            attackSound.play();
            return false;
        }
        return true;
    }

    private void decrementMagazine() {
        MagazineComponent mag = entity.getComponent(MagazineComponent.class);
        mag.setCurrentAmmo(mag.getCurrentAmmo() - 1);
    }

    /**
     * Plays the attack sound for the ranged weapon.
     */
    private void playAttackSound() {
        Sound attackSound = ServiceLocator.getResourceService()
                .getAsset("sounds/laser_blast.mp3", Sound.class);
        attackSound.play();
    }

    /**
     * Creates either a bomb or a standard projectile entity depending
     * on whether this item is a consumable.
     *
     * @param weaponsStats the stats of the weapon
     * @param texturePath  the texture path for the projectile
     * @return the created projectile entity
     */
    Entity createProjectileEntity(WeaponsStatsComponent weaponsStats, String texturePath, boolean isHoming) {
        boolean isConsumable = entity.hasComponent(ConsumableComponent.class);
        return isConsumable
                ? ProjectileFactory.createBomb(ProjectileTarget.ENEMY, weaponsStats, texturePath)
                : ProjectileFactory.createPistolBullet(weaponsStats, isHoming);
    }

    /**
     * Initializes a projectile entity (bullet).
     * <ul>
     *   <li>Places it at the player's position</li>
     *   <li>Registers it in the entity service</li>
     *   <li>Fires it toward the mouse input</li>
     * </ul>
     *
     * @param bullet the projectile entity
     * @param cam    the active camera
     */
    private void initializeBullet(Entity bullet, Camera cam) {
        PhysicsProjectileComponent projectilePhysics = bullet.getComponent(PhysicsProjectileComponent.class);

        // Place at player position
        Vector2 origin = new Vector2(player.getPosition());
        bullet.setPosition(origin.add(0.5f, 0.2f));

        // Register entity
        ServiceLocator.getEntityService().register(bullet);

        // Fire toward mouse input
        Vector2 center = bullet.getCenterPosition();
        Vector3 destination = cam.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        Vector2 destination2D = new Vector2(destination.x, destination.y);
        Vector2 adjustedDestination = destination2D.sub(center);
        projectilePhysics.fire(
                adjustedDestination,
                5
        );
        player.getEvents().trigger("player_shoot_order", destination2D, adjustedDestination);
    }

    /**
     * Starts the bomb timer for a consumable projectile.
     * <p>
     * Schedules its rotation, stop, pulsing animation, and eventual explosion.
     *
     * @param duration duration before explosion
     * @param bullet   the bomb entity
     */
    private void bombTimer(float duration, Entity bullet) {
        ConsumableComponent consumable = entity.getComponent(ConsumableComponent.class);

        scheduleBulletRotation(bullet);
        scheduleBulletStop(bullet);
        scheduleBulletPulse(bullet);
        scheduleExplosion(bullet, consumable, duration);
    }

    /**
     * Continuously rotates the bullet until it is removed.
     *
     * @param bullet the bullet entity
     */
    private void scheduleBulletRotation(Entity bullet) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (ServiceLocator.getTimeSource().isPaused()) {
                    return;
                }

                PhysicsComponent physics = bullet.getComponent(PhysicsComponent.class);
                if (physics == null || physics.getBody() == null) {
                    cancel(); // bullet gone, stop rotating
                    return;
                }

                TextureRenderWithRotationComponent render = bullet.getComponent(TextureRenderWithRotationComponent.class);
                render.setRotation(render.getRotation() + (float) 10.0);
            }
        }, 0f, 1 / 60f);
    }

    /**
     * Stops the bullet's movement after a short delay.
     *
     * @param bullet the bullet entity
     */
    private void scheduleBulletStop(Entity bullet) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                PhysicsComponent physics = bullet.getComponent(PhysicsComponent.class);
                if (physics != null && physics.getBody() != null) {
                    physics.getBody().setLinearVelocity(Vector2.Zero);
                }
            }
        }, 0.5f);
    }

    /**
     * Makes the bullet pulse (grow/shrink repeatedly) until it is disposed.
     *
     * @param bullet the bullet entity
     */
    private void scheduleBulletPulse(Entity bullet) {
        final Vector2 baseScale = bullet.getScale().cpy();

        Timer.schedule(new Timer.Task() {
            boolean enlarge = false;

            @Override
            public void run() {
                if (ServiceLocator.getTimeSource().isPaused()) {
                    return;
                }

                PhysicsComponent physics = bullet.getComponent(PhysicsComponent.class);
                if (physics == null || physics.getBody() == null) {
                    cancel(); // stop pulsing if bullet is gone
                    return;
                }

                Vector2 currentPos = bullet.getPosition();
                Vector2 oldScale = bullet.getScale().cpy();

                Vector2 newScale = enlarge
                        ? baseScale.cpy().scl(1.5f)
                        : baseScale.cpy();

                float dx = (oldScale.x - newScale.x) / 2f;
                float dy = (oldScale.y - newScale.y) / 2f;

                bullet.setScale(newScale);
                bullet.setPosition(new Vector2(currentPos.x + dx, currentPos.y + dy));

                enlarge = !enlarge;
            }
        }, 0.5f, 0.2f);
    }

    /**
     * Creates an explosion at the bullet's position after a delay.
     * <p>
     * Applies all effects from the consumable to the explosion entity
     * and disposes both the bullet and the explosion afterward.
     *
     * @param bullet     the bullet entity
     * @param consumable the consumable component defining the explosion's effects
     * @param delay      delay before the explosion
     */
    private void scheduleExplosion(Entity bullet, ConsumableComponent consumable, float delay) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (ServiceLocator.getTimeSource().isPaused()) {
                    return;
                }

                if (consumable == null) {
                    bullet.dispose();
                    return;
                }

                final Entity explosion = ItemFactory.createItem("images/electric_zap.png");
                AreaEffect aoe = (AreaEffect) consumable.getEffect(AreaEffect.class);
                float aoeSize = 2 * aoe.getRadius();

                // Get bullet center
                float bulletCenterX = bullet.getPosition().x + bullet.getScale().x / 2f;
                float bulletCenterY = bullet.getPosition().y + bullet.getScale().y / 2f;
                Vector2 bulletCenter = new Vector2(bulletCenterX, bulletCenterY);

                explosion.setScale(new Vector2(aoeSize, aoeSize));

                // Position explosion so it's centered
                Vector2 explosionPos = new Vector2(
                        bulletCenter.x - aoeSize / 2f,
                        bulletCenter.y - aoeSize / 2f
                );

                explosion.setPosition(explosionPos);
                explosion.create();

                for (Effect effect : consumable.getEffects()) {
                    effect.apply(explosion);
                }

                bullet.dispose();

                // Dispose explosion after 0.3s
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        explosion.dispose();
                    }
                }, 0.3f);
            }
        }, delay);
    }

    // Power Ups Checking
    private boolean isPowerUpActive(Class<? extends Effect> powerUpEffect) {
        PowerupComponent powerUps = player.getComponent(PowerupComponent.class);
        return powerUps.hasEffect(powerUpEffect);
    }
}
