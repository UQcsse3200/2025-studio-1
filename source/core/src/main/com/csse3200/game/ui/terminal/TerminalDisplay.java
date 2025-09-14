package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.screens.BaseScreenDisplay;
import com.csse3200.game.GdxGame;

/**
 * Displays the debug terminal as a bottom "prompt box".
 * Uses BaseScreenDisplay for shared Scene2D wiring.
 */
public class TerminalDisplay extends BaseScreenDisplay {
    private static final float Z_INDEX = 10f;

    // Layout constants
    private static final float H_PADDING = 16f;
    private static final float V_PADDING = 10f;
    private static final float MARGIN_BOTTOM = 8f;
    private static final float MAX_WIDTH_RATIO = 0.48f; // 92% of screen width

    // Scene2D actors
    private Table promptBox;
    private Label label;

    // Data source
    private Terminal terminal;

    public TerminalDisplay(GdxGame game) {
        super(game);
    }

    @Override
    public void create() {
        super.create();
        terminal = entity.getComponent(Terminal.class);
        if (terminal == null) {
            throw new IllegalStateException("Terminal component is required for TerminalDisplay");
        }
    }

    @Override
    protected void buildUI(Table root) {
        // We want bottom-left anchoring instead of the BaseScreenDisplay's default center
        root.clearChildren();
        root.setFillParent(true);
        root.align(Align.bottomLeft);

        // Prompt box container
        promptBox = new Table();
        promptBox.align(Align.left);

        // Background drawable: prefer common skin drawables, fallback to tinted "white"
        Drawable bg = null;
        if (skin.has("textfield", Drawable.class)) {
            bg = skin.getDrawable("textfield");
        } else if (skin.has("rounded", Drawable.class)) {
            bg = skin.getDrawable("rounded");
        } else if (skin.has("button", Drawable.class)) {
            bg = skin.getDrawable("button");
        }
        if (bg == null) {
            Color tint = new Color(0f, 0f, 0f, 0.6f);
            bg = skin.newDrawable("white", tint);
        }
        promptBox.setBackground(bg);

        // Label style: default skin label (swap to monospace in skin if desired)
        label = new Label("> ", skin);
        label.setAlignment(Align.left);
        label.setWrap(false);

        // Padding inside the prompt box
        promptBox.add(label)
                .padLeft(H_PADDING)
                .padRight(H_PADDING)
                .padTop(V_PADDING)
                .padBottom(V_PADDING)
                .growX();

        // Width & placement
        float worldWidth = stage.getViewport().getWorldWidth();
        float maxWidth = worldWidth * MAX_WIDTH_RATIO;

        root.add(promptBox)
                .width(maxWidth)
                .padBottom(MARGIN_BOTTOM)
                .left()
                .row();

        // Start hidden until the terminal is open
        root.setVisible(false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws itself; we just update visibility + text each frame
        if (terminal == null) return; // create() guards this, but be safe

        boolean open = terminal.isOpen();
        if (root != null) root.setVisible(open);
        if (!open) return;

        String message = terminal.getEnteredMessage();
        label.setText("> " + (message == null ? "" : message));
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }
}
