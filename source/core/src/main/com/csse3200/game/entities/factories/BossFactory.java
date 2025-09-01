package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.enemy.BossChargeSkillComponent;
import com.csse3200.game.components.enemy.BlackholeAttackComponent;
import com.csse3200.game.components.npc.GhostAnimationController;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.enemy.FireballAttackComponment;
import com.csse3200.game.components.enemy.FireballMovementComponent;
import com.csse3200.game.components.enemy.BlackholeComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.BaseEntityConfig;
import com.csse3200.game.entities.configs.NPCConfigs;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;



public class BossFactory {
    private static final NPCConfigs configs =
            FileLoader.readClass(NPCConfigs.class, "configs/NPCs.json");
    //new added boss2
    public static Entity createBoss2(Entity target) {
        Entity boss2 = createBaseNPC(target);
        BaseEntityConfig config = configs.boss2;

        AnimationRenderComponent animator =
                new AnimationRenderComponent(
                        ServiceLocator.getResourceService().getAsset("images/robot-2.atlas", TextureAtlas.class));
        animator.addAnimation("angry_float", 1.5f, Animation.PlayMode.LOOP);
        animator.addAnimation("float", 1.5f, Animation.PlayMode.LOOP);

        boss2
                .addComponent(new CombatStatsComponent(config.health, config.baseAttack))
                .addComponent(animator)
                .addComponent(new GhostAnimationController())
                .addComponent(new FireballAttackComponment(target, 1.5f, 8f, 6f, config.baseAttack + 2))
                .addComponent(new BossChargeSkillComponent(target, 6f, 5f, 0.4f, 12f, 0.6f, 1.5f))
                .addComponent(new BlackholeComponent(target,7f,8f))
                .addComponent(new FireballAttackComponment(target, 1.5f, 8f, 6f, config.baseAttack + 2));
        boss2.getComponent(AnimationRenderComponent.class).scaleEntity();

        return boss2;
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
                .addComponent(new PhysicsMovementComponent())
                .addComponent(new FireballMovementComponent(velocity))
                .addComponent(new ColliderComponent())
                .addComponent(new HitboxComponent().setLayer(PhysicsLayer.NPC));
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
    static Entity createBaseNPC(Entity target) {
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