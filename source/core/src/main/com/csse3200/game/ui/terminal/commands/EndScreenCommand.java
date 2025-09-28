package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import com.csse3200.game.screens.DeathScreen;
import com.csse3200.game.screens.WinScreen;
import com.csse3200.game.services.CountdownTimerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for switching to the Win Screen or other end screens.
 */
public record EndScreenCommand(GdxGame game, GdxGame.ScreenType screenType,
                               CountdownTimerService timer) implements Command {
    private static final Logger logger = LoggerFactory.getLogger(EndScreenCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        logger.info("Switching to {}", screenType);
        long elapsedSeconds = (timer.getDuration() - timer.getRemainingMs()) / 1000;
        switch (screenType) {
            case DEATH_SCREEN:
                DeathScreen deathScreen = new DeathScreen(game);
                deathScreen.updateTime(elapsedSeconds);
                game.setScreen(deathScreen);
                return true;
            case WIN_SCREEN:
                WinScreen winScreen = new WinScreen(game);
                winScreen.updateTime(elapsedSeconds);
                game.setScreen(winScreen);
                return true;
            default:
                logger.debug("Unrecognised argument received for 'Screen' command: {}", args);
                return false;
        }

    }
}
