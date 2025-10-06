package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Command to kill entities.
 * Usage:
 *   kill @p   -> kills the player (shows death screen)
 *   kill @a   -> kills all enemies (not the player)
 */
public class KillCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(KillCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        if (args == null || args.isEmpty()) {
            logger.warn("kill: missing selector. Use: kill @p | kill @a");
            return false;
        }

        String selector = args.getFirst();
        return switch (selector) {
            case "@p" -> killPlayer();
            case "@a" -> killAllEnemies();
            default -> {
                logger.warn("kill: unknown selector '{}'. Use @p or @a", selector);
                yield false;
            }
        };
    }

    private boolean killPlayer() {
        Entity player = ServiceLocator.getPlayer();
        if (player == null) {
            logger.warn("kill @p: player entity not available from ServiceLocator");
            return false;
        }

        CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
        if (stats == null) {
            logger.warn("kill @p: player has no CombatStatsComponent");
            return false;
        }

        logger.debug("kill @p: setting player health to 0");
        stats.setHealth(0); // triggers normal death flow
        return true;
    }

    private boolean killAllEnemies() {
        EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("kill @a: no EntityService registered");
            return false;
        }

        Array<Entity> entities = es.getEntities();
        if (entities == null || entities.size == 0) {
            logger.debug("kill @a: no entities present");
            return true; // nothing to kill
        }

        Entity player = ServiceLocator.getPlayer();
        int killed = 0;

        for (int i = 0; i < entities.size; i++) {
            Entity e = entities.get(i);

            HitboxComponent hb = e.getComponent(HitboxComponent.class);
            CombatStatsComponent stats = e.getComponent(CombatStatsComponent.class);

            boolean isEnemy = hb != null && hb.getLayer() == PhysicsLayer.NPC;
            boolean isAlive = stats != null && stats.getHealth() > 0;

            if (e != player && isEnemy && isAlive) {
                stats.setHealth(0);
                killed++;
            }
        }

        logger.debug("kill @a: killed {} enemies", killed);
        return true;
    }
}
