// PauseMenuDisplay.java
package com.csse3200.game.components.screens;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.GdxGame;
import com.csse3200.game.services.ServiceLocator;

public class PauseMenuDisplay extends BaseScreenDisplay {
    // NEW: controls whether the Restart button is shown
    private final boolean hideSome;
    private Image dimmer;

    /**
     * New: pass hideSome=false to hide the Restart button.
     */
    public PauseMenuDisplay(GdxGame game, boolean hideSome) {
        super(game);
        this.hideSome = hideSome;
    }

    @Override
    protected void buildUI(Table root) {
        if (dimmer == null) {
            dimmer = new Image(skin.newDrawable("white", new Color(0, 0, 0, 0.6f)));
            dimmer.setFillParent(true);
            dimmer.setTouchable(Touchable.disabled);
        }
        if (dimmer.getStage() == null) stage.addActor(dimmer);
        root.toFront();

        addTitle(root, "Game Paused", 2.0f, Color.WHITE, 24f);

        Table panel = new Table();
        panel.defaults().pad(10f);

        panel.add(button("Resume", 1.8f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            entity.getEvents().trigger("resume");
        })).row();

        // Only add Restart if enabled
        if (hideSome) {
            panel.add(button("Restart", 2f, () -> {
                ServiceLocator.getButtonSoundService().playClick();
                game.setScreen(GdxGame.ScreenType.MAIN_GAME);
            })).row();
        }

        panel.add(button("Main Menu", 2f, () -> {
            ServiceLocator.getButtonSoundService().playClick();
            backMainMenu();
        })).row();

        if (hideSome) {
            panel.add(button("Save", 1.8f, () -> {
                ServiceLocator.getButtonSoundService().playClick();
                entity.getEvents().trigger("save");
                backMainMenu();
            })).row();
        }

        root.add(panel).center().expandX().row();

        stage.setKeyboardFocus(root);
        root.setTouchable(Touchable.enabled);

        final InputListener escOnce = new InputListener() {
            private boolean handled = false;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE && !handled) {
                    handled = true;
                    entity.getEvents().trigger("resume");
                    root.removeListener(this);
                    return true;
                }
                return false;
            }
        };
        root.addListener(escOnce);
    }

    @Override
    public float getZIndex() {
        return 100f;
    }

    @Override
    public void dispose() {
        if (dimmer != null) {
            dimmer.remove();
            dimmer = null;
        }
        super.dispose();
    }
}
