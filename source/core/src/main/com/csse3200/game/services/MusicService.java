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
    private static final String FOREST_MUSIC = "sounds/forestmusic.mp3";
    private Music menuMusic;
    private Music forestMusic;

    public void load(ResourceService resourceService) {
        logger.info("Loading music assets");

        resourceService.loadMusic(new String[]{MENU_MUSIC, FOREST_MUSIC});
        resourceService.loadAll();

        menuMusic = resourceService.getAsset(MENU_MUSIC, Music.class);
        forestMusic = resourceService.getAsset(FOREST_MUSIC, Music.class);

        if (menuMusic != null) {
            menuMusic.setLooping(true);
            menuMusic.setVolume(0.5f);

            if (UserSettings.get().isMusicEnabled()) {
                menuMusic.play();
            }
        }
        if (forestMusic != null) {
            forestMusic.setLooping(true);
            forestMusic.setVolume(0.3f);
        }
    }

    public void updateForScreen(String screenType) {
        boolean musicEnabled = UserSettings.get().isMusicEnabled();

        boolean stopMusic = screenType.equals("MAIN_GAME")
                || screenType.equals("DEATH_SCREEN")
                || screenType.equals("WIN_SCREEN")
                || screenType.equals("LEADERBOARD");

        if (menuMusic != null) {
            if (stopMusic && menuMusic.isPlaying()) {
                logger.debug("Stopping menu music for {}", screenType);
                menuMusic.stop();
            } else if (!stopMusic && musicEnabled && !menuMusic.isPlaying()) {
                logger.debug("Starting menu music for {}", screenType);
                menuMusic.play();
            }
        }

        boolean stopForestMusic = !screenType.equals("MAIN_GAME");

        if (forestMusic != null) {
            if (stopForestMusic && forestMusic.isPlaying()) {
                forestMusic.stop();
            } else if (!stopForestMusic && musicEnabled && !forestMusic.isPlaying()) {
                forestMusic.play();
            }
        }
    }

    public boolean isMenuMusicPlaying() {
        return menuMusic != null && menuMusic.isPlaying();
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

    public void playForestMusic() {
        if (forestMusic != null && UserSettings.get().isMusicEnabled() && !forestMusic.isPlaying()) {
            forestMusic.play();
        }
    }

    public void resetForestMusic() {
        if (forestMusic != null) {
            forestMusic.stop();
            forestMusic.setPosition(0);
            if (UserSettings.get().isMusicEnabled()) {
                forestMusic.play();
            }
        }
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
