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

    /**
     * Executes the "pickupAll" command.
     * <p>
     *  Finds the player entity and triggers the "pickupAll" event.
     * @param args Optional command arguments (unused in this command)
     * @return {@code true} if the player entity was found and event triggered, {@code false} otherwise
     */
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

    /**
     * Searches all entities for the player entity
     * @return The player entity if found, or null otherwise
     */
    private Entity findPlayer() {
        var es = ServiceLocator.getEntityService();
        if (es == null) {
            logger.debug("pickupAll: no EntityService registered");
            return null;
        }

        Array<Entity> entities = es.getEntities();
        if (entities == null) return null;

        for (Entity e : entities) {
            if (e.getComponent(KeyboardPlayerInputComponent.class) != null) {
                return e;
            }
        }
        return null;
    }
}