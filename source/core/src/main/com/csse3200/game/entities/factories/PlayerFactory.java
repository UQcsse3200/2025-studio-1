package com.csse3200.game.entities.factories;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerInventoryDisplay;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.components.player.PlayerAnimationController;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;


/**
 * Factory to create a player entity.
 *
 * <p>Predefined player properties are loaded from a config stored as a json file and should have
 * the properties stores in 'PlayerConfig'.
 */
public class PlayerFactory {
  private static final PlayerConfig stats =
      FileLoader.readClass(PlayerConfig.class, "configs/player.json");

  /**
   * Create a player entity.
   * @return entity
   */
  public static Entity createPlayer() {
    InputComponent inputComponent =
        ServiceLocator.getInputService().getInputFactory().createForPlayer();

    AnimationRenderComponent animator =
            new AnimationRenderComponent(
                    ServiceLocator.getResourceService()
                            .getAsset("images/player.atlas", TextureAtlas.class));
    add_animations(animator);
    Entity player =
        new Entity()
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions())
            .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
            .addComponent(new InventoryComponent(stats.gold))
            .addComponent(inputComponent)
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new PlayerInventoryDisplay())
            .addComponent(new PlayerStatsDisplay())
                .addComponent(animator)
                .addComponent(new PlayerAnimationController());

    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(AnimationRenderComponent.class).scaleEntity(2f);
    PhysicsUtils.setScaledCollider(player, 1f,1f);
    return player;
  }

  /**
   * Add player animations to animation render component.
   * @param animator animation render component for the player
   */
  private static void add_animations(AnimationRenderComponent animator) {
    animator.addAnimation("right_run", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("left_run", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("right_jump", 0.1f, Animation.PlayMode.NORMAL);
    animator.addAnimation("left_jump", 0.1f, Animation.PlayMode.NORMAL);
    animator.addAnimation("right_stand", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("left_stand", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("right_walk", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("left_walk", 0.1f, Animation.PlayMode.LOOP);
    animator.addAnimation("right_crouch", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("left_crouch", 0.2f, Animation.PlayMode.LOOP);
    animator.addAnimation("right_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
    animator.addAnimation("left_stand_crouch", 0.1f, Animation.PlayMode.NORMAL);
    animator.startAnimation("right_stand");
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
