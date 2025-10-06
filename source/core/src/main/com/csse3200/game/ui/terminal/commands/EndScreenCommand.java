package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.Screen;
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
public record EndScreenCommand(
        GdxGame game,
        GdxGame.ScreenType screenType,
        CountdownTimerService timer
) implements Command {
    private static final Logger logger = LoggerFactory.getLogger(EndScreenCommand.class);

    @Override
    public boolean action(ArrayList<String> args) {
        logger.info("Switching to {}", screenType);
        long elapsedSeconds = (timer.getDuration() - timer.getRemainingMs()) / 1000;

        switch (screenType) {
            case DEATH_SCREEN, WIN_SCREEN -> {
                // Route by enum (no GL objects created here)
                game.setScreen(screenType);
                // If the router has created the screen already, update its time.
                applyElapsedToCurrent(elapsedSeconds);
                return true;
            }
            default -> {
                logger.debug("Unrecognised argument received for 'Screen' command: {}", args);
                return false;
            }
        }
    }

    private void applyElapsedToCurrent(long elapsedSeconds) {
        try {
            Screen current = game.getScreen();
            switch (current) {
                case WinScreen win -> win.updateTime(elapsedSeconds);
                case DeathScreen death -> death.updateTime(elapsedSeconds);
                default -> logger.debug("Current screen not time-updatable: {}", current);
            }
        } catch (Exception e) {
            // Be defensive in tests/mocks where getScreen() may be null or not set up
            logger.debug("Could not apply elapsed time to current screen", e);
        }
    }
}
