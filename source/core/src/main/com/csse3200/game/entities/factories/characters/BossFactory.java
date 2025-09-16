package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.npc.Boss2AnimationController;
import com.csse3200.game.components.npc.BossAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.tasks.BossChaseTask;
import com.csse3200.game.components.tasks.BossFuryTask;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.components.boss.*;
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
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.boss.CocoonSpawnerComponent;
import com.csse3200.game.components.boss.IndividualCocoonComponent;


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
                .addComponent(new BossAnimationController())
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f))
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(new WeaponsStatsComponent(5))
                .addComponent(new BossStatusDisplay("Boss_1"))
                .addComponent(new BossDeathComponent())
                .addComponent(animator);

        animator.startAnimation("Idle");
        animator.scaleEntity();

        AITaskComponent ai = new AITaskComponent()
                .addTask(new BossChaseTask(
                        target,
                        10,
                        12f,
                        18f
                ))
                .addTask(new WanderTask(
                        new Vector2(1f, 1f),
                        2f
                ));
        robot.addComponent(ai);

        robot.addComponent(new BossFuryTask(
                0.5f,
                1.5f,
                5,
                0.25f
                ));

        int maxHp = robot.getComponent(CombatStatsComponent.class).getHealth();
        int defenseHp = Math.round(maxHp * 0.30f);

        robot
                .addComponent(new com.csse3200.game.components.boss.DamageReductionComponent())
                .addComponent(new com.csse3200.game.components.boss.BossDefenseComponent(
                        10f,
                        1.0f,
                        defenseHp,
                        false
                ));

        // Replace original defense component with new cocoon defense component
        Vector2[] cocoonPositions = getDefaultCocoonPositions();
        robot.addComponent(new CocoonSpawnerComponent(0.30f, cocoonPositions));

        // Add defense animation listeners
        robot.getEvents().addListener("startDefenseMode", () -> {
            AnimationRenderComponent anim = robot.getComponent(AnimationRenderComponent.class);
            if (anim != null && anim.hasAnimation("defense")) {
                anim.startAnimation("defense");
            }
        });

        robot.getEvents().addListener("endDefenseMode", () -> {
            AnimationRenderComponent anim = robot.getComponent(AnimationRenderComponent.class);
            if (anim != null) {
                anim.startAnimation("Idle");
            }
        });

        robot.getComponent(AnimationRenderComponent.class).scaleEntity();
        Vector2 s = robot.getScale();
        float k = 2.0f;
        robot.setScale(s.x * k, s.y * k);

        PhysicsUtils.setScaledCollider(robot, 0.1f, 0.3f);
        robot.getComponent(ColliderComponent.class).setDensity(1.5f);

        return robot;
    }


    public static Entity createBoss2(Entity target) {
        Entity boss2 = createBaseBoss2(target);

        BaseEntityConfig config = configs.boss2;
        InventoryComponent playerInventory =
                (target != null) ? target.getComponent(InventoryComponent.class) : null;
        float patrolCenterX   = 5f;
        float patrolHalfWidth = 3f;
        float leftX  = patrolCenterX - patrolHalfWidth;
        float rightX = patrolCenterX + patrolHalfWidth;
        float patrolY = 8f;
        float patrolSpeed = 4f;

        boss2
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(new DamageReductionComponent())
                .addComponent(new AttackProtectionComponent())
                .addComponent(new AttackProtectionDisplay())
                .addComponent(new com.csse3200.game.components.boss.Boss2HealthPhaseSwitcher(
                        0.5f,   // phase2 阈值 / threshold
                        0.3f,   // angry  阈值 / threshold
                        "idle", "phase2", "angry"
                ))
                .addComponent(new BossStageComponent(boss2))
                .addComponent(new FireballAttackComponent(target, 1.5f, 8f, 6f, config.baseAttack + 2))
                .addComponent(new BossChargeSkillComponent(
                        target,
                        6f,
                        5f,
                        0.4f,
                        12f,
                        0.6f,
                        1.5f,
                        leftX, rightX, patrolY, patrolSpeed
                ))
                .addComponent(new BlackholeComponent(target, 7f, 8f))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new BossDeathComponent())
                .addComponent(new MissueAttackComponent())
                .addComponent(new BossStatusDisplay("Boss_2"));

        AnimationRenderComponent arc = boss2.getComponent(AnimationRenderComponent.class);
        if (arc != null) {
            arc.scaleEntity();
            float k = 3.0f;
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
                .addComponent(new CombatStatsComponent(config.health))
                .addComponent(new WeaponsStatsComponent(config.baseAttack))
                .addComponent(new EnemyDeathRewardComponent(100, playerInventory))
                .addComponent(new BossDeathComponent())
                .addComponent(new TextureRenderComponent("images/Boss_3.png"))
                .addComponent(new BossStatusDisplay("Boss_3"));;

        boss3.getComponent(TextureRenderComponent.class).scaleEntity();
        boss3.setScale(new Vector2(2f, 2f));
        PhysicsUtils.setScaledCollider(boss3, 1.2f, 0.6f);

        boss3.addComponent(new EnemyMudBallAttackComponent(
                target, "boss3_attack_cpu", 1.2f, 0f, 11f, 3f));
        boss3.addComponent(new EnemyMudRingSprayComponent(
                2.5f, 12, 6f, 3f));

        return boss3;
    }

    public static Entity createBlackhole(Vector2 pos,Entity target){
        Entity Blackhole = new Entity()
                .addComponent(new TextureRenderComponent("images/blackhole1.png"))
                .addComponent(new BlackholeAttackComponent(target,1.5f,4f));
        Blackhole.setPosition(pos);
        Blackhole.getComponent(TextureRenderComponent.class).scaleEntity();
        Vector2 s = Blackhole.getScale();
        float k = 1.8f;
        Blackhole.setScale(s.x * k, s.y * k);
        return Blackhole;
    }
    public static Entity createFireball(Vector2 from, Vector2 velocity) {
        Entity fireball = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new FireballMovementComponent(velocity))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
                .addComponent(new CombatStatsComponent(1))
                .addComponent(new WeaponsStatsComponent(12))
                .addComponent(new PhysicsProjectileComponent())
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1f));
        fireball.setPosition(from);
        TextureRenderComponent texture = new TextureRenderComponent("images/laserball.png");
        fireball.addComponent(texture);
        texture.scaleEntity();
        ColliderComponent collider = fireball.getComponent(ColliderComponent.class);
        collider.setLayer(PhysicsLayer.ENEMY_PROJECTILE)
                .setFilter(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.PLAYER);
        return fireball;
    }
    public static Entity createWarning(Vector2 pos) {
        Entity warning = new Entity()
                .addComponent(new TextureRenderComponent("images/warning.png"));
        warning.setPosition(pos);
        return warning;
    }
    public static Entity createMissle(Vector2 from) {
        Entity missle = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.ENEMY_PROJECTILE))
                .addComponent(new CombatStatsComponent(1))
                .addComponent(new WeaponsStatsComponent(12))
                .addComponent(new PhysicsProjectileComponent())
                .addComponent(new MissleMovementComponent(3f))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1f));
        missle.setPosition(from);
        TextureRenderComponent texture = new TextureRenderComponent("images/missle.png");
        missle.addComponent(texture);
        texture.scaleEntity();
        Vector2 s = missle.getScale();
        missle.setScale(s.x * 0.3f, s.y * 0.3f);
        ColliderComponent collider = missle.getComponent(ColliderComponent.class);
        collider.setLayer(PhysicsLayer.ENEMY_PROJECTILE)
                .setFilter(PhysicsLayer.ENEMY_PROJECTILE, PhysicsLayer.PLAYER);
        return missle;
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

    public static class ApplyInitialBoss2Setup extends Component {
        private final float scaleK;
        private final String startAnim;
        public ApplyInitialBoss2Setup(float scaleK, String startAnim) {
            this.scaleK = scaleK; this.startAnim = startAnim;
        }
        @Override public void create() {
            AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
            if (arc != null) {
                arc.scaleEntity();                      // 此时 entity 已注入，不会 NPE
                Vector2 s = entity.getScale();
                entity.setScale(s.x * scaleK, s.y * scaleK);
                if (arc.hasAnimation(startAnim)) arc.startAnimation(startAnim);
            }
        }
    }




    // Base：只负责渲染、碰撞、起始动画（不要主动触发 phase2）
    public static Entity createBaseBoss2(Entity target) {
        Entity boss = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));

        TextureAtlas atlas = ServiceLocator.getResourceService()
                .getAsset("images/boss_idle.atlas", TextureAtlas.class);

        AnimationRenderComponent arc = new AnimationRenderComponent(atlas);
        arc.setDisposeAtlas(false);
        arc.addAnimation("idle",   0.10f, Animation.PlayMode.LOOP);
        arc.addAnimation("phase2", 0.1f, Animation.PlayMode.LOOP);
        arc.addAnimation("angry", 0.1f, Animation.PlayMode.LOOP);
        boss.addComponent(arc);
        // 碰撞体缩放
        PhysicsUtils.setScaledCollider(boss, 0.9f, 0.4f);

        // 延迟到 create() 再做首帧缩放与播放
        boss.addComponent(new ApplyInitialBoss2Setup(4f, "idle"));

        return boss;
    }

    /**
     * Get default spawn positions for cocoons
     * @return Array of cocoon spawn positions
     */
    public static Vector2[] getDefaultCocoonPositions() {
        return new Vector2[] {
                new Vector2(30f, 12f),   // Top left
                new Vector2(8f, 7f),   // Top right
                new Vector2(10f, 10f),   // Bottom left
        };
    }

    /**
     * Create Robot with cocoon spawning capability (enhanced version)
     * @param target The player entity that the boss will chase and attack
     * @return Enhanced Robot entity with cocoon spawning capability
     */
    public static Entity createRobotWithCocoons(Entity target) {
        // Create original robot using existing method
        Entity robot = createRobot(target);

        // Add cocoon spawner component to existing robot
        Vector2[] cocoonPositions = getDefaultCocoonPositions();
        robot.addComponent(new CocoonSpawnerComponent(0.30f, cocoonPositions));

        // Add event listeners for cocoon spawning
        robot.getEvents().addListener("cocoonsSpawned", (Integer count) -> {
            System.out.println("Boss defense activated! " + count + " cocoons spawned!");
        });

        robot.getEvents().addListener("allCocoonsDestroyed", () -> {
            System.out.println("All cocoons destroyed! Boss defense can be overcome!");
        });

        return robot;
    }

}
