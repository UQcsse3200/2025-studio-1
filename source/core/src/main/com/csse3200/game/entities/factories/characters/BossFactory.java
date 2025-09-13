package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.npc.BossAnimationController;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.characters.BaseEntityConfig;
import com.csse3200.game.entities.configs.characters.NPCConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.*;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.player.BossStatusDisplay;
import com.csse3200.game.components.WeaponsStatsComponent;


/**
 * A factory class for creating Boss and NPC entities for the game.
 * This factory defines behavior, physics, and rendering properties for Boss-3
 * and provides a base NPC creation method that includes movement and attack logic.
 */

public class BossFactory {
    private static final NPCConfigs configs =
            FileLoader.readClass(NPCConfigs.class, "configs/NPCs.json");

    public static Entity createRobot(Entity target) {
        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService().getAsset("images/Robot_1.atlas", TextureAtlas.class));
        animator.addAnimation("Idle",   0.12f, Animation.PlayMode.LOOP);
        animator.addAnimation("attack", 0.06f, Animation.PlayMode.LOOP);
        animator.addAnimation("fury",   0.10f, Animation.PlayMode.LOOP);
        animator.addAnimation("die",    0.10f, Animation.PlayMode.NORMAL);

        Vector2 moveSpeed = new Vector2(2.5f, 2.5f);

        Entity robot = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent(moveSpeed))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(new WeaponsStatsComponent(5))
                .addComponent(new BossStatusDisplay("Boss_1"))
                .addComponent(new BossAnimationController())
                .addComponent(animator);

        AITaskComponent ai = new AITaskComponent()
                .addTask(new ChaseTask(
                        target,
                        10,
                        12f,
                        18f
                ))
                .addTask(new WanderTask(
                        new Vector2(4f, 4f),
                        2f
                ));
        robot.addComponent(ai);

        robot.getComponent(AnimationRenderComponent.class).scaleEntity();
        Vector2 s = robot.getScale();
        float k = 2.0f;
        robot.setScale(s.x * k, s.y * k);

        PhysicsUtils.setScaledCollider(robot, 0.1f, 0.3f);
        robot.getComponent(ColliderComponent.class).setDensity(1.5f);

        return robot;
    }


    public static Entity createBoss2(Entity target) {
        Entity boss2 = createBaseNPC(target);
        BaseEntityConfig config = configs.boss2;
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/boss_idle.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

        boss2
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new FireballAttackComponent(target, 1.5f, 8f, 6f, config.baseAttack + 2))
                .addComponent(new BossChargeSkillComponent(target, 6f, 5f, 0.4f, 12f, 0.6f, 1.5f))
                .addComponent(new BlackholeComponent(target,7f,8f))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new DeathParticleSpawnerComponent())
                .addComponent(new BossStatusDisplay("Boss_2"));;
        boss2.getComponent(AnimationRenderComponent.class).scaleEntity();
        float k = 4.0f;
        Vector2 s = boss2.getScale();
        boss2.setScale(s.x * k, s.y * k);


        return boss2;
    }

    /**
     * Creates a Boss-3 entity with combat stats, rendering, scaling, and physics collider.
     * This boss is capable of wandering, chasing, and attacking the player.
     *
     * @param target The player entity that the boss will chase and attack.
     * @return A fully configured {@link Entity} representing Boss-3.
     */
    public static Entity createBoss3(Entity target) {
        BaseEntityConfig config = configs.boss3;
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }
        Entity boss3 = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(config.health))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new DeathParticleSpawnerComponent())
                .addComponent(new TextureRenderComponent("images/Boss_3.png"))
                .addComponent(new BossStatusDisplay("Boss_3"));;

        boss3.getComponent(TextureRenderComponent.class).scaleEntity();
        boss3.setScale(new Vector2(2f, 2f));
        PhysicsUtils.setScaledCollider(boss3, 2.0f, 0.8f);

        boss3.addComponent(new EnemyMudBallAttackComponent(
                target, 1.2f, 9f, 6f, 3f));
        boss3.addComponent(new EnemyMudRingSprayComponent(
                2.5f, 12, 6f, 3f));
        return boss3;
    }

    public static Entity createBlackhole(Vector2 pos,Entity target){
        Entity Blackhole = new Entity()
                .addComponent(new TextureRenderComponent("images/blackhole1.png"))
                .addComponent(new BlackholeAttackComponent(target,1f,4f));
        Blackhole.setPosition(pos);
        Blackhole.getComponent(TextureRenderComponent.class).scaleEntity();
        return Blackhole;
    }
    public static Entity createFireball(Vector2 from, Vector2 velocity) {
        Entity fireball = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new FireballMovementComponent(velocity))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new CombatStatsComponent(1))
                .addComponent(new WeaponsStatsComponent(12))
                .addComponent(new PhysicsProjectileComponent())
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 0f));
        fireball.setPosition(from);
        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/fireball.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.3f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.3f, Animation.PlayMode.LOOP);
        animator.startAnimation("float");
        animator.startAnimation("angry_float");
        fireball.addComponent(animator);
        fireball.getComponent(AnimationRenderComponent.class).scaleEntity();

        return fireball;
    }

    /**
     * Creates a base NPC entity with default wandering, chasing, physics,
     * and touch attack behavior. This is used as a template for other bosses or NPCs.
     *
     * @param target The player entity that the NPC should follow or chase.
     * @return A configured {@link Entity} with AI movement and basic combat capability.
     */

    public static Entity createBaseNPC(Entity target) {
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new WanderTask(new Vector2(4f, 4f), 2f))
                        .addTask(new ChaseTask(target, 10, 6f, 8f));
        Entity npc =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
                        .addComponent(aiComponent);

        PhysicsUtils.setScaledCollider(npc, 0.9f, 0.4f);
        return npc;
    }
}
