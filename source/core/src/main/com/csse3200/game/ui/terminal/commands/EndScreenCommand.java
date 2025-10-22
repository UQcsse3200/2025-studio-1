package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.Screen;
import com.csse3200.game.GdxGame;
import com.csse3200.game.screens.DeathScreen;
import com.csse3200.game.screens.WinScreen;
import com.csse3200.game.services.CountdownTimerService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * A command for switching to the Win Screen or other end screens.
 *
 * @param game       The main game instance, used to change screens.
 * @param screenType The type of screen to transition to.
 * @param timer      The countdown timer service, used to calculate and display the final elapsed time.
 */
public record EndScreenCommand(
        GdxGame game,
        GdxGame.ScreenType screenType,
        CountdownTimerService timer,
        boolean won
) implements Command {
    private static final Logger logger = LoggerFactory.getLogger(EndScreenCommand.class);

    /**
     * Execute the end screen command
     * @param args Optional command arguments (unused in this command)
     * @return {@code true} if the screen transition was successful, {@code false} otherwise
     */
    @Override
    public boolean action(ArrayList<String> args) {
        logger.info("Switching to {}", screenType);
        long elapsedSeconds = (timer.getDuration() - timer.getRemainingMs()) / 1000;

        boolean won = (screenType == GdxGame.ScreenType.WIN_SCREEN);
        ServiceLocator.getGlobalEvents().trigger("round:finished", won);

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

    /**
     * Applies the calculated elapse time to the current end screen
     * @param elapsedSeconds The total number of seconds that have passed in the game
     */
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
