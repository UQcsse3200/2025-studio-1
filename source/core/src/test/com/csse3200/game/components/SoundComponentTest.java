package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@ExtendWith(GameExtension.class)
public class SoundComponentTest {
    @Test
    void shouldNotAddSoundToNullEntity() {
        SoundComponent soundComponent = new SoundComponent();
        assertFalse(soundComponent.registerSound("test", "sounds/enemyDeath.mp3"));
    }

    @Test
    void shouldAddSoundToEntity() {
        SoundComponent soundComponent = new SoundComponent();
        Entity entity = new Entity();
        entity.addComponent(soundComponent);
        assertTrue(soundComponent.registerSound("test", "sounds/enemyDeath.mp3"));
    }
}
