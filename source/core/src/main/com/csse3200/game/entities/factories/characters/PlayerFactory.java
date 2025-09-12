package com.csse3200.game.entities.factories.characters;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.*;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.characters.PlayerConfig;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsUtils;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.player.ItemPickUpComponent;


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
    InventoryComponent playerInventory = new InventoryComponent(stats.gold);

    //to make sure the player spawns unequipped at the start of the game
    playerInventory.equipWeapon(null);

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
            .addComponent(new CombatStatsComponent(stats.health))
            .addComponent(new WeaponsStatsComponent(stats.baseAttack))
            .addComponent(playerInventory)
            .addComponent(new ItemPickUpComponent(playerInventory))
            .addComponent(inputComponent)
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new PlayerInventoryDisplay(playerInventory))
            .addComponent(new StaminaComponent())
            .addComponent(animator)
            .addComponent(new PlayerAnimationController());

    player.getComponent(AnimationRenderComponent.class).scaleEntity(2f);
    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    PhysicsUtils.setScaledCollider(player, 0.3f,0.5f);
    player.getComponent(WeaponsStatsComponent.class).setCoolDown(0.2f);

    //Unequip player at spawn
    PlayerActions actions = player.getComponent(PlayerActions.class);
    actions.create();
    actions.unequipWeapon();  //start without a weapon equipped

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
    animator.addAnimation("right_fall", 0.1f, Animation.PlayMode.NORMAL);
    animator.addAnimation("left_fall", 0.1f, Animation.PlayMode.NORMAL);
    animator.startAnimation("right_stand");
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
