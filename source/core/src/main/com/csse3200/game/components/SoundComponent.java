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
     * Registers the sound to play when the given event is triggered.
     * @param event the name of the event
     * @param sound the path of the sound file to play
     */
    public void registerSound(String event, String sound) {
        if (this.entity == null) {
            logger.debug("Component must be attached to enemy before registering a sound.");
            return;
        }
        this.entity.getEvents().addListener(
                event,
                () -> {ServiceLocator.getResourceService().getAsset(sound, Sound.class).play();}
        );
    }
}
