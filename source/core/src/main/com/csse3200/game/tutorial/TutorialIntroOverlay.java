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
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

public final class TutorialIntroOverlay extends Component {

    private static final float PAD = 24f;
    private static final float DIM_ALPHA = 0.38f;
    private static final float PANEL_ALPHA = 0.92f;
    private static final float CORNER = 16f;
    private static final float BORDER = 2f;
    private static final float SHADOW_ALPHA = 0.35f;
    private static final float BTN_HEIGHT = 48f;

    private final Starter onStart;
    private Stage stage;
    private Table root;                         // full-screen dim
    private Container<Table> panel;             // rounded card (top of stack)
    private Container<Actor> shadow;            // soft shadow (bottom of stack)
    private Label title;
    private TextButton startBtn;

    // generated textures (dispose in dispose())
    private Texture dimTex, panelTex, shadowTex;
    private Texture btnUp, btnOver, btnDown;

    // input filtering
    private InputListener keyFilter;      // consumes disallowed keys
    private FocusListener focusKeeper;    // keeps keyboard focus on this overlay

    public TutorialIntroOverlay(Starter onStart) {
        this.onStart = onStart;
    }

    // ────────────────────────── Tiny texture helpers ──────────────────────────
    private static Texture solid(float r, float g, float b, float a) {
        Pixmap pm = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        pm.setColor(r, g, b, a);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture roundedCard(int w, int h, Color fill, float alpha, float radius, float border, Color borderCol) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        Color f = new Color(fill);
        f.a = alpha;

        // fill
        pm.setColor(f);
        fillRoundRect(pm, 0, 0, w, h, radius);

        // hairline border
        if (border > 0f) {
            pm.setColor(new Color(borderCol.r, borderCol.g, borderCol.b, Math.min(1f, borderCol.a)));
            strokeRoundRect(pm, Math.round(border / 2f), Math.round(border / 2f), w - Math.round(border), h - Math.round(border), radius);
        }

        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture softShadow(int w, int h, float radius, float alpha) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(new Color(0, 0, 0, alpha));
        fillRoundRect(pm, 0, 0, w, h, radius);
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static void fillRoundRect(Pixmap pm, int x, int y, int w, int h, float r) {
        int ri = Math.round(r);
        pm.fillRectangle(x + ri, y, w - 2 * ri, h);
        pm.fillRectangle(x, y + ri, w, h - 2 * ri);
        pm.fillCircle(x + ri, y + ri, ri);
        pm.fillCircle(x + w - ri - 1, y + ri, ri);
        pm.fillCircle(x + ri, y + h - ri - 1, ri);
        pm.fillCircle(x + w - ri - 1, y + h - ri - 1, ri);
    }

    private static void strokeRoundRect(Pixmap pm, int x, int y, int w, int h, float r) {
        int ri = Math.round(r);
        pm.fillRectangle(x + ri, y, w - 2 * ri, 1);
        pm.fillRectangle(x + ri, y + h - 1, w - 2 * ri, 1);
        pm.fillRectangle(x, y + ri, 1, h - 2 * ri);
        pm.fillRectangle(x + w - 1, y + ri, 1, h - 2 * ri);
        // light corner dots to keep it crisp
        pm.drawPixel(x + ri, y);
        pm.drawPixel(x, y + ri);
        pm.drawPixel(x + w - ri - 1, y);
        pm.drawPixel(x + w - 1, y + ri);
        pm.drawPixel(x + ri, y + h - 1);
        pm.drawPixel(x, y + h - ri - 1);
        pm.drawPixel(x + w - ri - 1, y + h - 1);
        pm.drawPixel(x + w - 1, y + h - ri - 1);
    }

    // ────────────────────────── Lifecycle ──────────────────────────

    @Override
    public void create() {
        stage = ServiceLocator.getRenderService().getStage();

        // Palette
        Color cardFill = new Color(0.08f, 0.08f, 0.10f, 1f);
        Color borderCol = new Color(1f, 1f, 1f, 0.08f);
        Color btnBase = new Color(0.12f, 0.58f, 0.98f, 1f);

        // Textures
        dimTex = solid(0f, 0f, 0f, DIM_ALPHA);
        int baseW = 1024, baseH = 240; // scalable canvas for card
        panelTex = roundedCard(baseW, baseH, cardFill, PANEL_ALPHA, CORNER, BORDER, borderCol);
        shadowTex = softShadow(baseW, baseH, CORNER + 6f, SHADOW_ALPHA);

        // Rounded button states
        btnUp = roundedCard(512, 64, btnBase, 1f, 12f, 0f, Color.CLEAR);
        btnOver = roundedCard(512, 64, new Color(0.16f, 0.66f, 1.0f, 1f), 1f, 12f, 0f, Color.CLEAR);
        btnDown = roundedCard(512, 64, new Color(0.08f, 0.50f, 0.90f, 1f), 1f, 12f, 0f, Color.CLEAR);

        // Root
        root = new Table();
        root.setFillParent(true);
        root.setBackground(new TextureRegionDrawable(new TextureRegion(dimTex)));
        stage.addActor(root);

        // Panel content
        Table panelTable = new Table();
        panelTable.pad(PAD);

        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        title = new Label("Press Start to begin the tutorial", labelStyle);
        title.setFontScale(1.6f);
        title.setWrap(false);
        title.setEllipsis(true);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = new BitmapFont();
        style.up = new TextureRegionDrawable(new TextureRegion(btnUp));
        style.over = new TextureRegionDrawable(new TextureRegion(btnOver));
        style.down = new TextureRegionDrawable(new TextureRegion(btnDown));

        startBtn = new TextButton("Start  (Enter / Space)", style);

        panelTable.add(title).padBottom(16f).growX().row();
        panelTable.add(startBtn).height(BTN_HEIGHT).growX();

        // Card (top) + Shadow (bottom) stacked
        panel = new Container<>(panelTable);
        panel.background(new TextureRegionDrawable(new TextureRegion(panelTex)));
        panel.setTransform(true);

        shadow = new Container<>(new Label("", labelStyle)); // dummy actor for size
        shadow.setBackground(new TextureRegionDrawable(new TextureRegion(shadowTex)));
        shadow.setTransform(true);

        Stack stack = new Stack();
        stack.addActor(shadow); // bottom
        stack.addActor(panel);  // top

        float w = Math.min(640f, stage.getWidth() - 64f);
        root.add(stack).width(w).center();

        // slight offset to make the shadow visible
        shadow.addAction(Actions.moveBy(6f, -6f));

        // Entrance animation
        panel.setScale(0.96f);
        panel.getColor().a = 0f;
        shadow.setScale(0.96f);
        shadow.getColor().a = 0f;
        panel.addAction(Actions.parallel(Actions.fadeIn(0.18f), Actions.scaleTo(1f, 1f, 0.18f)));
        shadow.addAction(Actions.parallel(Actions.fadeIn(0.18f), Actions.scaleTo(1f, 1f, 0.18f)));

        // Button click
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startTutorial(-1); // started by mouse
            }
        });

        // KEYBOARD FILTER: allow only ENTER/SPACE, block everything else
        keyFilter = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    startTutorial(keycode);
                    return true;
                }
                return true; // swallow everything else while visible
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                return true; // swallow keyUp too
            }
        };
        root.addListener(keyFilter);

        // Keep focus
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

        // Fade out card + shadow, then unregister and clean up
        panel.clearActions();
        shadow.clearActions();
        panel.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.scaleTo(1.03f, 1.03f, 0.12f),
                        Actions.fadeOut(0.12f)
                ),
                Actions.run(() -> {
                    dispose();
                    if (entity != null) {
                        entity.setEnabled(false);
                        var es = ServiceLocator.getEntityService();
                        if (es != null) es.unregister(entity);
                    }
                })
        ));
        shadow.addAction(Actions.fadeOut(0.12f));
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
        if (shadowTex != null) {
            shadowTex.dispose();
            shadowTex = null;
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
