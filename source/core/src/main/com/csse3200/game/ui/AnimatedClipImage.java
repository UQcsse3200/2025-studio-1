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

import java.util.ArrayList;
import java.util.List;

public class AnimatedClipImage extends Image {
    private final Animation<TextureRegion> animation;
    private final boolean looping;
    private final List<String> loadedPaths;
    private float stateTime = 0f;

    public AnimatedClipImage(TutorialClip clip) {
        Built built = buildAnimation(clip);
        this.animation = built.animation;
        this.looping = clip.loop;
        this.loadedPaths = built.paths;
        setDrawable(new TextureRegionDrawable(animation.getKeyFrame(0)));
    }

    private static class Built {
        final Animation<TextureRegion> animation; final List<String> paths;
        Built(Animation<TextureRegion> a, List<String> p) { animation = a; paths = p; }
    }

    private static Built buildAnimation(TutorialClip clip) {
        ResourceService rs = ServiceLocator.getResourceService();
        List<String> paths = new ArrayList<>(clip.frameCount);

        for (int i = 1; i <= clip.frameCount; i++) {
            String filename = String.format(clip.pattern, i);
            String path = clip.folder + "/" + filename;
            FileHandle fh = Gdx.files.internal(path);
            if (!fh.exists()) throw new GdxRuntimeException("Missing tutorial frame: " + path);
            paths.add(path);
        }

        rs.loadTextures(paths.toArray(new String[0]));
        rs.loadAll();

        TextureRegion[] regions = new TextureRegion[clip.frameCount];
        for (int i = 0; i < clip.frameCount; i++) {
            Texture tex = rs.getAsset(paths.get(i), Texture.class);
            if (tex == null) throw new GdxRuntimeException("Null texture: " + paths.get(i));
            tex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            regions[i] = new TextureRegion(tex);
        }

        float frameDuration = 1f / Math.max(1f, clip.fps);
        return new Built(new Animation<>(frameDuration, regions), paths);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        setDrawable(new TextureRegionDrawable(animation.getKeyFrame(stateTime, looping)));
    }

    public void disposeAssets() {
        if (!loadedPaths.isEmpty()) {
            ServiceLocator.getResourceService().unloadAssets(loadedPaths.toArray(new String[0]));
            loadedPaths.clear();
        }
    }

    @Override
    public boolean remove() {
        disposeAssets();
        return super.remove();
    }
}
