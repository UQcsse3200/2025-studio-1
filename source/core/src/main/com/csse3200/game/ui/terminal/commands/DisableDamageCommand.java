package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Null;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.components.CombatStatsComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import com.badlogic.gdx.utils.Array;

/**
 * A command for toggling whether a player takes damage
 */
public class DisableDamageCommand implements Command {
    private Array<Entity> entityList;
    private static final Logger logger = LoggerFactory.getLogger(DebugCommand.class);


    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'disable_damage' command: {}", args);
            return false;
        }

        String arg = args.get(0);
        entityList = ServiceLocator.getEntityService().getEntities();
        switch (arg) {
            case "on":
                setDisableDamageStatus(entityList, true);
                return true;
            case "off":
                setDisableDamageStatus(entityList, false);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'disable_damage' command: {}", args);
                return false;
        }
    }

    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    void setDisableDamageStatus(Array<Entity> entityList, boolean status) {
        for(Entity entity : entityList) {
            if (entity.getComponent(PlayerActions.class) != null
                    && entity.getComponent(WeaponsStatsComponent.class) != null) {
                entity.getComponent(WeaponsStatsComponent.class).setDisableDamage(status);
            }
        }
    }
}