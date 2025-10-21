package com.csse3200.game.services;

import com.badlogic.gdx.audio.Sound;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ButtonSoundService}.
 * Covers loading, playback, and disposal.
 */
class ButtonSoundServiceTest {

    private static class FakeSound implements Sound {
        boolean played = false;
        float lastVolume = 0f;

        @Override public long play() { played = true; lastVolume = 1f; return 0; }
        @Override public long play(float volume) { played = true; lastVolume = volume; return 0; }
        @Override public long play(float volume, float pitch, float pan) {
            played = true;
            lastVolume = volume;
            return 0;
        }
        @Override public long loop() { played = true; lastVolume = 1f; return 0; }
        @Override public long loop(float volume) { played = true; lastVolume = volume; return 0; }
        @Override public long loop(float volume, float pitch, float pan) {
            played = true;
            lastVolume = volume;
            return 0; }
        @Override public void stop() { played = false; }
        @Override public void pause() {}
        @Override public void resume() {}
        @Override public void dispose() {}
        @Override public void stop(long soundId) {}
        @Override public void pause(long soundId) {}
        @Override public void resume(long soundId) {}
        @Override public void setLooping(long soundId, boolean looping) {}
        @Override public void setPitch(long soundId, float pitch) {}
        @Override public void setVolume(long soundId, float volume) {}
        @Override public void setPan(long soundId, float pan, float volume) {}
    }

    private static class FakeResourceService extends ResourceService {
        FakeSound clickSound = new FakeSound();

        @Override
        public void loadSounds(String[] soundPaths) {}

        @Override
        public void loadAll() {}

        @Override
        public <T> T getAsset(String path, Class<T> type) {
            if (path.contains("buttonClick")) return type.cast(clickSound);
            return null;
        }

        @Override
        public void unloadAssets(String[] assetPaths) {}
    }

    private ButtonSoundService buttonSoundService;
    private FakeResourceService resourceService;

    @BeforeEach
    void setUp() {
        buttonSoundService = new ButtonSoundService();
        resourceService = new FakeResourceService();
    }

    @Test
    void testLoadInitializesSound() {
        buttonSoundService.load(resourceService);
        assertNotNull(resourceService.clickSound, "Click sound should be loaded");
    }

    @Test
    void testPlayClickPlaysSound() {
        buttonSoundService.load(resourceService);
        buttonSoundService.playClick();
        assertTrue(resourceService.clickSound.played, "Click sound should be played");
        assertEquals(0.6f, resourceService.clickSound.lastVolume, 0.001, "Click sound should play at 0.6 volume");
    }

    @Test
    void testDisposePreventsFurtherPlayback() {
        buttonSoundService.load(resourceService);
        buttonSoundService.dispose(resourceService);

        resourceService.clickSound.played = false;
        buttonSoundService.playClick();
        assertFalse(resourceService.clickSound.played, "Click sound should not play after dispose");
    }
}
