package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.SoundComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.enemy.*;
import com.csse3200.game.components.npc.BossAnimationController;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.tasks.*;
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
            FileLoader.read(NPCConfigs.class, "configs/NPCs.json", FileLoader.Location.INTERNAL)
                    .orElseGet(NPCConfigs::new);

    private NPCFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }

    /**
     * Creates GhostGPT enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
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
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);


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
        System.out.println(config.health * scalingFactor);
        System.out.println(scalingFactor);
        System.out.println(config.health);

        SoundComponent soundComponent = new SoundComponent();
        ghostGPT.addComponent(soundComponent);
        soundComponent.registerSound("damageTaken", "sounds/GPTDamage.mp3");
        soundComponent.registerSound("death", "sounds/GPTDeath.mp3");


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
     * Creates GhostGPTRed enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the GhostGPT
     * @return entity
     */
    public static Entity createGhostGPTRed(Entity target, GameArea area, float scalingFactor) {
        Entity ghostGPTRed = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
        PhysicsUtils.setScaledCollider(ghostGPTRed, 0.9f, 0.4f);

        GhostGPTRedConfig config = configs.ghostGPTRed;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/ghostGPTRed.atlas", TextureAtlas.class));
        animator.setDisposeAtlas(false);
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);


        ProjectileLauncherComponent projComp = new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER);
        // Use ground chase tasks for gravity-based movement
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                        .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f, projComp, ghostGPTRed, 3f, 3f));

        // Get player's inventory for reward system
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        WeaponsStatsComponent ghostGPTRedStats = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        ghostGPTRed
                .addComponent(ghostGPTRedStats)
                .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, ghostGPTRedStats))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(projComp) // Add the ability to fire projectiles
                .addComponent(new EnemyHealthDisplay(1.3f));

        ghostGPTRed.getComponent(AnimationRenderComponent.class).scaleEntity();

        return ghostGPTRed;
    }

    /**
     * Creates GhostGPTBlue enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the GhostGPT
     * @return entity
     */
    public static Entity createGhostGPTBlue(Entity target, GameArea area, float scalingFactor) {
        Entity ghostGPTBlue = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
        PhysicsUtils.setScaledCollider(ghostGPTBlue, 0.9f, 0.4f);

        GhostGPTBlueConfig config = configs.ghostGPTBlue;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/ghostGPTBlue.atlas", TextureAtlas.class));
        animator.setDisposeAtlas(false);
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);


        ProjectileLauncherComponent projComp = new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER);
        // Use ground chase tasks for gravity-based movement
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                        .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f, projComp, ghostGPTBlue, 2f, 3f));

        // Get player's inventory for reward system
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        WeaponsStatsComponent ghostGPTBlueStats = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        ghostGPTBlue
                .addComponent(ghostGPTBlueStats)
                .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, ghostGPTBlueStats))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(projComp) // Add the ability to fire projectiles
                .addComponent(new EnemyHealthDisplay(1.3f));

        ghostGPTBlue.getComponent(AnimationRenderComponent.class).scaleEntity();

        return ghostGPTBlue;
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
        animator.addAnimation("Idle", 0.12f, Animation.PlayMode.LOOP);
        animator.addAnimation("attack", 0.06f, Animation.PlayMode.LOOP);
        animator.addAnimation("fury", 0.10f, Animation.PlayMode.LOOP);
        animator.addAnimation("die", 0.10f, Animation.PlayMode.NORMAL);

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
     * @param target        entity to chase
     * @param area          the area/space it is living in
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
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        SoundComponent soundComponent = new SoundComponent();
        deepspin.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/deepspinDamage.mp3");
        soundComponent.registerSound("death", "sounds/deepspinDeath.mp3");

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
     * Creates DeepspinRed enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the DeepSpin
     * @return entity
     */
    public static Entity createDeepspinRed(Entity target, GameArea area, float scalingFactor) {
        Entity deepspinRed = createBaseNPC(target);
        DeepspinRedConfig config = configs.deepSpinRed;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/DeepspinRed.atlas", TextureAtlas.class));
        animator.setDisposeAtlas(false);
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        SoundComponent soundComponent = new SoundComponent();
        deepspinRed.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/deepspinDamage.mp3");
        soundComponent.registerSound("death", "sounds/deepspinDeath.mp3");

        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
                        .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f)));

        // Get player's inventory for reward system
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        CombatStatsComponent deepspinRedStats = new CombatStatsComponent((int) (config.health * scalingFactor));
        WeaponsStatsComponent deepspinRedAttack = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        deepspinRed
                .addComponent(deepspinRedStats)
                .addComponent(deepspinRedAttack)
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, deepspinRedAttack))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay())
                .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

        deepspinRed.getComponent(AnimationRenderComponent.class).scaleEntity();

        return deepspinRed;
    }

    /**
     * Creates DeepspinBlue enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the DeepSpin
     * @return entity
     */
    public static Entity createDeepspinBlue(Entity target, GameArea area, float scalingFactor) {
        Entity deepspinBlue = createBaseNPC(target);
        DeepspinBlueConfig config = configs.deepSpinBlue;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/DeepspinBlue.atlas", TextureAtlas.class));
        animator.setDisposeAtlas(false);
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        SoundComponent soundComponent = new SoundComponent();
        deepspinBlue.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/deepspinDamage.mp3");
        soundComponent.registerSound("death", "sounds/deepspinDeath.mp3");

        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTSlowChaseTask(target, 10, new Vector2(0.3f, 0.3f)))
                        .addTask(new GPTFastChaseTask(target, 10, new Vector2(1.2f, 1.2f)));

        // Get player's inventory for reward system
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        CombatStatsComponent deepspinBlueStats = new CombatStatsComponent((int) (config.health * scalingFactor));
        WeaponsStatsComponent deepspinBlueAttack = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        deepspinBlue
                .addComponent(deepspinBlueStats)
                .addComponent(deepspinBlueAttack)
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, deepspinBlueAttack))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay())
                .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

        deepspinBlue.getComponent(AnimationRenderComponent.class).scaleEntity();

        return deepspinBlue;
    }

    /**
     * Creates GrokDroid enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
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
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        SoundComponent soundComponent = new SoundComponent();
        grokDroid.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/grokDamage.mp3");
        soundComponent.registerSound("death", "sounds/grokDeath.mp3");

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
     * Creates GrokDroidRed enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the GrokDroid
     * @return entity
     */
    public static Entity createGrokDroidRed(Entity target, GameArea area, float scalingFactor) {
        Entity grokDroidRed = createBaseNPC(target);
        GrokDroidRedConfig config = configs.grokDroidRed;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/GrokdroidRed.atlas", TextureAtlas.class));
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

        CombatStatsComponent grokDroidRedStats = new CombatStatsComponent((int) (config.health * scalingFactor));
        WeaponsStatsComponent grokDroidRedWeapon = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        grokDroidRed
                .addComponent(grokDroidRedStats)
                .addComponent(grokDroidRedWeapon)
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, grokDroidRedWeapon))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay(0.3f))
                .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

        grokDroidRed.getComponent(AnimationRenderComponent.class).scaleEntity();

        return grokDroidRed;
    }

    /**
     * Creates GrokDroidBlue enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the GrokDroid
     * @return entity
     */
    public static Entity createGrokDroidBlue(Entity target, GameArea area, float scalingFactor) {
        Entity grokDroidBlue = createBaseNPC(target);
        GrokDroidBlueConfig config = configs.grokDroidBlue;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/GrokdroidBlue.atlas", TextureAtlas.class));
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

        CombatStatsComponent grokDroidBlueStats = new CombatStatsComponent((int) (config.health * scalingFactor));
        WeaponsStatsComponent grokDroidBlueWeapon = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        grokDroidBlue
                .addComponent(grokDroidBlueStats)
                .addComponent(grokDroidBlueWeapon)
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, grokDroidBlueWeapon))
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay(0.3f))
                .addComponent(new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER)); // Add the ability to fire projectiles

        grokDroidBlue.getComponent(AnimationRenderComponent.class).scaleEntity();

        return grokDroidBlue;
    }

    /**
     * Creates a Vroomba entity.
     *
     * @param target        entity to chase
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
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        // Ground chase: set X only; gravity handles Y (Box2D). See Box2D Manual (Forces/Impulses).
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                        .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f));


        SoundComponent soundComponent = new SoundComponent();
        vroomba.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/vroombaDamage.mp3");
        soundComponent.registerSound("death", "sounds/vroombaDeath.mp3");

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
     * Creates a VroombaRed entity.
     *
     * @param target        entity to chase
     * @param scalingFactor The scale of increase in health and attack of the Vroomba
     * @return entity
     */
    public static Entity createVroombaRed(Entity target, float scalingFactor) {
        // Ground enemy build: dynamic body with collider/hitbox; no PhysicsMovementComponent
        Entity vroombaRed = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
        PhysicsUtils.setScaledCollider(vroombaRed, 0.9f, 0.4f);

        VroombaRedConfig config = configs.vroombaRed;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/VroombaRed.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        // Ground chase: set X only; gravity handles Y (Box2D). See Box2D Manual (Forces/Impulses).
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                        .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f));


        SoundComponent soundComponent = new SoundComponent();
        vroombaRed.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/vroombaDamage.mp3");
        soundComponent.registerSound("death", "sounds/vroombaDeath.mp3");

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

        vroombaRed
                .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
                .addComponent(new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor)))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory))
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(new VroombaSuicideComponent(target, triggerRadius, damageRadius, boomDamage, fuseSeconds))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay());

        vroombaRed.getComponent(AnimationRenderComponent.class).scaleEntity();

        return vroombaRed;
    }

    /**
     * Creates a VroombaBlue entity.
     *
     * @param target        entity to chase
     * @param scalingFactor The scale of increase in health and attack of the Vroomba
     * @return entity
     */
    public static Entity createVroombaBlue(Entity target, float scalingFactor) {
        // Ground enemy build: dynamic body with collider/hitbox; no PhysicsMovementComponent
        Entity vroombaBlue = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
        PhysicsUtils.setScaledCollider(vroombaBlue, 0.9f, 0.4f);

        VroombaBlueConfig config = configs.vroombaBlue;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/VroombaBlue.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        // Ground chase: set X only; gravity handles Y (Box2D). See Box2D Manual (Forces/Impulses).
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new GPTGroundSlowChaseTask(target, 10, 0.3f, 15f))
                        .addTask(new GPTGroundFastChaseTask(target, 10, 1.2f));


        SoundComponent soundComponent = new SoundComponent();
        vroombaBlue.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/vroombaDamage.mp3");
        soundComponent.registerSound("death", "sounds/vroombaDeath.mp3");

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

        vroombaBlue
                .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor)))
                .addComponent(new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor)))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new EnemyDeathRewardComponent(30, playerInventory))
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(new VroombaSuicideComponent(target, triggerRadius, damageRadius, boomDamage, fuseSeconds))
                .addComponent(aiComponent)
                .addComponent(new EnemyHealthDisplay());

        vroombaBlue.getComponent(AnimationRenderComponent.class).scaleEntity();

        return vroombaBlue;
    }

    /**
     * Creates Turret enemy type
     *
     * @param target        entity to chase
     * @param area          the area/space it is living in
     * @param scalingFactor The scale of increase in health and attack of the Turret
     * @return entity
     */
    public static Entity createTurret(Entity target, GameArea area, float scalingFactor) {
        Entity turret = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC))
                .addComponent(new TouchAttackComponent(PhysicsLayer.PLAYER, 1.5f));
        PhysicsUtils.setScaledCollider(turret, 0.9f, 0.4f);

        TurretConfig config = configs.turret;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService()
                                .getAsset("images/Turret.atlas", TextureAtlas.class));
        animator.setDisposeAtlas(false);
        animator.addAnimation("angry_float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 0.1f, Animation.PlayMode.LOOP);
        animator.addAnimation("damage_taken", 0.1f, Animation.PlayMode.NORMAL);

        SoundComponent soundComponent = new SoundComponent();
        turret.addComponent(soundComponent);

        soundComponent.registerSound("damageTaken", "sounds/turretDamage.mp3");
        soundComponent.registerSound("death", "sounds/turretDeath.mp3");

        ProjectileLauncherComponent projComp = new ProjectileLauncherComponent(area, target, Projectiles.GHOSTGPT_LASER);
        // Has 0 speed due to stationary ememy
        AITaskComponent aiComponent =
                new AITaskComponent()
                        .addTask(new TurretIdleTask(target, 10))
                        .addTask(new TurretFiringTask(target, 10, projComp, turret,
                                3f, 3f, 5, 0.15f));

        // Get player's inventory for reward system
        InventoryComponent playerInventory = null;
        if (target != null) {
            playerInventory = target.getComponent(InventoryComponent.class);
        }

        WeaponsStatsComponent turretStats = new WeaponsStatsComponent((int) (config.baseAttack * scalingFactor));

        turret
                .addComponent(turretStats)
                .addComponent(new CombatStatsComponent((int) (config.health * scalingFactor), 1f))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new LowHealthAttackBuffComponent(10, turretStats))
                .addComponent(new EnemyDeathRewardComponent(15, playerInventory)) // Add reward + particles
                .addComponent(new DeathParticleSpawnerComponent("explosion_2"))
                .addComponent(aiComponent)
                .addComponent(projComp) // Add the ability to fire projectiles
                .addComponent(new EnemyHealthDisplay(1.3f));

        turret.getComponent(AnimationRenderComponent.class).scaleEntity();

        return turret;
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
}
