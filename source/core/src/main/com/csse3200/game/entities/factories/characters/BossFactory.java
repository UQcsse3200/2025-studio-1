package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.npc.Boss2AnimationController;
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
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
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
        Entity robot = createBaseNPC(target);

        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService().getAsset("images/Robot_1.atlas", TextureAtlas.class));
        animator.addAnimation("Idle",   0.12f, Animation.PlayMode.LOOP);
        animator.addAnimation("attack", 0.06f, Animation.PlayMode.LOOP);
        animator.addAnimation("fury",   0.10f, Animation.PlayMode.LOOP);
        animator.addAnimation("die",    0.10f, Animation.PlayMode.NORMAL);

        robot
                .addComponent(animator)
                .addComponent(new CombatStatsComponent(100))
                .addComponent(new WeaponsStatsComponent(5))
                .addComponent(new BossAnimationController())
                .addComponent(new BossDeathComponent())
                .addComponent(new BossStatusDisplay("Boss_1"));
        animator.startAnimation("Idle");
        animator.scaleEntity();

        robot.getComponent(AnimationRenderComponent.class).scaleEntity();
        float k = 3.0f;
        Vector2 s = robot.getScale();
        robot.setScale(s.x * k, s.y * k);

        return robot;
    }


    public static Entity createBoss2(Entity target) {
        Entity boss2 = createBaseBoss2(target);

        BaseEntityConfig config = configs.boss2;
        InventoryComponent playerInventory =
                (target != null) ? target.getComponent(InventoryComponent.class) : null;

        // ===== 手动指定巡逻走廊 =====
        float patrolCenterX   = 5f;  // 你想要的中心X
        float patrolHalfWidth = 3f;   // 左右摆动的半宽（可调）
        float leftX  = patrolCenterX - patrolHalfWidth; // = 12
        float rightX = patrolCenterX + patrolHalfWidth; // = 18
        float patrolY = 8f;          // 固定的Y（“中间偏上处”可自行改）
        float patrolSpeed = 4f;       // 左右巡逻速度（m/s，可调）

        boss2
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(new FireballAttackComponent(target, 1.5f, 8f, 6f, config.baseAttack + 2))
                // 传入 11 个参数：触发/停留/准备/速度/持续/冷却 + 左边界/右边界/固定Y/巡逻速度
                .addComponent(new BossChargeSkillComponent(
                        target,
                        6f,    // triggerRange
                        5f,  // dwellTime（原来是 5f 太久了，建议 0.3~0.6）
                        0.4f,  // prepareTime
                        12f,   // chargeSpeed
                        0.6f,  // chargeDuration
                        1.5f,  // cooldown
                        leftX, rightX, patrolY, patrolSpeed
                ))
                .addComponent(new BlackholeComponent(target, 7f, 8f))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new BossDeathComponent())
                .addComponent(new BossStatusDisplay("Boss_2"));

        // 可选：放大显示（如果 createBaseBoss2 已做可删）
        AnimationRenderComponent arc = boss2.getComponent(AnimationRenderComponent.class);
        if (arc != null) {
            arc.scaleEntity();
            float k = 4.0f;
            Vector2 s = boss2.getScale();
            boss2.setScale(s.x * k, s.y * k);
        }

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
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new CombatStatsComponent(config.health))
                .addComponent(new WeaponsStatsComponent(config.baseAttack))
                .addComponent(new AITaskComponent()
                        .addTask(new WanderTask(new Vector2(3f, 3f), 1f))
                        .addTask(new ChaseTask(target, 8, 5f, 7f)))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new BossDeathComponent())
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
        TextureRenderComponent texture = new TextureRenderComponent("images/laserball.png");
        fireball.addComponent(texture);
        texture.scaleEntity();

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

    public static Entity createBaseBoss2(Entity target) {
        Entity boss =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new PhysicsMovementComponent())
                        .addComponent(new ColliderComponent())
                        .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                        .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));

        // 载入你的 atlas（路径按你的工程调整）
        final String ATLAS_PATH = "images/boss_idle.atlas";
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(ATLAS_PATH, TextureAtlas.class);

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        boss.addComponent(arc);

        // ✅ 关键：所有状态共用同一动画基名（例如 "idle"）
        boss.addComponent(new Boss2AnimationController("idle", 0.10f, Animation.PlayMode.LOOP));

        // 尺寸/碰撞体
        arc.scaleEntity();
        PhysicsUtils.setScaledCollider(boss, 0.9f, 0.4f);

        // 可选：整体放大显示
        Vector2 s = boss.getScale();
        boss.setScale(s.x * 4f, s.y * 4f);

        return boss;
    }
}
