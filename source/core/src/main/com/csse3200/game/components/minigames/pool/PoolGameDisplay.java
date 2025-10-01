package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UI for the pool mini-game. Renders the table, HUD (scores/turn/fouls),
 * and controls (start, shoot, reset, close), and forwards user actions
 * to the provided Controller.
 */
public class PoolGameDisplay extends UIComponent {

    /** External callbacks for game logic. */
    public interface Controller {
        void onStart();
        void onShoot(float dirX, float dirY, float power);
        void onReset();
        void onStop();
    }

    private Controller controller;
    public void setController(Controller controller) { this.controller = controller; }

    private static final Logger logger = LoggerFactory.getLogger(PoolGameDisplay.class);

    private static final float PANEL_W = 1100f, PANEL_H = 800f;
    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");

    private Texture pixel;
    private Image dimmer, frame, background;
    private Table root;

    // HUD
    private Label titleLbl, p1Lbl, p2Lbl, turnLbl, foulLbl;

    // Controls
    private TextButton startBtn, shootBtn, resetBtn, closeBtn;
    private Slider powerSlider;
    private CheckBox guideToggle;

    // Table + assets
    private PoolTable poolTable;
    private Texture tableTex, cueBallTex, cueTex;

    // State
    private boolean running = false;
    private int p1Score = 0, p2Score = 0, turnIdx = 0; // 0=P1, 1=P2

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("interact", this::show);

        // Asset load
        tableTex   = ServiceLocator.getResourceService().getAsset("images/pool/table.png", Texture.class);
        cueBallTex = ServiceLocator.getResourceService().getAsset("images/pool/cueball.png", Texture.class);
        cueTex     = ServiceLocator.getResourceService().getAsset("images/pool/cue.png", Texture.class);

        buildBackdrop();
        buildRoot();
        buildHeader();
        buildTableArea();
        buildFooter();
        hide();
    }

    /** Builds the dim background and panel chrome behind the UI. */
    private void buildBackdrop() {
        pixel = solid(Color.WHITE);

        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        dimmer.setFillParent(true);
        dimmer.setColor(0, 0, 0, 0.6f);
        dimmer.setTouchable(Touchable.disabled);
        stage.addActor(dimmer);

        frame = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        frame.setSize(PANEL_W + 8, PANEL_H + 8);
        frame.setPosition((stage.getWidth() - frame.getWidth()) / 2f,
                (stage.getHeight() - frame.getHeight()) / 2f);
        frame.setColor(Color.BLACK);
        frame.setTouchable(Touchable.disabled);
        stage.addActor(frame);

        background = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2f,
                (stage.getHeight() - background.getHeight()) / 2f);
        background.setColor(PANEL_COLOR);
        background.setTouchable(Touchable.disabled);
        stage.addActor(background);
    }

    /** Root table layout for all child UI. */
    private void buildRoot() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(16);
        root.defaults().pad(10);
        root.setTouchable(Touchable.enabled);
        stage.addActor(root);
    }

    /** Header with title, scores, current turn, and foul message area. */
    private void buildHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TITLE_COLOR;
        titleLbl = new Label("8-Ball Pool", titleStyle);
        titleLbl.setFontScale(1.5f);

        Label.LabelStyle scoreStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        scoreStyle.fontColor = GOLD;
        p1Lbl = new Label("P1: 0", scoreStyle);
        p2Lbl = new Label("P2: 0", scoreStyle);

        Label.LabelStyle turnStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        turnStyle.fontColor = Color.LIME;
        turnLbl = new Label("Turn: P1", turnStyle);

        Label.LabelStyle foulStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        foulStyle.fontColor = Color.SCARLET;
        foulLbl = new Label("", foulStyle);

        Table hdr = new Table();
        hdr.add(titleLbl).left().expandX();
        hdr.add(turnLbl).center().padRight(10);
        hdr.add(p1Lbl).right().padRight(10);
        hdr.add(p2Lbl).right();
        root.add(hdr).growX().row();

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        divider.setColor(1f, 1f, 1f, 0.08f);
        root.add(divider).width(PANEL_W - 40f).height(2f).row();

        Table foulRow = new Table();
        foulRow.add(foulLbl).left().expandX();
        root.add(foulRow).growX().padTop(2).row();
    }

    /** Interactive table widget with aim handling. */
    private void buildTableArea() {
        float aspect = (float) tableTex.getWidth() / tableTex.getHeight();
        float targetW = PANEL_W - 40f;
        float targetH = targetW / aspect;

        poolTable = new PoolTable(tableTex, cueBallTex, cueTex);
        poolTable.setSize(targetW, targetH);
        poolTable.setOrigin(Align.center);
        poolTable.setTouchable(Touchable.enabled);

        // Aim input
        poolTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                poolTable.beginAim(x, y);
                return true;
            }
            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                poolTable.updateAim(x, y);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                poolTable.updateAim(x, y);
                poolTable.finishAim();
            }
            @Override
            public boolean mouseMoved(InputEvent event, float x, float y) {
                poolTable.updateAim(x, y);
                return false;
            }
        });

        root.add(poolTable).size(targetW, targetH).padTop(6).row();
    }

    /** Footer controls: power, guide toggle, and actions. */
    private void buildFooter() {
        powerSlider = new Slider(0f, 1f, 0.01f, false, skin);
        powerSlider.setValue(0.5f);

        guideToggle = new CheckBox(" Guide", skin);
        guideToggle.setChecked(true);
        guideToggle.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                poolTable.setGuideVisible(guideToggle.isChecked());
            }
        });

        startBtn = new TextButton("Rack / Break", skin);
        shootBtn = new TextButton("Shoot", skin);
        resetBtn = new TextButton("Reset", skin);
        closeBtn = new TextButton("Close", skin);

        startBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                running = true;
                if (controller != null) controller.onStart();
                entity.getEvents().trigger("pool:start");
                clearFoul();
            }
        });

        shootBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (!running) return;

                Vector2 dir = poolTable.getAimDir();
                if (dir.isZero(1e-4f)) return;

                float power = powerSlider.getValue();
                if (controller != null) controller.onShoot(dir.x, dir.y, power);
                entity.getEvents().trigger("pool:shoot", dir.x, dir.y, power);
                poolTable.kickbackCue(Interpolation.sine);
            }
        });

        resetBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (controller != null) controller.onReset();
                entity.getEvents().trigger("pool:reset");
                clearFoul();
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                if (controller != null) controller.onStop();
                entity.getEvents().trigger("pool:stop");
                hide();
            }
        });

        Table controls = new Table();
        controls.add(new Label("Power", skin)).left().padRight(6);
        controls.add(powerSlider).width(260).padRight(16);
        controls.add(guideToggle).padRight(20);
        controls.add(startBtn).width(150).height(44).padRight(6);
        controls.add(shootBtn).width(120).height(44).padRight(6);
        controls.add(resetBtn).width(120).height(44).padRight(6);
        controls.add(closeBtn).width(120).height(44);
        root.add(controls).growX().padTop(8).row();
    }

    // ----- HUD API -----
    public void setScores(int p1, int p2) {
        this.p1Score = Math.max(0, p1);
        this.p2Score = Math.max(0, p2);
        if (p1Lbl != null) p1Lbl.setText("P1: " + p1Score);
        if (p2Lbl != null) p2Lbl.setText("P2: " + p2Score);
    }

    public void setTurn(int playerIdx) {
        this.turnIdx = MathUtils.clamp(playerIdx, 0, 1);
        if (turnLbl != null) turnLbl.setText("Turn: " + (turnIdx == 0 ? "P1" : "P2"));
    }

    public void setFoul(String msg) { foulLbl.setText(msg == null ? "" : msg); }
    public void clearFoul() { foulLbl.setText(""); }

    public void setCueBall(Vector2 pos) { poolTable.setCueBall(pos); }
    public void setBalls(Vector2[] positions) { poolTable.setObjectBalls(positions); }

    // ----- Visibility -----
    public void show() {
        running = true;
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
        entity.getEvents().trigger("pool:stop");
    }

    private void setVisible(boolean v) {
        if (dimmer != null) dimmer.setVisible(v);
        if (frame != null) frame.setVisible(v);
        if (background != null) background.setVisible(v);
        if (root != null) {
            root.setVisible(v);
            root.setTouchable(v ? Touchable.enabled : Touchable.disabled);
        }
    }

    @Override public void draw(SpriteBatch batch) { /* Stage draws the UI. */ }

    @Override public void dispose() {
        if (pixel != null) pixel.dispose();
        super.dispose();
    }

    /** Creates a 1x1 solid-colour texture. */
    private static Texture solid(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Widget for rendering the table, cue ball, guide, and cue.
     * Handles user input to compute the aim direction.
     */
    private static class PoolTable extends Widget {
        private final Texture tableTex, cueBallTex, cueTex;
        private final TextureRegionDrawable whitePx; // 1x1 for guide dots
        private final Vector2 cueBall = new Vector2();
        private Vector2[] balls = new Vector2[0];

        private final Vector2 aimStart = new Vector2();
        private final Vector2 aimEnd = new Vector2();
        private final Vector2 aimDir = new Vector2();
        private boolean aiming = false;
        private boolean showGuide = true;
        private float cueKickT = 0f;

        PoolTable(Texture tableTex, Texture cueBallTex, Texture cueTex) {
            this.tableTex = tableTex;
            this.cueBallTex = cueBallTex;
            this.cueTex = cueTex;

            Texture px = solid(Color.WHITE);
            this.whitePx = new TextureRegionDrawable(new TextureRegion(px));

            // Center cue ball by default
            setCueBall(new Vector2(0.5f, 0.5f));
        }

        void setGuideVisible(boolean v) { this.showGuide = v; }

        /** Set cue ball using normalised [0,1] coordinates. */
        void setCueBall(Vector2 normPos) {
            cueBall.set(MathUtils.clamp(normPos.x, 0f, 1f),
                    MathUtils.clamp(normPos.y, 0f, 1f));
        }

        /** Set object balls using normalised [0,1] coordinates. */
        void setObjectBalls(Vector2[] normPositions) {
            this.balls = normPositions != null ? normPositions : new Vector2[0];
        }

        /** Begin aiming from the pointer location. */
        void beginAim(float x, float y) {
            aiming = true;
            aimStart.set(localToNorm(x, y));
            aimEnd.set(aimStart);
            computeDir();
        }

        /** Update the current aim with the pointer location. */
        void updateAim(float x, float y) {
            if (!aiming) return;
            aimEnd.set(localToNorm(x, y));
            computeDir();
        }

        /** Finish aiming; direction remains available via {@link #getAimDir()}. */
        void finishAim() { aiming = false; }

        /** Copy of the current normalised aim direction. */
        Vector2 getAimDir() { return aimDir.cpy(); }

        /** Trigger a brief cue kickback animation. */
        void kickbackCue(Interpolation interp) { cueKickT = 1f; }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (cueKickT > 0f) {
                cueKickT -= delta * 3f;
                if (cueKickT < 0f) cueKickT = 0f;
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // Table
            Color c = getColor();
            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);
            batch.draw(tableTex, getX(), getY(), getWidth(), getHeight());

            // Positions
            float cbx = getX() + cueBall.x * getWidth();
            float cby = getY() + cueBall.y * getHeight();
            float ballPx = Math.min(getWidth(), getHeight()) * 0.035f;

            // Object balls (placeholder: using cueBallTex)
            if (balls != null) {
                for (Vector2 b : balls) {
                    float bx = getX() + b.x * getWidth() - ballPx / 2f;
                    float by = getY() + b.y * getHeight() - ballPx / 2f;
                    batch.draw(cueBallTex, bx, by, ballPx, ballPx);
                }
            }

            // Cue ball
            batch.draw(cueBallTex, cbx - ballPx / 2f, cby - ballPx / 2f, ballPx, ballPx);

            // Guide and cue
            if (!aimDir.isZero(1e-4f)) {
                if (showGuide) {
                    batch.setColor(1, 1, 1, 0.25f);
                    float step = ballPx * 0.9f;
                    for (int i = 1; i <= 10; i++) {
                        float d = step * i;
                        float gx = cbx + aimDir.x * d;
                        float gy = cby + aimDir.y * d;
                        batch.draw(((TextureRegionDrawable) whitePx).getRegion().getTexture(), gx - 2, gy - 2, 4, 4);
                    }
                    batch.setColor(1, 1, 1, 1);
                }

                float cueLen = ballPx * 7f;
                float kick = (float) Math.pow(cueKickT, 2) * ballPx * 0.7f;
                float off = ballPx * 0.55f + kick;
                float cx = cbx - aimDir.x * (off + cueLen / 2f);
                float cy = cby - aimDir.y * (off + cueLen / 2f);

                batch.draw(cueTex,
                        cx, cy,
                        cueLen / 2f, ballPx * 0.25f,   // origin
                        cueLen, ballPx * 0.5f,        // size
                        1f, 1f
                );
            }
        }

        /** Recompute normalised aim direction from cue ball to aim target. */
        private void computeDir() {
            Vector2 target = aiming ? aimEnd : aimStart;
            aimDir.set(target).sub(cueBall).nor();
            if (Float.isNaN(aimDir.x) || Float.isNaN(aimDir.y)) aimDir.setZero();
        }

        /** Convert local widget coordinates to normalised [0,1] space. */
        private Vector2 localToNorm(float x, float y) {
            float nx = MathUtils.clamp(x / getWidth(), 0f, 1f);
            float ny = MathUtils.clamp(y / getHeight(), 0f, 1f);
            return new Vector2(nx, ny);
        }
    }
}