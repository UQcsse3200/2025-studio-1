package com.csse3200.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.csse3200.game.components.screens.TutorialClip;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An {@link Image} that plays a frame-by-frame animation built from a sequence of textures
 * stored under the game assets folder.
 * <p>
 * Frames are loaded via {@code Gdx.files.internal(...)} using a {@code folder + pattern} scheme
 * provided by {@link TutorialClip}, then managed by {@link ResourceService}. The actor advances
 * the animation on each {@link #act(float)} call and updates its drawable accordingly.
 * </p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * TutorialClip clip = new TutorialClip();
 * clip.folder = "tutorial/intro";
 * clip.pattern = "frame_%03d.png"; // frame_001.png, frame_002.png, ...
 * clip.frameCount = 60;
 * clip.fps = 24f;
 * clip.loop = true;
 *
 * AnimatedClipImage img = new AnimatedClipImage(clip);
 * stage.addActor(img);
 * }</pre>
 *
 * <p><strong>Lifecycle:</strong> Call {@link #disposeAssets()} or rely on {@link #remove()} to
 * free textures when the actor is no longer needed.</p>
 *
 * @see <a href="https://libgdx.com/wiki/graphics/2d/2d-graphics">LibGDX 2D Graphics</a>
 */
public class AnimatedClipImage extends Image {
    private final Animation<TextureRegion> animation;
    private final boolean looping;
    private final List<String> loadedPaths;
    private float stateTime = 0f;
    private final TextureRegionDrawable drawable;

    /**
     * Creates an animated image from the frames described by the given {@link TutorialClip}.
     * <p>
     * This constructor validates that all frame files exist under the internal assets folder.
     * It loads textures through {@link ResourceService}, sets linear filtering, constructs the
     * {@link Animation}, and initialises the drawable to the first frame.
     * </p>
     *
     * @param clip clip metadata including folder, filename pattern, frame count, fps and loop flag
     * @throws GdxRuntimeException if any frame is missing or any texture fails to load
     */
    public AnimatedClipImage(TutorialClip clip) {
        Built built = buildAnimation(clip);
        this.animation = built.animation;
        this.looping = clip.loop;
        this.loadedPaths = built.paths;

        this.drawable = new TextureRegionDrawable(animation.getKeyFrame(0));
        setDrawable(drawable);
    }

    /**
     * Internal holder for the build results.
     */
    private record Built(Animation<TextureRegion> animation, List<String> paths) { }

    /**
     * Loads all textures for the clip, validates existence, and builds the {@link Animation}.
     * <p>
     * Paths are constructed as {@code new File(clip.folder, String.format(clip.pattern, i)).getPath()}
     * and resolved via {@link Gdx#files}{@code .internal(path)}. All textures are loaded through
     * {@link ResourceService} to centralise asset management.
     * </p>
     *
     * @param clip frame source description
     * @return a {@link Built} object containing the animation and the list of loaded paths
     * @throws GdxRuntimeException if a frame file does not exist or a texture is null
     */
    private static Built buildAnimation(TutorialClip clip) {
        ResourceService rs = ServiceLocator.getResourceService();
        List<String> paths = new ArrayList<>(clip.frameCount);

        // Build and validate the list of frame paths
        for (int i = 1; i <= clip.frameCount; i++) {
            String filename = String.format(clip.pattern, i);
            String path = new File(clip.folder, filename).getPath();
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) {
                throw new GdxRuntimeException("Missing tutorial frame: " + path);
            }
            paths.add(path);
        }

        rs.loadTextures(paths.toArray(new String[0]));
        rs.loadAll();

        // Convert each Texture into a TextureRegion
        TextureRegion[] regions = new TextureRegion[clip.frameCount];
        for (int i = 0; i < clip.frameCount; i++) {
            Texture tex = rs.getAsset(paths.get(i), Texture.class);
            if (tex == null) {
                throw new GdxRuntimeException("Null texture: " + paths.get(i));
            }
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            regions[i] = new TextureRegion(tex);
        }

        float frameDuration = 1f / Math.max(1f, clip.fps);
        return new Built(new Animation<>(frameDuration, regions), paths);
    }

    /**
     * Advances the animation time and updates the displayed frame.
     *
     * @param delta time elapsed since the last frame in seconds
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        drawable.setRegion(animation.getKeyFrame(stateTime, looping));
    }

    /**
     * Unloads any textures loaded for this image from the {@link ResourceService}.
     * <p>
     * After disposal, this instance should not be used unless the assets are reloaded.
     * </p>
     */
    public void disposeAssets() {
        if (!loadedPaths.isEmpty()) {
            ServiceLocator.getResourceService().unloadAssets(loadedPaths.toArray(new String[0]));
            loadedPaths.clear();
        }
    }

    /**
     * Removes this actor from its parent and frees associated textures.
     *
     * @return {@code true} if the actor was removed
     * @see #disposeAssets()
     */
    @Override
    public boolean remove() {
        disposeAssets();
        return super.remove();
    }
}
