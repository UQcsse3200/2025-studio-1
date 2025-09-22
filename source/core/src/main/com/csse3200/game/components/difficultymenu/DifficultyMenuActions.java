package com.csse3200.game.components.difficultymenu;

import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.mainmenu.MainMenuActions;

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
    private final DifficultyMenuDisplay menuDisplay;

    public MainMenuActions(GdxGame game, DifficultyMenuDisplay difficultyMenuDisplay) {
        this.game = game;
        this.menuDsiplay = difficultyMenuDisplay;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("setDiffEasy", this::diffEasy);
        entity.getEvents().addListener("setDiffNormal", this::diffNormal);
        entity.getEvents().addListener("setDiffHard", this::diffHard);
        entity.getEvents().addListener("setDiffInsane", this::diffInsane);
    }

    /**
     * Swaps difficulty to Easy.
     */
    private void diffEasy() {
        logger.info("Difficulty set to Easy");
        menuDisplay.setDiffType(DifficultyType.EASY);
    }

    /**
     * Swaps difficulty to Normal.
     */
    private void diffNormal() {
        logger.info("Difficulty set to Normal");
        menuDisplay.setDiffType(DifficultyType.NORMAL);
    }

    /**
     * Swaps difficulty to Hard.
     */
    private void diffHard() {
        logger.info("Difficulty set to Hard");
        menuDisplay.setDiffType(DifficultyType.HARD);
    }

    /**
     * Swaps difficulty to Insane.
     */
    private void diffInsane() {
        logger.info("Difficulty set to Insane");
        menuDisplay.setDiffType(DifficultyType.INSANE);
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