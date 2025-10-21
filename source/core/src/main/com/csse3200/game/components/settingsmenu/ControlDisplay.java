package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.BaseScreenDisplay;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Control/keybindings menu overlay, built like PauseMenuDisplay.
 */
public class ControlDisplay extends BaseScreenDisplay {
    private final Runnable onBack;

    /**
     * @param game  The stage to add UI actors to.
     * @param onBack A callback to run when the back button is pressed (e.g. set previous screen).
     */
    public ControlDisplay(GdxGame game, Runnable onBack) {
        super(game);
        this.onBack = onBack;
    }

    @Override
    protected void buildUI(Table root) {
        root.setFillParent(true);

        Texture bgTexture = new Texture(Gdx.files.internal("images/menu_background.png"));
        Image bgImage = new Image(new TextureRegionDrawable(bgTexture));
        bgImage.setFillParent(true);
        root.addActorAt(0, bgImage);

        Label title = new Label("Controls", skin, "title");
        title.setFontScale(1.5f);
        title.setAlignment(Align.center);

        Table controlsTable = new Table();
        String[][] keybindings = {
                {"A", "Move Left"},
                {"D", "Move Right"},
                {"S", "Crouch"},
                {"Space", "Jump"},
                {"Spacex2", "Double Jump"},
                {"I", "Inventory"},
                {"E", "Pick Item"},
                {"R", "Drop Item"},
                {"Tab", "Open Mini-map"}
        };

        for (String[] pair : keybindings) {
            Label actionLabel = new Label(pair[1], skin, "white");
            actionLabel.setFontScale(1.2f);

            TextButton keyBtn = new TextButton(pair[0], skin, "default");
            keyBtn.setDisabled(true);
            keyBtn.getLabel().setFontScale(1.3f);
            keyBtn.getLabel().setAlignment(Align.center);
            keyBtn.setWidth(120f);

            controlsTable.add(actionLabel).left().pad(7).expandX();
            controlsTable.add(keyBtn).width(140).height(48).pad(7).right();
            controlsTable.row();
        }

        root.add(title).expandX().top().padTop(70f);
        root.row();
        root.add(controlsTable).padTop(36f).padBottom(60f).expand();
        root.row();

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.4f);
        root.add(backBtn).padTop(30f).expandX().center();

        backBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                root.remove();
                if (onBack != null) {
                    onBack.run();
                }
            }
        });
    }

    @Override
    public float getZIndex() {
        return 100f;
    }
}
