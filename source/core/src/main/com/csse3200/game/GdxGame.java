package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.*;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.MusicService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;
import com.csse3200.game.tutorial.TutorialGameScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.Gdx.app;

/**
 * Entry point of the non-platform-specific game logic. Controls which screen is currently running.
 * The current screen triggers transitions to other screens. This works similarly to a finite state
 * machine (See the State Pattern).
 */
public class GdxGame extends Game {
    private static final Logger logger = LoggerFactory.getLogger(GdxGame.class);

    private LeaderBoardManager carryOverLeaderBoard;

    public LeaderBoardManager getCarryOverLeaderBoard() {
        return carryOverLeaderBoard;
    }

    public void setCarryOverLeaderBoard(LeaderBoardManager lbm) {
        this.carryOverLeaderBoard = lbm;
    }

    @Override
    public void create() {
        logger.info("Creating game");
        loadSettings();

        // Sets background to light yellow
        Gdx.gl.glClearColor(248f / 255f, 249 / 255f, 178 / 255f, 1);

        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerResourceService(resourceService);

        MusicService musicService = new MusicService();
        ServiceLocator.registerMusicService(musicService);

        ButtonSoundService buttonSoundService = new ButtonSoundService();
        ServiceLocator.registerButtonSoundService(buttonSoundService);

        ServiceLocator.getGlobalEvents().addListener("screenChanged", musicService::updateForScreen);

        musicService.load(resourceService);
        buttonSoundService.load(resourceService);
        setScreen(ScreenType.MAIN_MENU);
    }

    /**
     * Loads the game's settings.
     */
    private void loadSettings() {
        logger.debug("Loading game settings");
        UserSettings.Settings settings = UserSettings.get();
        UserSettings.applySettings(settings);
    }

    /**
     * Sets the game's screen to a new screen of the provided type.
     *
     * @param screenType screen type
     */
    public void setScreen(ScreenType screenType) {
        logger.info("Setting game screen to {}", screenType);
        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        setScreen(newScreen(screenType));
        ServiceLocator.getGlobalEvents().trigger("screenChanged", screenType.name());
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of current screen");
        getScreen().dispose();
    }

    /**
     * Create a new screen of the provided type.
     *
     * @param screenType screen type
     * @return new screen
     */
    private Screen newScreen(ScreenType screenType) {
        return switch (screenType) {
            case MAIN_MENU -> new MainMenuScreen(this);
            case MAIN_GAME -> new MainGameScreen(this, false);
            case SETTINGS -> new SettingsScreen(this);
            case DEATH_SCREEN -> new DeathScreen(this);
            case WIN_SCREEN -> new WinScreen(this);
            case LOAD_GAME -> new MainGameScreen(this, true);
            case TUTORIAL_SCREEN -> new TutorialScreen(this);
            case STORY -> new StoryScreen(this);
            case CHOOSE_AVATAR -> new AvatarChoiceScreen(this);
            case DIFFICULTY_SCREEN -> new DifficultyScreen(this);
            case LEADERBOARD -> new LeaderboardScreen(this);

        };
    }

    /**
     * Exit the game.
     */
    public void exit() {
        app.exit();
    }

    public enum ScreenType {
        MAIN_MENU, MAIN_GAME, SETTINGS, DEATH_SCREEN, WIN_SCREEN, TUTORIAL_SCREEN,
        STORY, LOAD_GAME, DIFFICULTY_SCREEN, CHOOSE_AVATAR, LEADERBOARD
    }
}
