package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Array;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling boosted outgoing weapon damage (cheat code).
 */
public class DamageMultiplierCommand implements Command {

    private static final Logger logger = LoggerFactory.getLogger(DamageMultiplierCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'damageMultiplier' command: {}", args);
            return false;
        }

        String arg = args.getFirst();
        Array<Entity> entityList = ServiceLocator.getEntityService().getEntities();

        return switch (arg.toLowerCase()) {
            case "on" -> {
                setDamageMultiplierStatus(entityList, true);
                yield true;
            }
            case "off" -> {
                setDamageMultiplierStatus(entityList, false);
                yield true;
            }
            default -> {
                logger.debug("Unrecognised argument received for 'damageMultiplier' command: {}", args);
                yield false;
            }
        };
    }

    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    void setDamageMultiplierStatus(Array<Entity> entityList, boolean status) {
        for (int i = 0; i < entityList.size; i++) {
            Entity entity = entityList.get(i);
            WeaponsStatsComponent comp = entity.getComponent(WeaponsStatsComponent.class);
            if (comp != null) {
                comp.setDamageBoostEnabled(status);
                logger.info("Damage multiplier set to {} for entity {}", status, entity);
            }
        }
    }
}