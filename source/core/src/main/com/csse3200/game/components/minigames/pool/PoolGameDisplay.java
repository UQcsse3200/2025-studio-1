package com.csse3200.game.components.minigames.pool;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.minigames.pool.displayhelpers.PoolTable;
import com.csse3200.game.components.minigames.pool.logic.GameEvents;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.forever;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.run;

/**
 * The main UI component for the Pool minigame.
 * <p>
 * This class renders the pool table, HUD, and player controls, and
 * connects user inputs (e.g., aiming and shooting) to the game’s
 * {@link PoolGameDisplay.Controller} interface.
 * <p>
 * Presentation-only: all physics and logic interactions are routed
 * through the controller or triggered events.
 */
public class PoolGameDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(PoolGameDisplay.class);

    // Layout constants
    private static final float PANEL_W = 1100f;
    private static final float PANEL_H = 800f;
    private static final float ROOT_PAD = 16f;
    private static final float CELL_PAD = 10f;

    // UI colors
    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");
    private static final Color TURN_COLOR = Color.LIME;
    private static final Color FOUL_COLOR = Color.SCARLET;

    // Controller bridge
    private Controller controller;

    // Root chrome
    private Texture pixel; // 1x1 white texture
    private Image dimmer, frame, background;
    private Table root;

    // HUD widgets
    private Label p1Lbl;
    private Label p2Lbl;
    private Label turnLbl;
    private Label foulLbl;
    private TextButton shootBtn;
    private Slider powerSlider;
    private CheckBox guideToggle;

    // Table and textures
    private PoolTable poolTable;
    private Texture tableTex;
    private Texture cueTex;
    private TextureRegion cueBallTex;
    private TextureRegion[] ballTextures;

    // Runtime state
    private boolean running;
    private boolean shotTakenThisTurn = false;

    /**
     * Creates a solid 1x1 texture of the given color.
     *
     * @param c the color to fill the texture with
     * @return a {@link Texture} containing one solid pixel of color {@code c}
     */
    public static Texture makeSolid(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Creates an {@link Image} using a solid-colored region.
     *
     * @param px    the base pixel texture
     * @param x     x-position
     * @param y     y-position
     * @param w     width
     * @param h     height
     * @param color the fill color
     * @return an {@link Image} of the given color and size
     */
    private static Image makeSolidImage(Texture px, float x, float y, float w, float h, Color color) {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(px)));
        img.setBounds(x, y, w, h);
        img.setColor(color);
        return img;
    }

    /**
     * Sets the pool display controller, which bridges the UI with game logic.
     *
     * @param controller the controller to attach
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Creates the UI hierarchy, loads assets, builds HUD elements,
     * and wires events for real-time updates.
     */
    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener(GameEvents.INTERACT, this::show);

        loadAssets();
        buildBackdrop();
        buildRoot();
        buildHeader();
        buildTableArea();
        buildFooter();
        wireHudEvents();

        // Reactive UI update loop
        root.addAction(forever(run(this::updateUiState)));

        hide();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Scene2D stage handles all rendering
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (pixel != null) {
            pixel.dispose();
            pixel = null;
        }
        super.dispose();
    }


    /**
     * Updates the displayed player scores.
     *
     * @param p1 player 1 score
     * @param p2 player 2 score
     */
    public void setScores(int p1, int p2) {
        int p1Score = Math.max(0, p1);
        int p2Score = Math.max(0, p2);
        if (p1Lbl != null) p1Lbl.setText("P1: " + p1Score);
        if (p2Lbl != null) p2Lbl.setText("P2: " + p2Score);
    }

    /**
     * Updates the turn indicator label.
     *
     * @param playerIdx the index of the current player (0 = P1, 1 = P2)
     */
    public void setTurn(int playerIdx) {
        int turnIdx = MathUtils.clamp(playerIdx, 0, 1);
        if (turnLbl != null) turnLbl.setText("Turn: " + (turnIdx == 0 ? "P1" : "P2"));
    }

    /**
     * Displays a foul message on the HUD.
     *
     * @param msg the foul message text, or {@code null} to clear
     */
    public void setFoul(String msg) {
        if (foulLbl != null) foulLbl.setText(msg == null ? "" : msg);
    }

    /**
     * Clears any displayed foul message.
     */
    public void clearFoul() {
        setFoul("");
    }


    /**
     * Updates the cue ball’s position in normalized table coordinates.
     *
     * @param pos normalized cue ball position ([0..1])
     */
    public void setCueBall(Vector2 pos) {
        if (poolTable != null) poolTable.setCueBall(pos);
    }

    /**
     * Updates the object ball positions in normalized table coordinates.
     *
     * @param positions array of normalized ball positions ([0..1])
     */
    public void setBalls(Vector2[] positions) {
        if (poolTable != null) poolTable.setObjectBalls(positions);
    }


    /**
     * Shows the pool minigame UI.
     */
    public void show() {
        running = true;
        setVisible(true);
    }

    /**
     * Hides the pool minigame UI and triggers a stop event.
     */
    public void hide() {
        setVisible(false);
        entity.getEvents().trigger(GameEvents.STOP);
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


    /**
     * Loads all texture and atlas assets for the pool UI.
     */
    private void loadAssets() {
        tableTex = ServiceLocator.getResourceService().getAsset("images/pool/table.png", Texture.class);
        TextureAtlas ballAtlas = ServiceLocator.getResourceService().getAsset("images/pool/balls.atlas", TextureAtlas.class);
        cueTex = ServiceLocator.getResourceService().getAsset("images/pool/cue.png", Texture.class);

        ballTextures = new TextureRegion[16];
        for (int i = 1; i <= 15; i++) {
            ballTextures[i] = ballAtlas.findRegion("ball_" + i);
        }
        Texture cueBallTextureRaw = ServiceLocator.getResourceService().getAsset("images/pool/cue_ball.png", Texture.class);
        cueBallTex = new TextureRegion(cueBallTextureRaw);
    }

    /**
     * Builds the dimmer overlay and panel background layers.
     */
    private void buildBackdrop() {
        pixel = makeSolid(Color.WHITE);

        dimmer = makeSolidImage(pixel, 0, 0, stage.getWidth(), stage.getHeight(), new Color(0, 0, 0, 0.6f));
        dimmer.setFillParent(true);
        dimmer.setTouchable(Touchable.disabled);
        stage.addActor(dimmer);

        frame = makeSolidImage(pixel, 0, 0, PANEL_W + 8, PANEL_H + 8, Color.BLACK);
        frame.setPosition((stage.getWidth() - frame.getWidth()) / 2f, (stage.getHeight() - frame.getHeight()) / 2f);
        frame.setTouchable(Touchable.disabled);
        stage.addActor(frame);

        background = makeSolidImage(pixel, 0, 0, PANEL_W, PANEL_H, PANEL_COLOR);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2f, (stage.getHeight() - background.getHeight()) / 2f);
        background.setTouchable(Touchable.disabled);
        stage.addActor(background);
    }

    /**
     * Builds the root table container for the UI layout.
     */
    private void buildRoot() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(ROOT_PAD);
        root.defaults().pad(CELL_PAD);
        root.setTouchable(Touchable.enabled);
        stage.addActor(root);
    }

    /**
     * Builds the header section containing the title and score HUD.
     */
    private void buildHeader() {
        Label titleLbl = label("8-Ball Pool", TITLE_COLOR, 1.5f);
        p1Lbl = label("P1: 0", GOLD);
        p2Lbl = label("P2: 0", GOLD);
        turnLbl = label("Turn: P1", TURN_COLOR);
        foulLbl = label("", FOUL_COLOR);

        Table hdr = new Table();
        hdr.add(titleLbl).left().expandX();
        hdr.add(turnLbl).center().padRight(10);
        hdr.add(p1Lbl).right().padRight(10);
        hdr.add(p2Lbl).right();
        root.add(hdr).growX().row();

        Image divider = makeSolidImage(pixel, 0, 0, PANEL_W - 40f, 2f, new Color(1f, 1f, 1f, 0.08f));
        root.add(divider).width(PANEL_W - 40f).height(2f).row();

        Table foulRow = new Table();
        foulRow.add(foulLbl).left().expandX();
        root.add(foulRow).growX().padTop(2).row();
    }

    /**
     * Builds and embeds the interactive {@link PoolTable} widget.
     */
    private void buildTableArea() {
        float aspect = (float) tableTex.getWidth() / tableTex.getHeight();
        float targetW = PANEL_W - 40f;
        float targetH = targetW / aspect;

        poolTable = new PoolTable(
                tableTex,
                ballTextures,
                cueTex,
                cueBallTex,
                () -> powerSlider.getValue(),
                (cuePosNorm, dirNorm, desiredPx, ballPx) ->
                        controller != null
                                ? controller.capGuideLenPx(cuePosNorm, dirNorm, desiredPx, ballPx)
                                : desiredPx
        );
        poolTable.setSize(targetW, targetH);
        poolTable.setOrigin(Align.center);
        poolTable.setTouchable(Touchable.enabled);

        // Pointer aiming support
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

    /**
     * Builds the footer controls: power slider, guide toggle, and buttons.
     */
    private void buildFooter() {
        powerSlider = new Slider(0f, 1f, 0.01f, false, skin);
        powerSlider.setValue(0.5f);

        guideToggle = new CheckBox(" Guide", skin);
        guideToggle.setChecked(true);
        guideToggle.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                poolTable.setGuideVisible(guideToggle.isChecked());
            }
        });

        // Control buttons
        TextButton startBtn = new TextButton("Rack", skin);
        shootBtn = new TextButton("Shoot", skin);
        TextButton resetBtn = new TextButton("Reset", skin);
        TextButton closeBtn = new TextButton("Close", skin);

        // Button listeners
        startBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                running = true;
                if (controller != null) controller.onStart();
                entity.getEvents().trigger(GameEvents.START);
                clearFoul();
            }
        });

        shootBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!running) return;
                Vector2 dir = poolTable.getAimDir();
                if (dir.isZero(1e-4f)) return;
                float power = powerSlider.getValue();
                if (controller != null) controller.onShoot(dir.x, dir.y, power);
                entity.getEvents().trigger(GameEvents.SHOOT, dir.x, dir.y, power);
                poolTable.kickbackCue(Interpolation.sine);
            }
        });

        resetBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (controller != null) controller.onReset();
                entity.getEvents().trigger(GameEvents.RESET);
                clearFoul();
            }
        });

        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (controller != null) controller.onStop();
                entity.getEvents().trigger(GameEvents.STOP);
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


    /**
     * Updates interactivity (guide toggle, shoot button, cue visibility) per frame.
     */
    private void updateUiState() {
        if (controller == null || poolTable == null) return;

        boolean shotActive = controller.isShotActive();
        boolean guideAllowed = !shotActive;

        boolean guideCheckbox = (guideToggle == null) || guideToggle.isChecked();
        poolTable.setGuideVisible(guideCheckbox && guideAllowed);

        if (powerSlider != null) {
            powerSlider.setVisible(guideAllowed);
            powerSlider.setDisabled(!guideAllowed);
        }

        poolTable.setCueVisible(!shotActive);

        if (shootBtn != null) {
            boolean hasAim = !poolTable.getAimDir().isZero(1e-4f);
            shootBtn.setDisabled(!hasAim);
            shootBtn.setTouchable(hasAim ? Touchable.enabled : Touchable.disabled);
        }
    }

    /**
     * Wires game event listeners to update the HUD reactively.
     */
    private void wireHudEvents() {
        entity.getEvents().addListener(GameEvents.TURN, (Integer current, Integer p1, Integer p2) -> {
            setScores(p1, p2);
            setTurn((current != null ? current : 1) - 1);
            clearFoul();
            shotTakenThisTurn = false;
            updateUiState();
        });

        entity.getEvents().addListener(GameEvents.SCORE, (Integer current, Integer p1, Integer p2) -> {
            setScores(p1, p2);
        });

        entity.getEvents().addListener(GameEvents.FOUL, (Integer player, String reason) -> {
            setFoul("Foul on P" + (player == null ? "?" : player)
                    + ((reason == null || reason.isEmpty()) ? "" : " (" + reason + ")"));
        });

        entity.getEvents().addListener(GameEvents.SHOOT, (Float dx, Float dy, Float power) -> {
            shotTakenThisTurn = true;
            updateUiState();
        });
    }


    private Label label(String text, Color color) {
        return label(text, color, 1f);
    }

    private Label label(String text, Color color, float scale) {
        Label.LabelStyle base = skin.get(Label.LabelStyle.class);
        Label.LabelStyle style = new Label.LabelStyle(base);
        style.fontColor = color;
        Label lbl = new Label(text, style);
        lbl.setFontScale(scale);
        return lbl;
    }


    /**
     * Controller is the only way the UI queries physics/world.
     * UI passes normalised cue position and aim direction, desired guide length in pixels,
     * plus current ball pixel radius; the controller returns the clamped guide length in pixels.
     */
    public interface Controller {
        /**
         * Starts a new rack or resets the current one.
         */
        void onStart();

        /**
         * Fires a shot with the given aim vector and power.
         *
         * @param dirX  normalized X direction of aim
         * @param dirY  normalized Y direction of aim
         * @param power power scalar in [0..1]
         */
        void onShoot(float dirX, float dirY, float power);

        /**
         * Resets the rack positions without leaving the UI.
         */
        void onReset();

        /**
         * Stops the minigame and hides the UI.
         */
        void onStop();

        /**
         * Returns the maximum guide length in pixels, capped using physics raycasts.
         *
         * @param cuePosNorm   cue position in normalized table space ([0..1])
         * @param dirNorm      normalized aim direction
         * @param desiredLenPx desired guide length in pixels
         * @param ballPx       cue ball radius in pixels
         * @return clamped guide length in pixels
         */
        float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx);

        /**
         * @return true while a shot is in progress (balls are moving).
         */
        boolean isShotActive();
    }
}
