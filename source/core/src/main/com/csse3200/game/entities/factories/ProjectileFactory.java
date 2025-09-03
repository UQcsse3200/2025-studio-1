package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
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


    /**
     * Creates a pistol bullet entity
     * @return pistol bullet entity
     */
    public static Entity createPistolBullet() {
        Entity pistolBullet = createBaseProjectile();
        PistolBulletConfig config = configs.pistolBullet;
        pistolBullet
                .addComponent(new CombatStatsComponent(config.health, config.base_attack))
                .addComponent(new TextureRenderComponent("images/round.png"))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.FRIENDLY_PROJECTILE))
                .addComponent(new TouchAttackComponent(PhysicsLayer.NPC, 1f));

        ColliderComponent collider = pistolBullet.getComponent(ColliderComponent.class);
        collider.setLayer(PhysicsLayer.FRIENDLY_PROJECTILE)
                .setFilter(PhysicsLayer.FRIENDLY_PROJECTILE, (short) (PhysicsLayer.NPC));

        pistolBullet.scaleHeight(0.85f);
        return pistolBullet;
    }

    /**
     * Creates a laser shot entity
     * @param direction The direction to fire at
     * @return The laser entity
     */
    public static Entity createLaserShot(Vector2 direction) {
        Entity laser = createBaseProjectile();
        LaserConfig config = configs.laser;
        laser
                .addComponent(new TextureRenderWithRotationComponent("images/laser_shot.png"))
                .addComponent(new CombatStatsComponent(config.health, config.base_attack))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER)); // Knockback??

        ColliderComponent collider = laser.getComponent(ColliderComponent.class);
        collider.setLayer(PhysicsLayer.ENEMY_PROJECTILE)
                .setFilter(PhysicsLayer.ENEMY_PROJECTILE, (short) (PhysicsLayer.PLAYER));

        float angleToFire = direction.angleDeg() + 90;

        laser.getComponent(TextureRenderWithRotationComponent.class).setRotation(angleToFire);
        laser.getComponent(TextureRenderWithRotationComponent.class).scaleEntity();
        laser.scaleWidth(0.5f);
        laser.scaleHeight(0.5f);

        laser.getComponent(PhysicsProjectileComponent.class).create(); // Not called for some reason.
        laser.getComponent(PhysicsProjectileComponent.class).fire(direction, config.speed);

        return laser;
    }

    /**
     * Creates a base projectile entity, capable of motion
     * @return projectile entity
     */

    private static Entity createBaseProjectile() {

        Entity projectile =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsProjectileComponent());

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);


        return projectile;
    }



    private ProjectileFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}
