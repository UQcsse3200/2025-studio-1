package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling whether a player takes damage
 */
public class InfiniteStaminaCommand implements Command {
    private Array<Entity> entityList;
    private static final Logger logger = LoggerFactory.getLogger(DebugCommand.class);


    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'infinite_stamina' command: {}", args);
            return false;
        }

        String arg = args.get(0);
        entityList = ServiceLocator.getEntityService().getEntities();
        switch (arg) {
            case "on":
                setInfiniteStaminaStatus(entityList, true);
                return true;
            case "off":
                setInfiniteStaminaStatus(entityList, false);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'infinite_stamina' command: {}", args);
                return false;
        }
    }

    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    void setInfiniteStaminaStatus(Array<Entity> entityList, boolean status) {
        for(Entity entity : entityList) {
            if (entity.getComponent(PlayerActions.class) != null) {
                entity.getComponent(PlayerActions.class).infStamina();
            }
        }
    }
}
