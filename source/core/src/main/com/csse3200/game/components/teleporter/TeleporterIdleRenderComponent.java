package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.rendering.RenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Renders a single static frame (index 0) of a named atlas animation as the teleporter's idle look.
 * This lets us keep a full animation atlas without looping it when idle.
 */
public class TeleporterIdleRenderComponent extends RenderComponent {
    private static final Logger logger = LoggerFactory.getLogger(TeleporterIdleRenderComponent.class);
    private final TextureAtlas atlas;
    private final String regionName;
    private TextureRegion frame;
    private boolean visible = true;

    public TeleporterIdleRenderComponent(TextureAtlas atlas, String regionName) {
        this.atlas = atlas;
        this.regionName = regionName;
        // Attempt to fetch first frame immediately; will be null-safe in draw
        var regions = atlas.findRegions(regionName);
        if (regions != null && regions.size > 0) {
            frame = regions.first();
        } else {
            // fallback: single region (non-indexed) lookup
            frame = atlas.findRegion(regionName);
            if (frame == null) {
                logger.warn("Teleporter idle frame '{}' not found in atlas", regionName);
            }
        }
    }

    public void setVisible(boolean v) { this.visible = v; }
    public boolean isVisible() { return visible; }

    @Override
    protected void draw(SpriteBatch batch) {
        if (!visible || frame == null) return;
        Vector2 pos = entity.getPosition();
        Vector2 scale = entity.getScale();
        batch.draw(frame, pos.x, pos.y, scale.x, scale.y);
    }
}
