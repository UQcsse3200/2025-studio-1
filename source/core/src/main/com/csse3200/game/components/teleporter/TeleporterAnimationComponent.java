package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Spritesheet animation for the teleporter. Attempts to auto-detect frame layout:
 * 1) If sheet width is an integer multiple of height -> assumes square frames of size=height.
 * 2) Otherwise tries candidate horizontal frame counts (2..32) and picks the first that divides width evenly
 *    producing a frame width >= 8 and <= 1024.
 * Falls back to single-frame if no layout fits.
 */
public class TeleporterAnimationComponent extends RenderComponent {
    private final Texture texture;
    private final Array<TextureRegion> frames = new Array<>();
    private final float frameDuration;
    private float time;

    public TeleporterAnimationComponent(String sheetPath, float frameDuration) {
        ResourceService rs = ServiceLocator.getResourceService();
        this.texture = rs.getAsset(sheetPath, Texture.class);
        this.frameDuration = frameDuration;
        slice();
    }

    private void slice() {
        int h = texture.getHeight();
        int w = texture.getWidth();
        if (h <= 0 || w <= 0) {
            frames.add(new TextureRegion(texture));
            return;
        }
        // Case 1: square frames (width multiple of height)
        if (w % h == 0) {
            int count = w / h;
            for (int i = 0; i < count; i++) {
                frames.add(new TextureRegion(texture, i * h, 0, h, h));
            }
            Gdx.app.log("TeleporterAnim", "Detected square frame sheet: " + count + " frames");
            return;
        }
        // Case 2: try candidate horizontal splits
        boolean sliced = false;
        for (int candidate = 2; candidate <= 32; candidate++) {
            if (w % candidate == 0) {
                int fw = w / candidate; // frame width
                if (fw >= 8 && fw <= 1024) { // sane bounds
                    for (int i = 0; i < candidate; i++) {
                        frames.add(new TextureRegion(texture, i * fw, 0, fw, h));
                    }
                    Gdx.app.log("TeleporterAnim", "Detected horizontal strip: " + candidate + " frames each " + fw + "x" + h);
                    sliced = true;
                    break;
                }
            }
        }
        if (!sliced) {
            frames.add(new TextureRegion(texture));
            Gdx.app.log("TeleporterAnim", "Could not auto-slice teleporter sheet; using single frame");
        }
    }

    @Override
    public void update() {
        time += ServiceLocator.getTimeSource() != null ? ServiceLocator.getTimeSource().getDeltaTime() : 1/60f;
    }

    private TextureRegion current() {
        if (frames.size == 0) return null;
        int idx = (int)(time / frameDuration) % frames.size;
        return frames.get(idx);
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (isDisabled()) return;
        TextureRegion region = current();
        if (region == null) return;
        Vector2 pos = entity.getPosition();
        Vector2 scale = entity.getScale();
        if (scale.x == 0 || scale.y == 0) {
            float aspect = (float) region.getRegionHeight() / region.getRegionWidth();
            entity.setScale(1f, aspect);
            scale = entity.getScale();
        }
        batch.draw(region, pos.x, pos.y, scale.x, scale.y);
    }
}
