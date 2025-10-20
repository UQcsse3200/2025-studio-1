package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;

/**
 * show a small shield icon while the shield is active.
 */
public class ShieldDisplay extends Component {
    private Image icon;
    private Stage stage;

    private float iconX = 6f;
    private float iconYFromTop = 22f;
    private float iconSize = 16f;

    private static final String ICON_PATH = "images/npcshield.png";

    @Override
    public void create() {
        ResourceService rs = ServiceLocator.getResourceService();
        RenderService render = ServiceLocator.getRenderService();
        stage = render != null ? render.getStage() : null;

        if (!rs.containsAsset(ICON_PATH, Texture.class)) {
            rs.loadTextures(new String[]{ICON_PATH});
            rs.loadAll();
        }
        Texture tex = rs.getAsset(ICON_PATH, Texture.class);

        icon = new Image(tex);
        icon.setSize(iconSize, iconSize);
        // Place icon near top-left (beside HP bar)
        float worldH = stage != null ? stage.getViewport().getWorldHeight() : 720f;
        icon.setPosition(iconX, worldH - iconYFromTop);
        icon.setVisible(false);

        if (stage != null) stage.addActor(icon);

        // Listen for shield lifecycle events on entity
        entity.getEvents().addListener("shieldStart", this::showIcon);
        entity.getEvents().addListener("shieldEnd", this::hideIcon);
    }

    private void showIcon() { if (icon != null) icon.setVisible(true); }
    private void hideIcon() { if (icon != null) icon.setVisible(false); }

    @Override
    public void dispose() {
        if (icon != null) { icon.remove(); icon = null; }
        super.dispose();
    }

    public ShieldDisplay setIconPosition(float x, float fromTop) {
        this.iconX = x; this.iconYFromTop = fromTop; return this;
    }
    public ShieldDisplay setIconSize(float s) {
        this.iconSize = s; return this;
    }
}

