package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling whether a player has infinite jumps
 */
public class InfiniteJumpsCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DebugCommand.class);
    private Array<Entity> entityList;

    /**
     * Executes the infinite jump command based on the provided arguments.
     * @param args List of arguments; must contain exactly one element ("on" or "off")
     * @return {@code true} if the screen transition was successful, {@code false} otherwise
     */
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'infinite_jumps' command: {}", args);
            return false;
        }

        String arg = args.get(0);
        entityList = ServiceLocator.getEntityService().getEntities();
        switch (arg) {
            case "on":
                setInfiniteJumpsStatus(entityList, true);
                return true;
            case "off":
                setInfiniteJumpsStatus(entityList, false);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'infinite_jumps' command: {}", args);
                return false;
        }
    }

    /**
     * Validates that the argument list contains exactly one argument.
     * @param args List of arguments
     * @return {@code true} if the arguments are valid, false otherwise
     */
    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }

    /**
     * Iterates over all entities and updates the infinite jump
     * status for entities that have a {@link PlayerActions} component.
     * @param entityList List of entities to check
     * @param status {@code true} to enable infinite dash, false to disable
     */
    void setInfiniteJumpsStatus(Array<Entity> entityList, boolean status) {
        for (Entity entity : entityList) {
            if (entity.getComponent(PlayerActions.class) != null) {
                entity.getComponent(PlayerActions.class).infJumps();
            }
        }
    }
}
