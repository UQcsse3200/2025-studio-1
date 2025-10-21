package com.csse3200.game.services;

import com.badlogic.gdx.audio.Music;
import com.csse3200.game.files.UserSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link MusicService}.
 * These tests cover music loading, playback control, screen updates, and resource disposal.
 */
class MusicServiceTest {

    private static class FakeMusic implements Music {
        private boolean playing = false;
        private boolean looping = false;
        private float volume = 0f;

        @Override
        public void play() { playing = true; }
        @Override
        public void stop() { playing = false; }
        @Override
        public void pause() {}
        @Override
        public boolean isPlaying() { return playing; }
        @Override
        public void setLooping(boolean isLooping) { looping = isLooping; }
        @Override
        public boolean isLooping() { return looping; }
        @Override
        public void setVolume(float volume) { this.volume = volume; }
        @Override
        public float getVolume() { return volume; }
        @Override
        public void setPan(float pan, float volume) {}
        @Override
        public void setPosition(float position) {}
        @Override
        public float getPosition() { return 0; }
        @Override
        public void dispose() {}
        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {}
    }

    /**
     * A fake ResourceService to simulate asset loading and unloading.
     */
    private static class FakeResourceService extends ResourceService {
        private final FakeMusic menuMusic = new FakeMusic();
        private final FakeMusic forestMusic = new FakeMusic();

        @Override
        public void loadMusic(String[] musicPaths) {
            // Simulate loading
        }

        @Override
        public void loadAll() {
            // Simulate all loaded
        }

        @Override
        public <T> T getAsset(String path, Class<T> type) {
            if (path.contains("menu")) return type.cast(menuMusic);
            if (path.contains("forest")) return type.cast(forestMusic);
            return null;
        }

        @Override
        public void unloadAssets(String[] assetPaths) {
            // Simulate unloading
        }
    }

    private MusicService musicService;
    private FakeResourceService resourceService;

    @BeforeEach
    void setUp() {
        musicService = new MusicService();
        resourceService = new FakeResourceService();
    }

    @Test
    void testUpdateForScreenStopsMenuMusic() {
        UserSettings.get().setMusicEnabled(true);
        musicService.load(resourceService);

        musicService.updateForScreen("MAIN_GAME");
        assertFalse(resourceService.menuMusic.isPlaying(), "Menu music should stop during main game");
    }

    @Test
    void testSetForestMusicPlayingTogglesPlayback() {
        UserSettings.get().setMusicEnabled(true);
        musicService.load(resourceService);

        musicService.setForestMusicPlaying(true);
        assertTrue(resourceService.forestMusic.isPlaying());

        musicService.setForestMusicPlaying(false);
        assertFalse(resourceService.forestMusic.isPlaying());
    }

    @Test
    void testIsMenuMusicPlayingReflectsState() {
        musicService.load(resourceService);
        resourceService.menuMusic.play();
        assertTrue(musicService.isMenuMusicPlaying());

        resourceService.menuMusic.stop();
        assertFalse(musicService.isMenuMusicPlaying());
    }

    @Test
    void testDisposeStopsAndUnloads() {
        musicService.load(resourceService);
        resourceService.menuMusic.play();

        musicService.dispose(resourceService);
        assertFalse(resourceService.menuMusic.isPlaying(), "Menu music should stop on dispose");
    }
}
