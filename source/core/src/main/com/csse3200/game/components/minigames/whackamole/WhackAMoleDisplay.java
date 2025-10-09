package com.csse3200.game.components.minigames.whackamole;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.audio.Sound;

/**
 * Whack-A-Mole UI overlay:
 * - Modal panel with title, score, 3×3 grid and controls
 * - Handles click-to-hit logic and visual/audio feedback
 * - Emits wm:start / wm:stop / wm:hit events back to the game
 * - Pauses game time while visible
 */
public class WhackAMoleDisplay extends UIComponent {
    private static final float PANEL_W = 720f, PANEL_H = 520f;
    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");

    private Texture pixel;
    private Image dimmer, frame, background;
    private Table root;
    private Label scoreLabel;
    private TextButton startBtn, closeBtn;
    private Image[] moleImgs;
    private int score = 0;
    private TextureRegionDrawable moleDr, holeDr;
    private Sound hitSfx;

    /**
     * Build the overlay:
     * - Register 'interact' to open
     * - Load textures + sfx
     * - Build backdrop, header, grid, footer
     * - Start hidden
     */
    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("betPlaced", this::show); // open on interact
        entity.getEvents().addListener("interact", this::hide);
        hitSfx = ServiceLocator.getResourceService().getAsset("sounds/whack.mp3", Sound.class);

        Texture moleTex = ServiceLocator.getResourceService().getAsset("images/mole.png", Texture.class);
        Texture holeTex = ServiceLocator.getResourceService().getAsset("images/hole.png", Texture.class);
        moleDr = new TextureRegionDrawable(new TextureRegion(moleTex));
        holeDr = new TextureRegionDrawable(new TextureRegion(holeTex));

        buildBackdrop();
        buildRoot();
        buildHeader();
        buildGrid();
        buildFooter();
        hide(); // start hidden
    }

    /** Backdrop: screen dimmer, frame, main panel. */
    private void buildBackdrop() {
        pixel = solid(Color.WHITE);

        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        dimmer.setFillParent(true);
        dimmer.setColor(0,0,0,0.6f);
        stage.addActor(dimmer);

        frame = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        frame.setSize(PANEL_W + 8, PANEL_H + 8);
        frame.setPosition((stage.getWidth()-frame.getWidth())/2f, (stage.getHeight()-frame.getHeight())/2f);
        frame.setColor(Color.BLACK);
        stage.addActor(frame);

        background = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition((stage.getWidth()-background.getWidth())/2f, (stage.getHeight()-background.getHeight())/2f);
        background.setColor(PANEL_COLOR);
        stage.addActor(background);
    }

    /** Root layout table inside the panel. */
    private void buildRoot() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

    /** Header: title on left, live score on right, divider line. */
    private void buildHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TITLE_COLOR;

        Label title = new Label("Whack-A-Mole", titleStyle);
        title.setFontScale(1.8f);

        Label.LabelStyle scoreStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        scoreStyle.fontColor = GOLD;
        scoreLabel = new Label("Score: 0", scoreStyle);

        Table hdr = new Table();
        hdr.add(title).left().expandX();
        hdr.add(scoreLabel).right();
        root.add(hdr).growX().row();

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        divider.setColor(1f, 1f, 1f, 0.08f);
        root.add(divider).width(PANEL_W - 40f).height(2f).row();
    }

    /**
     * 3×3 grid:
     * - Each cell is a hole + mole
     * - Click counts only if mole is visible:
     *   flash, +1, play sfx, increment score, fire wm:hit, hide mole
     */
    private void buildGrid() {
        Table grid = new Table();
        grid.defaults().pad(12).size(92f, 92f).uniform(true);

        moleImgs = new Image[9];

        for (int i = 0; i < 9; i++) {
            Image hole = new Image(holeDr);
            hole.setColor(0.20f, 0.20f, 0.20f, 1f);

            Image mole = new Image(moleDr);
            mole.setVisible(false); // start hidden
            moleImgs[i] = mole;

            Stack cell = new Stack();
            cell.add(hole);
            cell.add(mole);

            // Click to whack (only counts if mole visible)
            cell.addListener(new ClickListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    if (mole.isVisible()) {
                        mole.setColor(0.35f, 0.85f, 0.35f, 1f); // flash
                        com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                            @Override public void run() { mole.setColor(1f, 1f, 1f, 1f); }
                        }, 0.10f);

                        playHitFeedback(cell);
                        setScore(score + 1);
                        entity.getEvents().trigger("wm:hit");
                        mole.setVisible(false);
                    }
                    return true;
                }
            });

            grid.add(cell);
            if ((i + 1) % 3 == 0) grid.row();
        }

        root.add(grid).padTop(6).row();
    }

    /**
     * Footer controls:
     * - Start toggles to Stop and fires wm:start / wm:stop
     * - Close stops, resets, hides UI
     */
    private void buildFooter() {
        startBtn = new TextButton("Start", skin);
        startBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                boolean starting = "Start".contentEquals(startBtn.getText());
                startBtn.setText(starting ? "Stop" : "Start");
                if (starting) {
                    entity.getEvents().trigger("wm:start");
                } else {
                    entity.getEvents().trigger("wm:stop");
                }
            }
        });

        closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // ensure loop stops & UI resets when closing
                entity.getEvents().trigger("wm:stop");
                ServiceLocator.getTimeSource().setPaused(false);
                prepareToPlay();
                hide();
            }
        });

        Table footer = new Table();
        footer.add(startBtn).width(140).height(44).padRight(8);
        footer.add(closeBtn).width(140).height(44);
        root.add(footer).padTop(4).row();
    }

    /** Show a mole by index (safe bounds check). */
    public void showMoleAt(int idx) {
        if (idx >= 0 && idx < moleImgs.length) moleImgs[idx].setVisible(true);
    }

    /** Hide a mole by index (safe bounds check). */
    public void hideMoleAt(int idx) {
        if (idx >= 0 && idx < moleImgs.length) moleImgs[idx].setVisible(false);
    }

    /** Hide all moles. */
    public void hideAllMoles() {
        if (moleImgs == null) return;
        for (Image m : moleImgs) m.setVisible(false);
    }

    /** Reset UI for a fresh run (score=0, no moles, Start label). */
    public void prepareToPlay() {
        setScore(0);
        hideAllMoles();
        setRunning(false);
    }

    /** Score label -> 0. */
    public void resetScore() {
        score = 0;
        if (scoreLabel != null) scoreLabel.setText("Score: 0");
    }

    /** Update score label (clamped to >= 0). */
    public void setScore(int value) {
        score = Math.max(0, value);
        if (scoreLabel != null) scoreLabel.setText("Score: " + score);
    }

    /** Get current score value. */
    public int getScore() {
        return score;
    }

    /** Spawn the “+1” label and animate it up+fade. */
    private void spawnPlusOne(Actor target) {
        Label.LabelStyle st = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        st.fontColor = GOLD;
        Label plus = new Label("+1", st);
        plus.setFontScale(0.9f);
        plus.setTouchable(Touchable.disabled);

        Vector2 p = new Vector2(target.getWidth() / 2f, target.getHeight() * 0.65f);
        target.localToActorCoordinates(root, p);

        root.addActor(plus);
        float w = plus.getPrefWidth();
        plus.setPosition(p.x - w / 2f, p.y);
        plus.toFront(); // make sure it's on top

        // Rise & fade, then remove
        plus.getColor().a = 1f;
        plus.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.moveBy(0f, 22f, 0.35f),
                        Actions.fadeOut(0.35f)
                ),
                Actions.removeActor()
        ));
    }

    /** Play hitsound (whack.mp3) */
    private void playHitSound() {
        if (hitSfx != null) hitSfx.play(0.6f);
    }

    /** Spawn the “+1” label and animate it up+fade. */
    private void playHitFeedback(Actor target) {
        spawnPlusOne(target);
        playHitSound();
    }

    /** Set Start/Stop button label. */
    public void setRunning(boolean running) {
        if (startBtn != null) startBtn.setText(running ? "Stop" : "Start");
    }

    /** Show modal + pause game time. */
    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        setVisible(true);
    }

    /** Hide modal + resume time + ensure loop stops. */
    public void hide() {
        setVisible(false);
        entity.getEvents().trigger("wm:stop");
    }

    /** Toggle visibility for backdrop and root. */
    private void setVisible(boolean v) {
        if (dimmer != null) dimmer.setVisible(v);
        if (frame != null) frame.setVisible(v);
        if (background != null) background.setVisible(v);
        if (root != null) root.setVisible(v);
    }

    /** Build a 1×1 solid texture (remember to dispose). */
    private static Texture solid(Color c) {
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /** Simple end dialog (used for Win/Lose). */
    public void showEnd(String title, String message) {
        Dialog d = new Dialog(title, skin);
        d.text(message);
        d.button("OK", true);
        d.show(stage);
    }

    @Override public void draw(SpriteBatch batch) { /* Stage draws */ }

    @Override public void dispose() {
        if (pixel != null) pixel.dispose();
        super.dispose();
    }
}