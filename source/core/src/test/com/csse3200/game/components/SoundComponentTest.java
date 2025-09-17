package com.csse3200.game.components;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.headless.mock.audio.MockSound;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.spi.ResourceBundleControlProvider;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
public class SoundComponentTest {
    @Test
    void shouldNotAddSoundToNullEntity() {
        SoundComponent soundComponent = new SoundComponent();
        assertFalse(soundComponent.registerSound("test", "sound"));
    }

    @Test
    void shouldNotRegistSoundIfNotLoaded() {
        ResourceService resourceService = mock(ResourceService.class);
        when(resourceService.getAsset("test", Sound.class)).thenReturn(null);
        ServiceLocator.registerResourceService(resourceService);

        SoundComponent soundComponent = new SoundComponent();
        Entity entity = new Entity();
        entity.addComponent(soundComponent);
        assertFalse(soundComponent.registerSound("test", "sound"));
    }

    @Test
    void shouldRegisterSound() {
        ResourceService resourceService = mock(ResourceService.class);
        MockSound mockSound = new MockSound();
        when(resourceService.getAsset("sound", Sound.class)).thenReturn(mockSound);
        ServiceLocator.registerResourceService(resourceService);

        SoundComponent soundComponent = new SoundComponent();
        Entity entity = new Entity();
        entity.addComponent(soundComponent);
        assertTrue(soundComponent.registerSound("test", "sound"));
    }
}
