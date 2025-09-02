package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.LowHealthAttackBuff;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.*;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.enemy.EnemyDeathRewardComponent;
import com.csse3200.game.components.enemy.DeathParticleSpawnerComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
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
   * Creates a ghost entity.
   *
   * @param target entity to chase
   * @return entity
   */
  public static Entity createGhost(Entity target) {
    Entity ghost = createBaseNPC(target);
    BaseEntityConfig config = configs.ghost;

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService().getAsset("images/ghost.atlas", TextureAtlas.class));
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

    // Create the dash attack AI component
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new WanderTask(new Vector2(2f, 2f), 1000L))
            .addTask(new DashAttackTask(target, 15, new Vector2(7f, 7f), 1000L, 200L));


    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    ghost
        .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(animator)
        .addComponent(new GhostAnimationController())
        .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
        .addComponent(new DeathParticleSpawnerComponent())
        .addComponent(aiComponent);

    ghost.getComponent(AnimationRenderComponent.class).scaleEntity();

    return ghost;
  }

  /**
   * Creates a ghost king entity.
   *
   * @param target entity to chase
   * @return entity
   */
  public static Entity createGhostKing(Entity target) {
    Entity ghostKing = createBaseNPC(target);
    GhostKingConfig config = configs.ghostKing;

    AnimationRenderComponent animator =
        new AnimationRenderComponent(
            ServiceLocator.getResourceService()
                .getAsset("images/ghostKing.atlas", TextureAtlas.class));
    animator.setDisposeAtlas(false);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);

    // Create the constant chase AI task
    AITaskComponent aiComponent =
        new AITaskComponent()
          .addTask(new WanderTask(new Vector2(2f, 2f), 2000L))
          .addTask(new DashAttackTask(target, 15, new Vector2(15f, 15f), 500L, 300L));


    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    ghostKing
        .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
        .addComponent(animator)
        .addComponent(new GhostAnimationController())
        .addComponent(new EnemyDeathRewardComponent(30, playerInventory))
        .addComponent(new DeathParticleSpawnerComponent())
        .addComponent(aiComponent);

    ghostKing.getComponent(AnimationRenderComponent.class).scaleEntity();
    return ghostKing;
  }
  /**
   * Creates GhostGPT enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @return entity
   */
  public static Entity createGhostGPT(Entity target, ForestGameArea area) {
    Entity ghostGPT = createBaseNPC(target);
    GhostGPTConfig config = configs.ghostGPT;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService()
                            .getAsset("images/ghostGPT.atlas", TextureAtlas.class));
    animator.setDisposeAtlas(false);
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);


    ProjectileLauncherComponent projComp = new ProjectileLauncherComponent(area, target);
    AITaskComponent aiComponent =
        new AITaskComponent()
            .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
            .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f), projComp, ghostGPT));

    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    CombatStatsComponent ghostGPTStats = new CombatStatsComponent(config.health, config.baseAttack);

    ghostGPT
            .addComponent(ghostGPTStats)
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuff(10, ghostGPTStats))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent) // Add reward + particles
            .addComponent(projComp); // Add the ability to fire projectiles

    ghostGPT.getComponent(AnimationRenderComponent.class).scaleEntity();

    return ghostGPT;
  }

  /**
   * Creates Deepspin enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @return entity
   */
  public static Entity createDeepspin(Entity target, ForestGameArea area) {
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

    CombatStatsComponent deepspinStats = new CombatStatsComponent(config.health, config.baseAttack);

    deepspin
            .addComponent(deepspinStats)
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuff(10, deepspinStats))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent) // Add reward + particles
            .addComponent(new ProjectileLauncherComponent(area, target)); // Add the ability to fire projectiles

    deepspin.getComponent(AnimationRenderComponent.class).scaleEntity();

    return deepspin;
  }

  /**
   * Creates Deepspin enemy type
   *
   * @param target entity to chase
   * @param area the area/space it is living in
   * @return entity
   */
  public static Entity createGrokDroid(Entity target, ForestGameArea area) {
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

    CombatStatsComponent grokDroidStats = new CombatStatsComponent(config.health, config.baseAttack);

    grokDroid
            .addComponent(grokDroidStats)
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new LowHealthAttackBuff(10, grokDroidStats))
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
            .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
            .addComponent(aiComponent) // Add reward + particles
            .addComponent(new ProjectileLauncherComponent(area, target)); // Add the ability to fire projectiles

    grokDroid.getComponent(AnimationRenderComponent.class).scaleEntity();

    return grokDroid;
  }
  /**
   * Creates a Vroomba entity.
   *
   * @param target entity to chase
   * @return entity
   */
  public static Entity createVroomba(Entity target, ForestGameArea area) {
    Entity vroomba = createBaseNPC(target);
    VroombaConfig config = configs.vroomba;

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService().getAsset("images/Vroomba.atlas", TextureAtlas.class));
    animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);

    // Create the dash attack AI component
    AITaskComponent aiComponent =
            new AITaskComponent()
                    .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
                    .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f)));


    // Get player's inventory for reward system
    InventoryComponent playerInventory = null;
    if (target != null) {
      playerInventory = target.getComponent(InventoryComponent.class);
    }

    vroomba
            .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
            .addComponent(animator)
            .addComponent(new GhostAnimationController())
            .addComponent(new EnemyDeathRewardComponent(15, playerInventory))
            .addComponent(new DeathParticleSpawnerComponent())
            .addComponent(aiComponent);

    vroomba.getComponent(AnimationRenderComponent.class).scaleEntity();

    return vroomba;
  }
  /**
   * Creates a generic NPC to be used as a base entity by more specific NPC creation methods.
   *
   * @return entity
   */
  private static Entity createBaseNPC(Entity target) {
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
