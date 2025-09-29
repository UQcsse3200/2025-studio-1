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
    private Image[] cells;

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("interact", this::show); // open on interact
        buildBackdrop();
        buildRoot();
        buildHeader();
        buildGrid();
        buildFooter();
        hide(); // start hidden
    }

    // --- UI build ---
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

        scoreLabel = new Label("Score: 0", skin);
        scoreLabel.setColor(GOLD);

        Table hdr = new Table();
        hdr.add(title).left().expandX();
        hdr.add(scoreLabel).right();
        root.add(hdr).growX().row();

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
        divider.setColor(1f,1f,1f,0.08f);
        root.add(divider).width(PANEL_W-40f).height(2f).row();

        root.add(new Label("UI demo only (no logic yet).", skin)).padTop(4).row();
    }

    private void buildGrid() {
        Table grid = new Table();
        grid.defaults().pad(12).size(92f,92f).uniform(true);
        cells = new Image[9];

        for (int i = 0; i < 9; i++) {
            Image cell = new Image(new TextureRegionDrawable(new TextureRegion(pixel)));
            cell.setColor(0.20f,0.20f,0.20f,1f); // idle gray
            cells[i] = cell;

            // simple click feedback: flash brighter briefly
            cell.addListener((event) -> {
                if (event instanceof InputEvent ie && ie.getType() == InputEvent.Type.touchDown) {
                    cell.setColor(0.35f,0.85f,0.35f,1f);
                    stage.getActionsRequestRendering(); // immediate feel
                    com.badlogic.gdx.utils.Timer.schedule(new com.badlogic.gdx.utils.Timer.Task() {
                        @Override public void run() { cell.setColor(0.20f,0.20f,0.20f,1f); }
                    }, 0.12f);
                    return true;
                }
                return false;
            });

            grid.add(cell);
            if ((i+1)%3==0) grid.row();
        }

        root.add(grid).padTop(6).row();
    }

    private void buildFooter() {
        startBtn = new TextButton("Start", skin);
        startBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                // UI-only toggle; no gameplay yet
                boolean starting = "Start".contentEquals(startBtn.getText());
                startBtn.setText(starting ? "Stop" : "Start");
            }
        });

        closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        Table footer = new Table();
        footer.add(startBtn).width(140).height(44).padRight(8);
        footer.add(closeBtn).width(140).height(44);
        root.add(footer).padTop(4).row();
    }

    // --- Show / Hide ---
    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        setVisible(true);
    }

    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        setVisible(false);
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

    @Override public void draw(SpriteBatch batch) { /* Stage draws */ }

    @Override public void dispose() {
        if (pixel != null) pixel.dispose();
        super.dispose();
    }
}