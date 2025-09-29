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
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

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

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("interact", this::show); // open on interact

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

    // Build UI
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

    private void buildRoot() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);
        stage.addActor(root);
    }

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
                prepareToPlay();
                hide();
            }
        });

        Table footer = new Table();
        footer.add(startBtn).width(140).height(44).padRight(8);
        footer.add(closeBtn).width(140).height(44);
        root.add(footer).padTop(4).row();
    }

    public void showMoleAt(int idx) {
        if (idx >= 0 && idx < moleImgs.length) moleImgs[idx].setVisible(true);
    }
    public void hideMoleAt(int idx) {
        if (idx >= 0 && idx < moleImgs.length) moleImgs[idx].setVisible(false);
    }
    public void hideAllMoles() {
        if (moleImgs == null) return;
        for (Image m : moleImgs) m.setVisible(false);
    }

    public void prepareToPlay() {
        setScore(0);
        hideAllMoles();
        setRunning(false);
    }

    public void resetScore() {
        score = 0;
        if (scoreLabel != null) scoreLabel.setText("Score: 0");
    }

    public void setScore(int value) {
        score = Math.max(0, value);
        if (scoreLabel != null) scoreLabel.setText("Score: " + score);
    }

    public int getScore() {
        return score;
    }

    public void setRunning(boolean running) {
        if (startBtn != null) startBtn.setText(running ? "Stop" : "Start");
    }

    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        setVisible(true);
    }

    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        setVisible(false);
        entity.getEvents().trigger("wm:stop");
    }

    private void setVisible(boolean v) {
        if (dimmer != null) dimmer.setVisible(v);
        if (frame != null) frame.setVisible(v);
        if (background != null) background.setVisible(v);
        if (root != null) root.setVisible(v);
    }

    private static Texture solid(Color c) {
        Pixmap pm = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

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