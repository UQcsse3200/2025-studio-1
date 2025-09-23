package com.csse3200.game.components.difficultymenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.mainmenu.MainMenuActions;
import com.csse3200.game.services.ServiceLocator;

import java.security.spec.ECPrivateKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events triggered in the difficulty menu screen, sets
 * the difficulty accordingly
 */
public class DifficultyMenuActions extends Component {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuActions.class);
    private final GdxGame game;
    private DifficultyType diffType;

    public DifficultyMenuActions(GdxGame game) {
        this.game = game;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("setDiffEasy", this::diffEasy);
        entity.getEvents().addListener("setDiffNormal", this::diffNormal);
        entity.getEvents().addListener("setDiffHard", this::diffHard);
        entity.getEvents().addListener("setDiffInsane", this::diffInsane);
        entity.getEvents().addListener("applyDiff", this::applyDiff);
    }

    /**
     * Swaps difficulty to Easy.
     */
    private void diffEasy() {
        logger.info("Difficulty set to Easy");
        this.diffType = DifficultyType.EASY;
    }

    /**
     * Swaps difficulty to Normal.
     */
    private void diffNormal() {
        logger.info("Difficulty set to Normal");
        this.diffType = DifficultyType.NORMAL;
    }

    /**
     * Swaps difficulty to Hard.
     */
    private void diffHard() {
        logger.info("Difficulty set to Hard");
        this.diffType = DifficultyType.HARD;
    }

    /**
     * Swaps difficulty to Insane.
     */
    private void diffInsane() {
        logger.info("Difficulty set to Insane");
        this.diffType = DifficultyType.INSANE;
    }

    /**
     * Applies the difficulty to service locator
     * and then switches back to main menu
     */
    private void applyDiff() {
        logger.info("Difficulty Set");
        ServiceLocator.registerDifficulty(new Difficulty(this.diffType));
        game.setScreen(ScreenType.MAIN_MENU);
    }

    /**
     * Intended for loading a saved game state.
     * Load functionality is not actually implemented.
     */
    private void onLoad() {
        logger.info("Load game");
        game.setScreen(GdxGame.ScreenType.LOAD_GAME);
    }
}