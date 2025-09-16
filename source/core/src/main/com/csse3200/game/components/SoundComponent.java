package com.csse3200.game.components;


import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sound handler for entities. Provides a component to register sounds with an event.
 * Once these events are registered, the associated sound will play every time the event is triggered.
 */
public class SoundComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(Component.class);

    /**
     * Registers the sound to play when the given event is triggered. Returns true if successful and false if not.
     * @param event the name of the event
     * @param sound the path of the sound file to play
     * @requires sound has already been registered with the resourceService
     * @returns whether the sound was successfully registered
     */
    public boolean registerSound(String event, String sound) {
        if (this.entity == null) {
            logger.debug("Component must be attached to enemy before registering a sound.");
            return false;
        } else if (ServiceLocator.getResourceService().getAsset(sound, Sound.class) == null) {
            logger.debug("Sound must be registered with the resource service before registration.");
            return false;
        }
        this.entity.getEvents().addListener(
                event,
                () -> {ServiceLocator.getResourceService().getAsset(sound, Sound.class).play();}
        );
        return true;
    }
}
