package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;


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

    private static final ProjectileConfig configs =
            FileLoader.readClass(ProjectileConfig.class, "configs/projectiles.json");



    public static Entity createPistolBullet() {
        Entity pistolBullet = createBaseProjectile();
        PistolBulletConfig config = configs.pistolBullet;
        pistolBullet
                .addComponent(new CombatStatsComponent(config.health, config.base_attack))
                .addComponent(new TextureRenderComponent("images/ammo.png"));
        pistolBullet.scaleHeight(1.5f);
        return pistolBullet;
    }

    private static Entity createBaseProjectile() {

        Entity projectile =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsProjectileComponent())
                        .addComponent(new ColliderComponent().setLayer(PhysicsLayer.NONE))
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NONE));

        projectile.getComponent(PhysicsComponent.class).setBodyType(BodyDef.BodyType.DynamicBody);
        PhysicsUtils.setScaledCollider(projectile, 0.5f, 0.2f);

        return projectile;
    }

    private ProjectileFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

}
