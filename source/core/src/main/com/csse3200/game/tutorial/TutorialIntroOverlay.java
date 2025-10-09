package com.csse3200.game.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * Intro overlay that blocks ALL input except Enter/Space (to start).
 * On start it notifies the parent via {@link Starter#onStart(int)} with the starting keycode
 * (or -1 if started by mouse), then fades out and removes itself.
 * <p>
 * While visible, this overlay:
 * • Captures keyboard focus.
 * • Consumes every key event except ENTER/SPACE (so TAB cannot slip through and open minimap, etc).
 * • Optionally you can allow ESC by changing the key filter below (currently blocked).
 */
public final class TutorialIntroOverlay extends Component {

    private final Starter onStart;
    private Stage stage;
    private Table root;                   // full-screen dim
    private Container<Table> panel;       // centered panel
    private Label title;
    private TextButton startBtn;
    // generated textures (disposed)
    private Texture dimTex, panelTex, btnUp, btnOver, btnDown;
    // input filtering
    private InputListener keyFilter;      // consumes disallowed keys
    private FocusListener focusKeeper;    // keeps keyboard focus on this overlay

    public TutorialIntroOverlay(Starter onStart) {
        this.onStart = onStart;
    }

    private static Texture solid(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // simple solid color drawables (no asset pipeline needed)
        dimTex = solid(0f, 0f, 0f, 0.35f);
        panelTex = solid(0f, 0f, 0f, 0.85f);
        btnUp = solid(0.10f, 0.55f, 0.95f, 1f);
        btnOver = solid(0.12f, 0.65f, 1.00f, 1f);
        btnDown = solid(0.05f, 0.45f, 0.85f, 1f);

        // dimmed full-screen root
        root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(dimTex)));
        stage.addActor(root);

        // panel content
        Table panelTable = new Table();
        panelTable.pad(24f);

        title = new Label("Press Start to begin the tutorial", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        title.setFontScale(1.6f);
        title.setWrap(false);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = new BitmapFont();
        style.up = new TextureRegionDrawable(new TextureRegion(btnUp));
        style.over = new TextureRegionDrawable(new TextureRegion(btnOver));
        style.down = new TextureRegionDrawable(new TextureRegion(btnDown));

        startBtn = new TextButton("Start  (Enter / Space)", style);

        panelTable.add(title).padBottom(16f).row();
        panelTable.add(startBtn).width(Math.min(420f, stage.getWidth() - 64f)).height(48f);

        panel = new Container<>(panelTable);
        panel.background(new TextureRegionDrawable(new TextureRegion(panelTex)));
        panel.setTransform(true);
        root.add(panel).center().width(Math.min(640f, stage.getWidth() - 64f));

        // click to start
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startTutorial(-1); // mouse click → no starting key to forward
            }
        });

        // KEYBOARD FILTER: allow only ENTER/SPACE, block everything else (TAB/ESC/etc.)
        keyFilter = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    startTutorial(keycode);
                    return true; // consume so nothing else sees it
                }
                // Block everything else while the intro is up (prevent TAB pause/minimap etc.)
                return true;
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                // swallow as well to avoid trailing keyUp reaching game systems
                return true;
            }
        };
        root.addListener(keyFilter);

        // keep keyboard focus on the overlay (so it always receives key events first)
        focusKeeper = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused && root.getStage() != null) {
                    root.getStage().setKeyboardFocus(root);
                }
            }
        };
        root.addListener(focusKeeper);
        stage.setKeyboardFocus(root);
    }

    private void startTutorial(int startedBy) {
        if (onStart != null) {
            try {
                onStart.onStart(startedBy);
            } catch (Throwable t) {
                Gdx.app.error("TutorialIntroOverlay", "onStart threw", t);
            }
        }

        // Fade out the panel, then remove the entire entity from the world
        if (panel != null) {
            panel.clearActions();
            panel.addAction(Actions.sequence(
                    Actions.parallel(
                            Actions.scaleTo(1.03f, 1.03f, 0.12f),
                            Actions.fadeOut(0.12f)
                    ),
                    Actions.run(() -> {
                        // Drop Stage actors & input hooks
                        dispose();
                        // IMPORTANT: unregister the entity so the screen no longer sees this overlay
                        if (entity != null) {
                            entity.setEnabled(false);
                            com.csse3200.game.entities.EntityService es = ServiceLocator.getEntityService();
                            if (es != null) {
                                es.unregister(entity);
                            }
                        }
                    })
            ));
        } else {
            // Fallback: if there's no panel, at least clean up and unregister immediately
            dispose();
            if (entity != null) {
                entity.setEnabled(false);
                var es = ServiceLocator.getEntityService();
                if (es != null) es.unregister(entity);
            }
        }
    }


    @Override
    public void dispose() {
        if (root != null) {
            if (keyFilter != null) {
                root.removeListener(keyFilter);
                keyFilter = null;
            }
            if (focusKeeper != null) {
                root.removeListener(focusKeeper);
                focusKeeper = null;
            }
            root.remove();
            root = null;
        }
        if (dimTex != null) {
            dimTex.dispose();
            dimTex = null;
        }
        if (panelTex != null) {
            panelTex.dispose();
            panelTex = null;
        }
        if (btnUp != null) {
            btnUp.dispose();
            btnUp = null;
        }
        if (btnOver != null) {
            btnOver.dispose();
            btnOver = null;
        }
        if (btnDown != null) {
            btnDown.dispose();
            btnDown = null;
        }
    }

    public interface Starter {
        /**
         * @param startedByKeycodeOrMinus1 keycode that started the tutorial, or -1 if via mouse click
         */
        void onStart(int startedByKeycodeOrMinus1);
    }
}
