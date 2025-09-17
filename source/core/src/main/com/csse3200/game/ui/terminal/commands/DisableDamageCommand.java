package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling whether a player takes damage.
 * Usage: disable_damage on|off
 */
public class DisableDamageCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DisableDamageCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'disable_damage' command: {}", args);
            return false;
        }

        final String arg = args.getFirst().trim().toLowerCase();
        final EntityService es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.warn("disable_damage: no EntityService registered");
            return false;
        }

        final Array<Entity> entities = es.getEntities();
        if (entities == null) {
            logger.debug("disable_damage: entity list is null");
            return false;
        }

        final boolean status = arg.equals("on");
        int changed = setDisableDamageStatus(entities, status);
        logger.info("disable_damage '{}': toggled {} entity(ies)", arg, changed);
        return true;
    }

    boolean isValid(ArrayList<String> args) {
        if (args == null || args.size() != 1) return false;
        String a = args.getFirst();
        return a != null && (a.equalsIgnoreCase("on") || a.equalsIgnoreCase("off"));
    }

    /** Returns count of entities updated. */
    int setDisableDamageStatus(Array<Entity> entityList, boolean status) {
        int changed = 0;
        for (Entity entity : entityList) {
            final PlayerActions actions = entity.getComponent(PlayerActions.class);
            final WeaponsStatsComponent weapon = entity.getComponent(WeaponsStatsComponent.class);
            if (actions != null && weapon != null) {
                weapon.setDisableDamage(status);
                changed++;
            }
        }
        return changed;
    }
}
