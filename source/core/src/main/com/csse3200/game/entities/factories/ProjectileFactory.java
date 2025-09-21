package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ActiveProjectileTypes;
import com.csse3200.game.entities.configs.projectiles.ActiveProjectile;
import com.csse3200.game.entities.configs.projectiles.ProjectileConfig;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ServiceLocator;


/**
 * Factory to projectile entities with predefined components.
 *
 * <p>Each projectile entity type should have a creation method that returns a corresponding entity.
 * Predefined entity properties can be loaded from the config classes
 *
 * <p>If needed, this factory can be separated into more specific factories for projectiles with
 * similar characteristics.
 */

public class ProjectileFactory {

    /**
     * Base class to create a projectile.
     *
     * @param target      The physics layer the projectile can collide with
     * @param source      The stats of the weapon that shot the projectile.
     * @param texturePath The texture of the projectile
     * @return An entity for the projectile
     */
    public static Entity createProjectile(ProjectileTarget target, WeaponsStatsComponent source, String texturePath) {
        // Create a config based on the projectile's target
        ProjectileConfig config = new ProjectileConfig(target, texturePath);

        // Create the projectile and add components
        Entity projectile = new Entity().addComponent(new PhysicsComponent()).addComponent(new PhysicsProjectileComponent()).addComponent(new WeaponsStatsComponent(source.getBaseAttack())).addComponent(new TextureRenderWithRotationComponent(config.texturePath)).addComponent(new ColliderComponent()).addComponent(new HitboxComponent().setLayer(config.projectileType)).addComponent(new TouchAttackComponent(config.target, 1f));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Ensure the collision is only checked between the projectile and the target
        ColliderComponent collider = projectile.getComponent(ColliderComponent.class);
        collider.setLayer(config.projectileType).setFilter(config.projectileType, config.target);

        return projectile;
    }

    /**
     * Base class to create a projectile.
     *
     * @param target          The physics layer the projectile can collide with
     * @param source          The stats of the weapon that shot the projectile.
     * @param texturePath     The texture of the projectile
     * @param gravityStrength The strength of gravity affecting the projectile during its lifetime.
     * @return An entity for the projectile
     */
    public static ActiveProjectile createArcProjectile(ProjectileTarget target, WeaponsStatsComponent source, String texturePath, float gravityStrength) {
        // Create a config based on the projectile's target
        ProjectileConfig config = new ProjectileConfig(target, texturePath);

        // Create the projectile and add components
        ActiveProjectile projectile = (ActiveProjectile) new ActiveProjectile().addComponent(new PhysicsComponent()).addComponent(new PhysicsProjectileComponent()).addComponent(new WeaponsStatsComponent(source.getBaseAttack())).addComponent(new TextureRenderWithRotationComponent(config.texturePath)).addComponent(new ColliderComponent()).addComponent(new HitboxComponent().setLayer(config.projectileType)).addComponent(new TouchAttackComponent(config.target, 1f));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Ensure the collision is only checked between the projectile and the target
        ColliderComponent collider = projectile.getComponent(ColliderComponent.class);
        collider.setLayer(config.projectileType).setFilter(config.projectileType, config.target);

        projectile.setGravityStrength(gravityStrength);
        projectile.setActiveProjectileType(ActiveProjectileTypes.ARC);

        return projectile;
    }

    /**
     * Base class to create a projectile.
     *
     * @param target       The physics layer the projectile can collide with
     * @param source       The stats of the weapon that shot the projectile.
     * @param texturePath  The texture of the projectile
     * @param targetEntity the entity to follow/target
     * @param speed        The speed of the projectile
     * @return An entity for the projectile
     */
    public static ActiveProjectile createFollowingProjectile(ProjectileTarget target, WeaponsStatsComponent source, String texturePath, Entity targetEntity, float speed) {
        // Create a config based on the projectile's target
        ProjectileConfig config = new ProjectileConfig(target, texturePath);

        // Create the projectile and add components
        ActiveProjectile projectile = (ActiveProjectile) new ActiveProjectile().addComponent(new PhysicsComponent()).addComponent(new PhysicsProjectileComponent()).addComponent(new WeaponsStatsComponent(source.getBaseAttack())).addComponent(new TextureRenderWithRotationComponent(config.texturePath)).addComponent(new ColliderComponent()).addComponent(new HitboxComponent().setLayer(config.projectileType)).addComponent(new TouchAttackComponent(config.target, 1f));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        // Ensure the collision is only checked between the projectile and the target
        ColliderComponent collider = projectile.getComponent(ColliderComponent.class);
        collider.setLayer(config.projectileType).setFilter(config.projectileType, config.target);

        projectile.setTarget(targetEntity);
        projectile.setProjectileSpeed(speed);
        projectile.setActiveProjectileType(ActiveProjectileTypes.FOLLOW_TARGET);

        return projectile;
    }

    public static Entity createBomb(ProjectileTarget target, WeaponsStatsComponent source, String texturePath) {
        // Create a config based on the projectile's target
        ProjectileConfig config = new ProjectileConfig(target, texturePath);

        // Create the projectile and add components
        Entity projectile = new Entity().addComponent(new PhysicsComponent()).addComponent(new PhysicsProjectileComponent()).addComponent(new WeaponsStatsComponent(source.getBaseAttack())).addComponent(new TextureRenderWithRotationComponent(config.texturePath));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);

        return projectile;
    }

    /**
     * Creates a pistol bullet entity
     *
     * @return pistol bullet entity
     */
    public static Entity createPistolBullet(WeaponsStatsComponent source) {
        ProjectileTarget target = ProjectileTarget.ENEMY;
        Entity item = ServiceLocator.getPlayer().getComponent(InventoryComponent.class).getCurrSlot();
        //Player's weapon has the water bullet upgrade
        if (item.hasComponent(BulletEnhancerComponent.class)) {
            Entity projectile = createProjectile(target, source, "images/waterBullet.png");
            projectile.scaleHeight(0.85f);
            return projectile;
        }
        Entity projectile = createProjectile(target, source, "images/round.png");
        projectile.scaleHeight(0.85f);
        return projectile;
    }

    /**
     * Creates a standard laser shot entity projectile
     *
     * @param direction direction in which the projectile is shot
     * @param source    weapon stats to derive from
     * @return The laser entity
     */
    public static Entity createEnemyLaserProjectile(Vector2 direction, WeaponsStatsComponent source) {
        ProjectileTarget target = ProjectileTarget.PLAYER;
        Entity projectile = createProjectile(target, source, "images/laser_shot.png");

        float angleToFire = direction.angleDeg() + 90;

        projectile.getComponent(TextureRenderWithRotationComponent.class).setRotation(angleToFire);
        projectile.getComponent(TextureRenderWithRotationComponent.class).scaleEntity();

        return projectile;
    }


    private ProjectileFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}
