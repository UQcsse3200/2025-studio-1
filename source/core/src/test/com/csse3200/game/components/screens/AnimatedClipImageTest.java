package com.csse3200.game.components.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.AnimatedClipImage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AnimatedClipImage}.
 * Uses HeadlessFiles, a FakeResourceService, and empty files for frame existence.
 */
public class AnimatedClipImageTest {
    @Before
    public void setup() {
        Gdx.files = new HeadlessFiles();
    }

    @After
    public void cleanUp() {
        ServiceLocator.clear();
    }

    /** Fake ResourceService that returns mocked Textures and records unloads. */
    private static class FakeResourceService extends ResourceService {
        private final Map<String, Texture> assets = new HashMap<>();
        private String[] lastLoaded = new String[0];
        private final List<String> unloaded = new ArrayList<>();

        @Override
        public void loadTextures(String[] texturePaths) {
            lastLoaded = texturePaths;
        }

        @Override
        public void loadAll() {
            for (String p : lastLoaded) {
                assets.put(p, mock(Texture.class));
            }
        }

        @Override
        public <T> T getAsset(String filepath, Class<T> type) {
            return type.cast(assets.get(filepath));
        }

        @Override
        public void unloadAssets(String[] assetPaths) {
            for (String p : assetPaths) {
                unloaded.add(p);
                assets.remove(p);
            }
        }

        List<String> unloadedPaths() { return unloaded; }
    }

    /** Create empty files */
    private void createEmptyFrames(String folder, String pattern, int count) throws Exception {
        File dir = new File(folder);
        if (!dir.exists()) assertTrue(dir.mkdirs());
        for (int i = 1; i <= count; i++) {
            String name = String.format(pattern, i);
            File f = new File(dir, name);
            if (!f.exists()) {
                try (FileOutputStream out = new FileOutputStream(f)) {
                    out.write(0);
                }
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeFrameCountThrows() {
        ServiceLocator.registerResourceService(new FakeResourceService());
        TutorialClip bad = new TutorialClip("images/tutorial/movement", "frame_%04d.png", -1, 12f, true);
        new AnimatedClipImage(bad);
    }

    @Test(expected = GdxRuntimeException.class)
    public void missingFrameThrows() {
        ServiceLocator.registerResourceService(new FakeResourceService());
        TutorialClip missing = new TutorialClip("does/not/exist", "frame_%04d.png", 3, 12f, true);
        new AnimatedClipImage(missing);
    }

    @Test
    public void zeroFpsBuilds() throws Exception {
        FakeResourceService fake = new FakeResourceService();
        ServiceLocator.registerResourceService(fake);

        String folder = "build/testassets/zero_fps";
        String pattern = "frame_%04d.png";
        createEmptyFrames(folder, pattern, 2);

        TutorialClip clip = new TutorialClip(folder, pattern, 2, 0f, true);
        AnimatedClipImage img = new AnimatedClipImage(clip);
        assertNotNull(img);
    }

    @Test
    public void loopingAdvancesFrame() throws Exception {
        FakeResourceService fake = new FakeResourceService();
        ServiceLocator.registerResourceService(fake);

        String folder = "build/testassets/loop";
        String pattern = "frame_%04d.png";
        createEmptyFrames(folder, pattern, 2);

        AnimatedClipImage img = new AnimatedClipImage(new TutorialClip(folder, pattern, 2, 1f, true));

        TextureRegionDrawable drw = (TextureRegionDrawable) img.getDrawable();
        TextureRegion before = drw.getRegion();

        img.act(1.1f);
        TextureRegion after = drw.getRegion();

        assertNotSame(before, after);
    }

    @Test
    public void disposeAssetsUnloadsTextures() throws Exception {
        FakeResourceService fake = new FakeResourceService();
        ServiceLocator.registerResourceService(fake);

        String folder = "build/testassets/dispose";
        String pattern = "frame_%04d.png";
        createEmptyFrames(folder, pattern, 2);

        AnimatedClipImage img = new AnimatedClipImage(new TutorialClip(folder, pattern, 2, 12f, true));
        img.disposeAssets();

        assertEquals(2, fake.unloadedPaths().size());

    }

    @Test
    public void removeCallsDisposeAssets() throws Exception {
        FakeResourceService fake = new FakeResourceService();
        ServiceLocator.registerResourceService(fake);

        String folder = "build/testassets/remove";
        String pattern = "frame_%04d.png";
        createEmptyFrames(folder, pattern, 2);

        AnimatedClipImage img = new AnimatedClipImage(new TutorialClip(folder, pattern, 2, 12f, true));
        img.remove();

        assertEquals(2, fake.unloadedPaths().size());

    }

    @Test
    public void nonLoopingClampsToLastFrame() throws Exception {
        FakeResourceService fake = new FakeResourceService();
        ServiceLocator.registerResourceService(fake);

        String folder = "build/testassets/noloop";
        String pattern = "frame_%04d.png";
        int frames = 3;
        createEmptyFrames(folder, pattern, frames);

        TutorialClip clip = new TutorialClip(folder, pattern, frames, 10f, false);
        AnimatedClipImage img = new AnimatedClipImage(clip);

        TextureRegionDrawable drw = (TextureRegionDrawable) img.getDrawable();
        assertNotNull(drw);

        img.act(10f);
        TextureRegion after = drw.getRegion();

        img.act(10f);
        TextureRegion after2 = drw.getRegion();

        assertSame(after, after2);
    }
}
