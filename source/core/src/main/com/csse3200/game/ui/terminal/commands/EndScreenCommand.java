package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for switching to the Win Screen or other end screens.
 */
public record EndScreenCommand(GdxGame game, GdxGame.ScreenType screenType) implements Command {
    private static final Logger logger = LoggerFactory.getLogger(EndScreenCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        logger.info("Switching to {}", screenType);
        game.setScreen(screenType);
        return true;
    }
}
