package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling whether a player takes damage
 */
public class DisableDamageCommand implements Command {
    private ArrayList<Entity> EntityList;


    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'disable_damage' command: {}", args);
            return false;
        }

        String arg = args.get(0);
        EntityList = ServiceLocator.getEntityService().getEntities();
        switch (arg) {
            case "on":
                setDisableDamageStatus(EntityList, true);
                return true;
            case "off":
                setDisableDamageStatus(EntityList, false);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'disable_damage' command: {}", args);
                return false;
        }
    }

    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    void setDisableDamageStatus(ArrayList<Entity> EntityList, boolean status) {
        for(Entity entity : EntityList) {
            if (entity.getComponent(PlayerActions.class)
                    && entity.getComponent(CombatStatsComponent.class)) {
                entity.getComponent(CombatStatsComponent.class).setDisableDamageStatus(status);
            }
        }
    }
}