package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Spawns 1 enemy name listed
 * Usage: spawn
 */
public class SpawnCommand implements Command {
  private static final Logger logger = LoggerFactory.getLogger(SpawnCommand.class);

  @Override
  public boolean action(ArrayList<String> args) {
    GameArea ga = ServiceLocator.getGameArea();
    if (ga == null) {
      logger.warn("SpawnCommand: Current GameArea is not a ForestGameArea; cannot start waves");
      return false;
    }
    Entity player = null;

    EntityService es = ServiceLocator.getEntityService();
    if (es == null) {
      logger.debug("No EntityService registered; cannot kill enemy");
      return false;
    }

    for (Entity e : es.getEntities()) {
      CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);
      if (stats == null || !isPlayer(e)) {
        continue;
      }
      player = e;
      break;
    }

    if (player != null) {
      ga.spawn(args.get(0), ga.getRoomName(), 1, 1, player);

      logger.info("SpawnCommand: Enemy Spawned");
      return true;
    }
    return false;
  }

  private boolean isPlayer(Entity e) {
    return e.getComponent(StaminaComponent.class) != null;
  }
}

