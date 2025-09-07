package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.entities.configs.projectiles.LaserConfig;
import com.csse3200.game.entities.configs.projectiles.PistolBulletConfig;
import com.csse3200.game.entities.configs.projectiles.ProjectileConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.*;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.rendering.TextureRenderComponent;


/**
 * Factory to projectile entities with predefined components.
 *
 * <p>Each projectile entity type should have a creation method that returns a corresponding entity.
 * Predefined entity properties can be loaded from configs stored as json files which are defined in
 * "ProjectileConfig".
 *
 * <p>If needed, this factory can be separated into more specific factories for projectiles with
 * similar characteristics.
 */

public class ProjectileFactory {

    //removed final modifier to allow for class to be tested
    private static ProjectileConfig configs =
            FileLoader.readClass(ProjectileConfig.class, "configs/projectiles.json");

    public static Entity createProjectile(Class<? extends ProjectileConfig> configClass) {
        ProjectileConfig config;
        try {
            config = configClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Entity projectile = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsProjectileComponent())
                .addComponent(new WeaponsStatsComponent(config))
                .addComponent(new TextureRenderComponent(config.texturePath))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(config.projectileType))
                .addComponent(new TouchAttackComponent(config.target, 1f));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);
        ColliderComponent collider = projectile.getComponent(ColliderComponent.class);
        collider.setLayer(config.projectileType)
                .setFilter(config.projectileType, config.target);

        return projectile;
    }


    /**
     * Creates a pistol bullet entity
     * @return pistol bullet entity
     */
    public static Entity createPistolBullet() {
        Entity pistolBullet = createProjectile(PistolBulletConfig.class);
        pistolBullet.scaleHeight(0.85f);
        return pistolBullet;
    }

    /**
     * Creates a laser shot entity
     * @return The laser entity
     */
    public static Entity createEnemyProjectile(Vector2 direction) {
        Entity projectile = createProjectile(LaserConfig.class);

        float angleToFire = direction.angleDeg() + 90;

        projectile.getComponent(TextureRenderWithRotationComponent.class).setRotation(angleToFire);
        projectile.getComponent(TextureRenderWithRotationComponent.class).scaleEntity();

        projectile.getComponent(PhysicsProjectileComponent.class).create(); // Not called for some reason.
        projectile.getComponent(PhysicsProjectileComponent.class).fire(direction, LaserConfig.speed);

        return projectile;
    }


    private ProjectileFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}
