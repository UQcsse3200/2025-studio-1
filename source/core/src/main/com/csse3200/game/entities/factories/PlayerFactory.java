package com.csse3200.game.entities.factories;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.PowerupComponent;
import com.csse3200.game.components.TagComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.PlayerInventoryDisplay;
import com.csse3200.game.components.player.PlayerStatsDisplay;
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

    Entity player =
        new Entity()
            .addComponent(new TextureRenderComponent("images/box_boy_leaf.png"))
            .addComponent(new PhysicsComponent())
            .addComponent(new ColliderComponent())
            .addComponent(new HitboxComponent().setLayer(PhysicsLayer.PLAYER))
            .addComponent(new PlayerActions())
            .addComponent(new CombatStatsComponent(stats.health, stats.baseAttack))
            .addComponent(new InventoryComponent(stats.gold))
            .addComponent(inputComponent)
            .addComponent(new PlayerStatsDisplay())
            .addComponent(new PlayerInventoryDisplay())
            .addComponent(new PowerupComponent());

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(TextureRenderComponent.class).scaleEntity();
    player.getComponent(CombatStatsComponent.class).setCoolDown(0.2f);

    // pick up rapid fire powerup
    // remove this if we have item pickup available
    // (disposes entity when player go near it)
    player.addComponent(new Component() {
      @Override
      public void update() {
        var entities = ServiceLocator.getEntityService().getEntities();
        for (int i = 0; i < entities.size; i++) {
          Entity entityRapidFirePowerup = entities.get(i);
          TagComponent tag = entityRapidFirePowerup.getComponent(TagComponent.class);

          if (tag != null && tag.getTag().equals("rapidfire")) {
            if (entityRapidFirePowerup.getCenterPosition().dst(player.getCenterPosition()) < 1f) {
              CombatStatsComponent statsComp = player.getComponent(CombatStatsComponent.class);
              PowerupComponent powerup = player.getComponent(PowerupComponent.class);

              powerup.applyRapidFire(statsComp, 2f);

              entityRapidFirePowerup.dispose();
            }
          }
        }
      }
    });

    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
