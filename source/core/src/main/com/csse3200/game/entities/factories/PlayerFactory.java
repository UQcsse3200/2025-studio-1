package com.csse3200.game.entities.factories;

import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.player.TouchPlayerInputComponent;
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

public class PlayerFactory {
  private static final PlayerConfig stats =
          FileLoader.readClass(PlayerConfig.class, "configs/player.json");

  /**
   * Create a player entity with default input.
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
                    .addComponent(new PlayerStatsDisplay());

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(TextureRenderComponent.class).scaleEntity();
    return player;
  }

  /**
   * Create a player entity that uses arrow keys for movement.
   */
  public static Entity createPlayerWithArrowKeys() {
    InputComponent inputComponent = new TouchPlayerInputComponent();

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
                    .addComponent(new PlayerStatsDisplay());

    PhysicsUtils.setScaledCollider(player, 0.6f, 0.3f);
    player.getComponent(ColliderComponent.class).setDensity(1.5f);
    player.getComponent(TextureRenderComponent.class).scaleEntity();

    PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
    if (physics != null) {
      for (Fixture fixture : physics.getBody().getFixtureList()) {
        Filter filter = fixture.getFilterData();
        filter.maskBits = PhysicsLayer.WALL | PhysicsLayer.GATE;
        fixture.setFilterData(filter);
      }
    }
    return player;
  }

  private PlayerFactory() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
