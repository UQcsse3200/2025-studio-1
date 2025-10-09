package com.csse3200.game.entities.factories.system;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.teleporter.TeleporterComponent;
import com.csse3200.game.components.teleporter.TeleporterMenuUI;
import com.csse3200.game.components.teleporter.TeleporterIdleRenderComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;

/**
 * Factory for creating a teleporter entity using an atlas animation (teleporter1-Sheet).
 */
public class TeleporterFactory {
    private static final String TELEPORTER_ATLAS = "foreg_sprites/teleporter1-Sheet.atlas";
    private static final float BASE_WIDTH = 1.1f; // reduced size

    private TeleporterFactory() {}

    private static void ensureAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        boolean needLoad = !rs.containsAsset(TELEPORTER_ATLAS, TextureAtlas.class);
        if (needLoad) {
            rs.loadTextureAtlases(new String[]{TELEPORTER_ATLAS});
            rs.loadAll();
        } else {
            // Validate that the loaded atlas actually has the frames; if not, force reload (handles updated file on disk)
            TextureAtlas atlas = rs.getAsset(TELEPORTER_ATLAS, TextureAtlas.class);
            if (atlas.findRegions("teleporter1").size == 0) {
                rs.unloadAssets(new String[]{TELEPORTER_ATLAS});
                rs.loadTextureAtlases(new String[]{TELEPORTER_ATLAS});
                rs.loadAll();
            }
        }
    }

    public static Entity createTeleporter(Vector2 worldPos) {
        ensureAssets();
        ResourceService rs = ServiceLocator.getResourceService();
        TextureAtlas atlas = rs.getAsset(TELEPORTER_ATLAS, TextureAtlas.class);

        Entity tp = new Entity();

        // Static idle renderer (draws first frame only)
        tp.addComponent(new TeleporterIdleRenderComponent(atlas, "teleporter1"));

        // Animator only used during activation
        AnimationRenderComponent animator = new AnimationRenderComponent(atlas);
        animator.setDisposeAtlas(false);
        animator.addAnimation("teleporter1", 0.12f, PlayMode.LOOP); // only used during activation now
        tp.addComponent(animator);

        TeleporterMenuUI menu = new TeleporterMenuUI();
        tp.addComponent(menu);
        tp.addComponent(new TeleporterComponent());
        tp.setPosition(worldPos);

        // Manual scaling using first teleporter frame (falls back gracefully)
        Array<TextureAtlas.AtlasRegion> frames = atlas.findRegions("teleporter1");
        TextureRegion frame = frames.size > 0 ? frames.first() : null;
        float targetWidth = BASE_WIDTH;
        if (frame != null && frame.getRegionWidth() > 0) {
            float aspect = (float) frame.getRegionHeight() / frame.getRegionWidth();
            tp.setScale(targetWidth, targetWidth * aspect);
        } else {
            // fallback default size
            tp.setScale(targetWidth, targetWidth);
        }

        menu.setVisible(false);
        // Do NOT start any animation here (idle is static)
        return tp;
    }
}
