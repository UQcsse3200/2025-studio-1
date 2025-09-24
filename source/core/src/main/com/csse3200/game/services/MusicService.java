package com.csse3200.game.services;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.files.UserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles background music for the menu
 */
public class MusicService {
    private static final Logger logger = LoggerFactory.getLogger(MusicService.class);
    private static final String MENU_MUSIC = "sounds/menuMusic.mp3";
    private Music menuMusic;

    public MusicService() {
        ServiceLocator.getGlobalEvents().addListener("screenChanged", this::updateForScreen);
    }

    public void load(ResourceService resourceService) {
        logger.info("Loading music assets");
        resourceService.loadMusic(new String[]{MENU_MUSIC});
        resourceService.loadAll();

        menuMusic = resourceService.getAsset(MENU_MUSIC, Music.class);
        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f);

            if (UserSettings.get().musicEnabled) {
                menuMusic.play();
            }
        }
    }

    public void updateForScreen(String screenType) {
        boolean musicEnabled = UserSettings.get().musicEnabled;

        boolean stopMusic = screenType.equals("MAIN_GAME")
                || screenType.equals("DEATH_SCREEN")
                || screenType.equals("WIN_SCREEN");

        if (menuMusic != null) {
            if (stopMusic && menuMusic.isPlaying()) {
                logger.debug("Stopping menu music for {}", screenType);
                menuMusic.stop();
            } else if (!stopMusic && musicEnabled && !menuMusic.isPlaying()) {
                logger.debug("Starting menu music for {}", screenType);
                menuMusic.play();
            }
        }
    }

    public void setMenuMusicPlaying(boolean play) {
        if (menuMusic != null) {
            if (play && !menuMusic.isPlaying()) {
                menuMusic.play();
            } else if (!play && menuMusic.isPlaying()) {
                menuMusic.stop();
            }
        }
    }

    public boolean isMenuMusicPlaying() {
        return menuMusic != null && menuMusic.isPlaying();
    }

    public void dispose(ResourceService resourceService) {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic = null;
        }
        if (resourceService != null) {
            resourceService.unloadAssets(new String[]{MENU_MUSIC});
        }
    }
}