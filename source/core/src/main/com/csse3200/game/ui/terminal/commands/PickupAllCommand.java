package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.KeyboardPlayerInputComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling whether a player can pick up items in the world at once.
 */
public class PickupAllCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(PickupAllCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        Entity player = findPlayer();
        if (player == null) {
            logger.debug("pickupAll: player not found");
            return false;
        }

        player.getEvents().trigger("pickupAll");
        logger.info("pickupAll cheat triggered (global)");
        return true;
    }

    private Entity findPlayer() {
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
        for (Entity e : entities) {
            if (e.getComponent(KeyboardPlayerInputComponent.class) != null) {
                return e;
            }
        }
        return null;
    }
}