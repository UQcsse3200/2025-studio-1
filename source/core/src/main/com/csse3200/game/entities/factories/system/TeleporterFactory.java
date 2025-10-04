package com.csse3200.game.entities.factories.system;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.teleporter.TeleporterComponent;
import com.csse3200.game.components.teleporter.TeleporterAnimationComponent;
import com.csse3200.game.components.teleporter.TeleporterMenuUI;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Factory for creating a Teleporter entity. A teleporter provides an in-world
 * interaction point that opens a room travel UI limited to discovered rooms
 * (DiscoveryService). The existing terminal commands (teleport = cheat,
 * travel = gated) remain unchanged.
 */
public class TeleporterFactory {
    private static final String TELEPORTER_SHEET = "foreg_sprites/teleporter1-Sheet.png";

    private TeleporterFactory() {}

    private static void ensureAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        if (!rs.containsAsset(TELEPORTER_SHEET, Texture.class)) {
            rs.loadTextures(new String[]{TELEPORTER_SHEET});
            rs.loadAll();
        }
    }

    /**
     * Create an animated teleporter at a world position. Position should be clear of walls/doors.
     * @param worldPos target bottom-left placement
     */
    public static Entity createTeleporter(Vector2 worldPos) {
        ensureAssets();
        Entity tp = new Entity();
        TeleporterAnimationComponent anim = new TeleporterAnimationComponent(TELEPORTER_SHEET, 0.09f);
        tp.addComponent(anim);
        // Add menu UI BEFORE registration so engine accepts it
        TeleporterMenuUI menu = new TeleporterMenuUI();
        tp.addComponent(menu);
        tp.addComponent(new TeleporterComponent());
        tp.setScale(1.8f, 1.8f);
        tp.setPosition(worldPos);
        // ensure hidden initially
        menu.setVisible(false);
        return tp;
    }
}
