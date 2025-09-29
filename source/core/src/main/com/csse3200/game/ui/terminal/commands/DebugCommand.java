package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for toggling debug mode on and off.
 */
public class DebugCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DebugCommand.class);

    /**
     * Toggles debug mode on or off if the corresponding argument is received.
     *
     * @param args command arguments
     */
    public boolean action(ArrayList<String> args) {
        if (!isValid(args)) {
            logger.debug("Invalid arguments received for 'debug' command: {}", args);
            return false;
        }

        String arg = args.getFirst();
        return switch (arg) {
            case "on" -> {
                ServiceLocator.getRenderService().getDebug().setActive(true);
                yield true;
            }
            case "off" -> {
                ServiceLocator.getRenderService().getDebug().setActive(false);
                yield true;
            }
            default -> {
                logger.debug("Unrecognised argument received for 'debug' command: {}", args);
                yield false;
            }
        };
    }

    /**
     * Validates the command arguments.
     *
     * @param args command arguments
     * @return is valid
     */
    boolean isValid(ArrayList<String> args) {
        return args.size() == 1;
    }
}
