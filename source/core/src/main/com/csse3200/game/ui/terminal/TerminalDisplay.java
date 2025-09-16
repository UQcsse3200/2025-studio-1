package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.csse3200.game.components.screens.BaseScreenDisplay;
import com.csse3200.game.GdxGame;

public class TerminalDisplay extends BaseScreenDisplay {
    private static final float Z_INDEX = 10f;

    // Layout
    private static final float WIDTH_RATIO = 0.20f; // 10% of screen width
    private static final float H_PADDING = 16f;
    private static final float V_PADDING = 10f;
    private static final float MARGIN_BOT = 8f;

    private Table container;
    private Label label;

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
        // root may be shared; mount our own container
        container = new Table();
        container.setFillParent(true);
        container.align(Align.bottomLeft);
        container.setTouchable(Touchable.disabled);
        stage.addActor(container);

        // prompt background
        Table promptBox = new Table();
        promptBox.align(Align.left);
        promptBox.setClip(true);

        Drawable bg = null;
        if (skin.has("textfield", Drawable.class)) bg = skin.getDrawable("textfield");
        else if (skin.has("rounded", Drawable.class)) bg = skin.getDrawable("rounded");
        else if (skin.has("button", Drawable.class)) bg = skin.getDrawable("button");
        if (bg == null) bg = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f));
        promptBox.setBackground(bg);

        // label: single line + ellipsis
        label = new Label("> ", skin);
        label.setAlignment(Align.left);
        label.setWrap(false);
        label.setEllipsis(true);
        promptBox.setClip(true);

        promptBox.add(label)
                .minWidth(0f)
                .growX()
                .padLeft(H_PADDING)
                .padRight(H_PADDING)
                .padTop(V_PADDING)
                .padBottom(V_PADDING);

        // width = 10% of container (screen) width; auto-updates on resize
        container.add(promptBox)
                .width(Value.percentWidth(WIDTH_RATIO, container))
                .left()
                .padBottom(MARGIN_BOT);
        container.row();

        container.setVisible(false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (terminal == null) return;

        boolean open = terminal.isOpen();
        if (container != null) {
            container.setVisible(open);
            if (open) container.toFront();
        }
        if (!open) return;

        String message = terminal.getEnteredMessage();
        label.setText("> " + (message == null ? "" : message));
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }
}
