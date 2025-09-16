package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.enemy.LowHealthAttackBuffComponent;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.components.npc.BossAnimationController;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.*;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Projectiles;
import com.csse3200.game.entities.configs.characters.*;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory to create non-playable character (NPC) entities with predefined components.
 *
 * <p>Each NPC entity type should have a creation method that returns a corresponding entity.
 * Predefined entity properties can be loaded from configs stored as json files which are defined in
 * "NPCConfigs".
 *
 * <p>If needed, this factory can be separated into more specific factories for entities with
 * similar characteristics.
 */
public class NPCFactory {
  private static final NPCConfigs configs =
      FileLoader.readClass(NPCConfigs.class, "configs/NPCs.json");

  /**
   * Creates GhostGPT enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @param scalingFactor The scale of increase in health and attack of the GhostGPT
   * @return entity
   */
  public static Entity createGhostGPT(Entity target, GameArea area, float scalingFactor) {
    // Build GhostGPT as a ground enemy (do not use createBaseNPC to avoid floating movement)
    Entity ghostGPT = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
    PhysicsUtils.setScaledCollider(ghostGPT, 0.9f, 0.4f);

    GhostGPTConfig config = configs.ghostGPT;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService()
                            .getAsset("images/ghostGPT.atlas", TextureAtlas.class));
    animator.setDisposeAtlas(false);
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);


    ProjectileLauncherComponent projComp = new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER);
    // Use ground chase tasks for gravity-based movement
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
            .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f, projComp, ghostGPT, 3f, 3f));

    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    WeaponsStatsComponent ghostGPTStats = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

    ghostGPT
            .addComponent(ghostGPTStats)
            .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuffComponent(10, ghostGPTStats))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory)) // Add reward + particles
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent)
            .addComponent(projComp) // Add the ability to fire projectiles
            .addComponent(new EnemyHealthDisplay(1.3f));

    ghostGPT.getComponent(AnimationRenderComponent.class).scaleEntity();

    return ghostGPT;
  }

  /**
   * Creates a robot entity.
   *
   * @param target entity to chase (e.g. player)
   * @return robot entity
   */
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
            .addComponent(new EnemyHealthDisplay());

    return robot;
  }

  /**
   * Creates Deepspin enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @param scalingFactor The scale of increase in health and attack of the DeepSpin
   * @return entity
   */
  public static Entity createDeepspin(Entity target, GameArea area, float scalingFactor) {
    Entity deepspin = createBaseNPC(target);
    DeepspinConfig config = configs.deepSpin;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService()
                            .getAsset("images/Deepspin.atlas", TextureAtlas.class));
    animator.setDisposeAtlas(false);
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

    AITaskComponent aiComponent =
            new AITaskComponent()
                    .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
                    .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f)));

    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    CombatStatsComponent deepspinStats = new CombatStatsComponent((int) (config.health * scalingFactor));
    WeaponsStatsComponent deepspinAttack = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

    deepspin
            .addComponent(deepspinStats)
            .addComponent(deepspinAttack)
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuffComponent(10, deepspinAttack))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory)) // Add reward + particles
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent)
            .addComponent(new EnemyHealthDisplay())
            .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

    deepspin.getComponent(AnimationRenderComponent.class).scaleEntity();

    return deepspin;
  }

  /**
   * Creates GrokDroid enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @param scalingFactor The scale of increase in health and attack of the GrokDroid
   * @return entity
   */
  public static Entity createGrokDroid(Entity target, GameArea area, float scalingFactor) {
    Entity grokDroid = createBaseNPC(target);
    GrokDroidConfig config = configs.grokDroid;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService()
                            .getAsset("images/Grokdroid.atlas", TextureAtlas.class));
    animator.setDisposeAtlas(false);
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

    AITaskComponent aiComponent =
            new AITaskComponent()
                    .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
                    .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f)));

    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    CombatStatsComponent grokDroidStats = new CombatStatsComponent((int) (config.health * scalingFactor));
    WeaponsStatsComponent grokDroidWeapon = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

    grokDroid
            .addComponent(grokDroidStats)
            .addComponent(grokDroidWeapon)
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuffComponent(10, grokDroidWeapon))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory)) // Add reward + particles
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent)
            .addComponent(new EnemyHealthDisplay(0.3f))
            .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

    grokDroid.getComponent(AnimationRenderComponent.class).scaleEntity();

    return grokDroid;
  }

  /**
   * Creates a Vroomba entity.
   *
   * @param target entity to chase
   * @param scalingFactor The scale of increase in health and attack of the Vroomba
   * @return entity
   */
  public static Entity createVroomba(Entity target, float scalingFactor) {
    // Ground enemy build: dynamic body with collider/hitbox; no PhysicsMovementComponent
    Entity vroomba = new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
    PhysicsUtils.setScaledCollider(vroomba, 0.9f, 0.4f);

    VroombaConfig config = configs.vroomba;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService().getAsset("images/Vroomba.atlas", TextureAtlas.class));
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

    // Ground chase: set X only; gravity handles Y (Box2D). See Box2D Manual (Forces/Impulses).
    AITaskComponent aiComponent =
            new AITaskComponent()
                    .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                    .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f));


    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    // Explosion: arm when close; damage if within radius; then die (spawn particles)
    float triggerRadius = 1.2f;
    float damageRadius = 1.6f;
    int boomDamage = Math.max(1, (int) (config.baseAttack * scalingFactor * 2));
    float fuseSeconds = 1.5f;     // fuse time after triggered, before boom

    vroomba
            .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
            .addComponent(new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor)))
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(new VroombaSuicideComponent(target, triggerRadius, damageRadius, boomDamage, fuseSeconds))
            .addComponent(aiComponent)
            .addComponent(new EnemyHealthDisplay());

    vroomba.getComponent(AnimationRenderComponent.class).scaleEntity();

    return vroomba;
  }

  /**
   * Creates a generic NPC to be used as a base entity by more specific NPC creation methods.
   *
   * @param target entity to chase
   * @return entity
   */
  static Entity createBaseNPC(Entity target) {
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new WanderTask(new Vector2(2f, 2f), 2f))
            .addTask(new ChaseTask(target, 10, 3f, 4f));
    Entity npc =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new PhysicsMovementComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
            .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));

    PhysicsUtils.setScaledCollider(npc, 0.9f, 0.4f);
    return npc;
  }

  private NPCFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
