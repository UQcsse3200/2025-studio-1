package com.csse3200.game.services;

import com.badlogic.gdx.audio.Sound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles menu button sound clicks
 */
public class ButtonSoundService  {
    private static final Logger logger = LoggerFactory.getLogger(ButtonSoundService.class);

    private static final String CLICK_SOUND = "sounds/buttonClick.mp3";
    private Sound clickSound;

    public void load(ResourceService resourceService) {
        logger.info("Loading button sound effects");
        resourceService.loadSounds(new String[]{CLICK_SOUND});
        resourceService.loadAll();

        clickSound = resourceService.getAsset(CLICK_SOUND, Sound.class);
    }

    public void playClick() {
        if (clickSound != null) {
            clickSound.play(0.6f);
        }
    }

    public void dispose(ResourceService resourceService) {
        if (resourceService != null) {
            resourceService.unloadAssets(new String[]{CLICK_SOUND});
        }
        clickSound = null;
    }
}
