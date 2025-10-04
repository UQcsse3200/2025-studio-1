package com.csse3200.game.entities.factories.system;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.teleporter.TeleporterComponent;
import com.csse3200.game.components.teleporter.TeleporterMenuUI;
import com.csse3200.game.components.teleporter.TeleporterVisualComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Teleporter now uses 3 standalone textures:
 *  - TP_n1.png : idle (default)
 *  - TP_n2.png : activation frame 1
 *  - TP_n3.png : activation frame 2 (final before transition)
 */
public class TeleporterFactory {
    private static final String IDLE_TEX = "images/TP_n1.png";
    private static final String ACT1_TEX = "images/TP_n2.png";
    private static final String ACT2_TEX = "images/TP_n3.png";

    private TeleporterFactory() {}

    private static void ensureAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        String[] needed = {IDLE_TEX, ACT1_TEX, ACT2_TEX};
        boolean load = false;
        for (String p : needed) {
            if (!rs.containsAsset(p, Texture.class)) { load = true; break; }
        }
        if (load) {
            rs.loadTextures(needed);
            rs.loadAll();
        }
    }

    public static Entity createTeleporter(Vector2 worldPos) {
        ensureAssets();
        ResourceService rs = ServiceLocator.getResourceService();
        Texture idle = rs.getAsset(IDLE_TEX, Texture.class);
        Texture a1 = rs.getAsset(ACT1_TEX, Texture.class);
        Texture a2 = rs.getAsset(ACT2_TEX, Texture.class);

        Entity tp = new Entity();
        TeleporterVisualComponent visual = new TeleporterVisualComponent(idle, a1, a2);
        visual.setFrameDuration(0.2f); // slightly slower so player can see n2 -> n3
        tp.addComponent(visual);
        TeleporterMenuUI menu = new TeleporterMenuUI();
        tp.addComponent(menu);
        tp.addComponent(new TeleporterComponent());
        tp.setPosition(worldPos);
        // uniform scale based on idle's aspect to approx width 1.6
        float aspect = (float) idle.getHeight() / idle.getWidth();
        tp.setScale(1.6f, 1.6f * aspect);
        menu.setVisible(false);
        return tp;
    }
}
