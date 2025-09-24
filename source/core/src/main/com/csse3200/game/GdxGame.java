package com.csse3200.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.screens.*;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
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
    private static final String MENU_MUSIC = "sounds/menuMusic.mp3";
    private Music menuMusic;

    @Override
    public void create() {
        logger.info("Creating game");
        loadSettings();

        // Sets background to light yellow
        Gdx.gl.glClearColor(248f / 255f, 249 / 255f, 178 / 255f, 1);

        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.getResourceService().loadMusic(new String[]{MENU_MUSIC});
        ServiceLocator.getResourceService().loadAll();

        menuMusic = ServiceLocator.getResourceService().getAsset(MENU_MUSIC, Music.class);
        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f);
            menuMusic.play();
            logger.info("Menu music started");
        } else {
            logger.warn("Menu music asset not loaded!");
        }

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

        if (screenType == ScreenType.MAIN_GAME
                || screenType == ScreenType.DEATH_SCREEN
                || screenType == ScreenType.WIN_SCREEN) {
            if (menuMusic != null && menuMusic.isPlaying()) {
                logger.info("Stopping menu music for {}", screenType);
                menuMusic.stop();
            }
        } else {
            // play menu music if we're in a menu-type screen
            if (menuMusic != null && !menuMusic.isPlaying()) {
                logger.info("Starting menu music for {}", screenType);
                menuMusic.play();
            }
        }

        Screen currentScreen = getScreen();
        if (currentScreen != null) {
            currentScreen.dispose();
        }
        setScreen(newScreen(screenType));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing of current screen");

        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
        }
        ServiceLocator.getResourceService().unloadAssets(new String[]{MENU_MUSIC});

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
            case MAIN_GAME -> new MainGameScreen(this);
            case SETTINGS -> new SettingsScreen(this);
            case DEATH_SCREEN -> new DeathScreen(this);
            case WIN_SCREEN -> new WinScreen(this);
            case LOAD_GAME -> new MainGameScreen(this, "placeholder");
            case TUTORIAL_SCREEN -> new TutorialScreen(this);
            case STORY -> new StoryScreen(this);
        };
    }

    public enum ScreenType {
        MAIN_MENU, MAIN_GAME, SETTINGS, DEATH_SCREEN, WIN_SCREEN, TUTORIAL_SCREEN,
        STORY, LOAD_GAME
    }

    /**
     * Exit the game.
     */
    public void exit() {
        app.exit();
    }
}
