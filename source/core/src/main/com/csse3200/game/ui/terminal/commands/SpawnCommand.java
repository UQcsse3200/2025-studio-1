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
 * Spawns 1 enemy by name.
 * Usage: spawn <EnemyName>
 */
public class SpawnCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(SpawnCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        GameArea ga = ServiceLocator.getGameArea();
        if (ga == null) {
            logger.warn("SpawnCommand: no active GameArea; cannot spawn");
            return false;
        }

        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("SpawnCommand: EntityService not registered; cannot spawn");
            return false;
        }

        // Java 21: List#getFirst(); also guard against missing/blank argument
        if (args == null || args.isEmpty() || args.getFirst() == null || args.getFirst().isBlank()) {
            logger.warn("SpawnCommand: missing enemy name (usage: spawn <EnemyName>)");
            return false;
        }
        final String enemyName = args.getFirst().trim();

        // Find a player: must have both CombatStats and Stamina
        Entity player = null;
        var entities = es.getEntities();
        for (int i = 0, n = entities.size; i < n; i++) {
            var e = entities.get(i);
            var stats = e.getComponent(CombatStatsComponent.class);
            var hasStamina = e.getComponent(StaminaComponent.class) != null;
            if (stats != null && hasStamina) {
                player = e;
                break;
            }
        }

        if (player == null) {
            logger.warn("SpawnCommand: no suitable player entity found");
            return false;
        }

        ga.spawn(enemyName, ga.getRoomName(), 1, 1f, player);
        logger.info("SpawnCommand: Enemy '{}' spawned", enemyName);
        return true;
    }
}
