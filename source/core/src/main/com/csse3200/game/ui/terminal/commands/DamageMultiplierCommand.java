package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling boosted outgoing weapon damage (cheat code).
 */
public class DamageMultiplierCommand implements Command {

    private Array<Entity> entityList;
    private static final Logger logger = LoggerFactory.getLogger(DamageMultiplierCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'damageMultiplier' command: {}", args);
            return false;
        }

        String arg = args.get(0);
        entityList = ServiceLocator.getEntityService().getEntities();

        switch (arg.toLowerCase()) {
            case "on":
                setDamageMultiplierStatus(entityList, true);
                return true;
            case "off":
                setDamageMultiplierStatus(entityList, false);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'damageMultiplier' command: {}", args);
                return false;
        }
    }

    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    void setDamageMultiplierStatus(Array<Entity> entityList, boolean status) {
        for (Entity entity : entityList) {
            if (entity.getComponent(WeaponsStatsComponent.class) != null) {
                entity.getComponent(WeaponsStatsComponent.class).setDamageBoostEnabled(status);
                logger.info("Damage multiplier set to {} for entity {}", status, entity);
            }
        }
    }

}