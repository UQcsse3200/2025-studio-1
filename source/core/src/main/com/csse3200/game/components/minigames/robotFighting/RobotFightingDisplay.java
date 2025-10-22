package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.math.MathUtils;

/**
 * Display class for the "Clanker Royale" minigame.
 * Handles UI transitions: welcome → main game.
 */
public class RobotFightingDisplay extends UIComponent {
    // --- UI Constants ---
    private static final float PANEL_W = 1280f;
    private static final float PANEL_H = 720f;
    private static final float CELL_W = 150f;
    private static final float CELL_H = 180f;

    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");

    // --- UI Elements ---
    private Table root, welcomeRoot, footer;
    private Image frame, dimmer, background;
    private Texture pixelTex;
    private Label narratorLabel;
    private ProgressBar healthBar1;
    private ProgressBar healthBar2;
    private AnimatedImage competitor1;
    private AnimatedImage competitor2;

    // --- Game Elements ---
    private Robot selectedFighter;
    private boolean active = false;
    private Timer.Task typewriterTask;

    // --- Motion fields ---
    private Timer.Task motionTask;
    private float motionTime = 0f;
    private boolean motionPaused = false;
    private static final float MOTION_AMPLITUDE = 30f; // side-to-side pixel shift
    private static final float MOTION_SPEED = 2f;      // oscillation speed

    @Override
    public void create() {
        super.create();

        buildBackdrop();
        buildWelcomeScreen();
        buildMainUI();
        buildCompetitors();

        hide();
    }

    // ------------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------------

    private void buildBackdrop() {
        pixelTex = makeSolidTexture();

        dimmer = makeImage(pixelTex, new Color(0f, 0f, 0f, 0.6f), stage.getWidth(), stage.getHeight(), 0, 0);
        frame = makeImage(pixelTex, Color.BLACK, PANEL_W + 8, PANEL_H + 8,
                (stage.getWidth() - PANEL_W) / 2f - 4, (stage.getHeight() - PANEL_H) / 2f - 4);
        background = makeImage(pixelTex, PANEL_COLOR, PANEL_W, PANEL_H,
                (stage.getWidth() - PANEL_W) / 2f, (stage.getHeight() - PANEL_H) / 2f);
    }

    private void buildWelcomeScreen() {
        welcomeRoot = new Table();
        welcomeRoot.setFillParent(true);
        welcomeRoot.center().pad(20);

        Label title = makeLabel("Welcome to Clanker Royale!", TITLE_COLOR, 2f);
        Label subtitle = makeLabel("Press start to enter the arena.", Color.WHITE, 1.2f);

        TextButton startBtn = makeButton("Start Game", this::onStartGame);

        welcomeRoot.add(title).padBottom(20).row();
        welcomeRoot.add(subtitle).padBottom(40).row();
        welcomeRoot.add(startBtn).width(200).height(60).row();

        stage.addActor(welcomeRoot);
    }

    private void buildMainUI() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);

        Label title = makeLabel("Clanker Royale", TITLE_COLOR, 1.8f);

        buildHealthBars();

        // Create a header row table
        Table headerRow = new Table();
        headerRow.add(healthBar1).width(300).height(20).padRight(150);
        headerRow.add(title).expandX().center();
        headerRow.add(healthBar2).width(300).height(20).padLeft(150);

        root.add(headerRow).colspan(2).padBottom(10).row();

        Image divider = makeImage(pixelTex, new Color(1f, 1f, 1f, 0.08f),
                PANEL_W - 40f, 2f, 0, 0);
        root.add(divider).padBottom(8).row();

        Table grid = new Table();
        grid.defaults().pad(12).size(CELL_W, CELL_H).uniform(true);
        root.add(grid).row();

        narratorLabel = makeLabel("", Color.CYAN, 1.6f);
        narratorLabel.setAlignment(Align.center);

        footer = new Table();
        Drawable bg = makeSolidDrawable(Color.BLACK, (int) PANEL_W, 40);
        footer.setBackground(bg);
        footer.center();
        footer.add(narratorLabel).expandX().center();

        root.add().expandY().row();
        root.add(footer).growX().padTop(8f).padBottom(10f).bottom().row();

        stage.addActor(root);
        root.setVisible(false);
    }


    private void buildCompetitors() {
        float arenaX = background.getX();
        float arenaY = background.getY();
        float arenaWidth = background.getWidth();
        float arenaHeight = background.getHeight();

        // Vertically center the fighters relative to the background
        float fighterHeight = 300f;
        float arenaCenterY = arenaY + (arenaHeight - fighterHeight) / 2f;

        competitor1 = createFighter(
                Robot.DEEP_SPIN,
                arenaX + arenaWidth - 250f,
                arenaCenterY
        );

        competitor2 = createFighter(
                Robot.GHOST_GPT,
                arenaX + 50f,
                arenaCenterY
        );

        stage.addActor(competitor1);
        stage.addActor(competitor2);
        setScreenVisible(competitor1, false);
        setScreenVisible(competitor2, false);
    }

    private void buildHealthBars() {
        ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle();
        healthBarStyle.background = makeSolidDrawable(Color.DARK_GRAY, 300, 20);
        healthBarStyle.knobBefore = makeSolidDrawable(Color.RED, 300, 20);

        healthBar1 = new ProgressBar(0f, 100f, 1f, false, healthBarStyle);
        healthBar2 = new ProgressBar(0f, 100f, 1f, false, healthBarStyle);

        healthBar1.setValue(100f);
        healthBar2.setValue(100f);

        setScreenVisible(healthBar1, false);
        setScreenVisible(healthBar2, false);
    }


    // ------------------------------------------------------------------------
    // Event Handlers
    // ------------------------------------------------------------------------

    private void onStartGame() {
        setScreenVisible(welcomeRoot, false);
        narrateMain();
        setScreenVisible(root, true);
    }

    private void onFighterSelect() {
        for (Actor f : new Actor[]{competitor1, competitor2}) {
            if (f == null) return;

            f.addAction(Actions.scaleTo(0.8f, 0.8f, 0.5f));

            startFighterMotion();

            f.clearListeners();

            narrateFightStart();
        }
        setScreenVisible(healthBar1, true);
        setScreenVisible(healthBar2, true);
    }

    private void onFightStart() {
        entity.getEvents().trigger("robotFighting:startFight");
        enableDialogueMode();
        narratorLabel.setText("");

        footer.clear();

        // Create the center "ENCOURAGE!" button
        TextButton encourageBtn = makeButton("ENCOURAGE!", () -> entity.getEvents().trigger("robotFighting:encourage"));
        encourageBtn.getLabel().setFontScale(1.2f);

        Table centerContainer = new Table();
        centerContainer.add(encourageBtn)
                .center()
                .width(200)
                .height(60)
                .pad(10);

        float labelWidth = PANEL_W * 0.35f;
        Table narratorContainer = new Table();
        narratorContainer.add(narratorLabel)
                .width(labelWidth)
                .center();


        if (getChosenActor() == competitor1) {
            footer.add(narratorContainer).left().padLeft(20);
            footer.add(centerContainer).center().expandX();
            footer.add().width(labelWidth).right().padRight(20);
        } else {
            footer.add().width(labelWidth).left().padLeft(20);
            footer.add(centerContainer).center().expandX();
            footer.add(narratorContainer).right().padRight(20);
        }

        footer.invalidateHierarchy();
    }

    private void narrateMain() {
        narratorLabel.addAction(Actions.sequence(
                Actions.run(() -> showTypewriterText(narratorLabel, "Welcome to Clanker Royale!", 0.05f)),
                Actions.delay(2f),
                Actions.run(() -> showTypewriterText(narratorLabel, "Please pick your Clanker.", 0.05f)),
                Actions.delay(1f),
                Actions.run(this::spawnFighters)
        ));
    }

    private void narrateFightStart() {
        showTypewriterTextChain(new String[]{
                "Clankers to your corners.",
                "3..",
                "2.",
                "1",
                "CLANK!"
        }, 0.05f, this::onFightStart);
    }

    private void spawnFighters() {
        if (!active) {
            return;
        }
        for (Actor f : new Actor[]{competitor1, competitor2}) {

            setScreenVisible(f, true);
            f.addAction(Actions.sequence(
                    Actions.alpha(0f),
                    Actions.fadeIn(1f)
            ));
        }
    }

    private void startFighterMotion() {
        if (motionTask != null) motionTask.cancel();
        motionTime = 0f;

        float baseX1 = competitor1.getX();
        float baseX2 = competitor2.getX();

        motionTask = new Timer.Task() {
            @Override
            public void run() {
                if (!active) {
                    cancel();
                    return;
                }
                if (motionPaused) {
                    return;
                }

                motionTime += 0.05f; // time increment (update every 50 ms)
                float offset = (float) Math.sin(motionTime * MOTION_SPEED) * MOTION_AMPLITUDE;

                competitor1.setX(baseX1 + offset);
                competitor2.setX(baseX2 - offset);
            }
        };

        Timer.schedule(motionTask, 0f, 0.05f);
    }

    /**
     * Plays the fighter's attack animation and triggers a hit effect.
     *
     * @param fighter the attacking actor
     */
    public void playAttackAnimation(Actor fighter) {
        motionPaused = true;

        float punchDistance = (fighter == competitor2) ? 150f : -150f;  // move 30px toward opponent
        float punchSpeed = 0.14f;   // quick jab

        fighter.addAction(Actions.sequence(
                Actions.moveBy(punchDistance, 0f, punchSpeed),
                Actions.run(() -> playHitAnimation(getOtherActor(fighter))),
                Actions.moveBy(-punchDistance, 0f, punchSpeed),
                Actions.run(() -> motionPaused = false)
        ));
    }

    private void playHitAnimation(Actor fighter) {

        float shake = (fighter == competitor2) ? 15f : -15f;
        float speed = 0.03f;

        fighter.addAction(Actions.sequence(
                Actions.moveBy(shake, 0f, speed),
                Actions.moveBy(-shake, 0f, speed),
                Actions.moveBy(shake, 0f, speed),
                Actions.moveBy(-shake, 0f, speed)
        ));

        fighter.addAction(Actions.sequence(
                Actions.color(Color.RED, 0.05f),
                Actions.delay(0.15f),
                Actions.color(Color.WHITE, 0.1f)
        ));
    }

    public void setHealthFighter(Actor fighter, float healthPercent) {
        if (healthBar1 != null && fighter == competitor1) {
            healthBar1.setValue(MathUtils.clamp(healthPercent, 0f, 100f));
        } else if (healthBar2 != null && fighter == competitor2) {
            healthBar2.setValue(MathUtils.clamp(healthPercent, 0f, 100f));
        }
    }

    /**
     * Displays an encouragement message in the narrator label.
     *
     * @param encouragingMessage text to show on screen
     */
    public void encourageFighter(String encouragingMessage) {
        narratorLabel.setText("");
        String quoted = "\"" + encouragingMessage + "\"";
        showTypewriterText(narratorLabel, quoted, 0.05f);
    }


    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    public void playExplosionEffect(Actor fighter) {
        float centerX = fighter.getX() + fighter.getWidth() / 2f;
        float centerY = fighter.getY() + fighter.getHeight() / 2f;

        float explosionSize = 100f;
        Image explosion = makeImage(pixelTex, Color.ORANGE,
                explosionSize, explosionSize,
                centerX - explosionSize / 2f,
                centerY - explosionSize / 2f);
        explosion.setOrigin(Align.center);
        explosion.setColor(new Color(1f, 0.5f, 0f, 0.8f));

        // Flash background to simulate shockwave
        background.addAction(Actions.sequence(
                Actions.color(Color.valueOf("FFECB3"), 0.1f), // light yellow flash
                Actions.delay(0.1f),
                Actions.color(PANEL_COLOR, 0.2f)
        ));

        explosion.addAction(Actions.sequence(
                Actions.parallel(
                        Actions.scaleTo(3f, 3f, 0.5f),
                        Actions.color(Color.RED, 0.3f)
                ),
                Actions.fadeOut(0.4f),
                Actions.run(explosion::remove)
        ));

        stage.addActor(explosion);
    }


    /**
     * Switches the narrator label to a dialogue-style appearance:
     * white background, black text, and a slight padding.
     * Ensures the dialogue text is displayed horizontally (no line wrap).
     */
    private void enableDialogueMode() {
        if (narratorLabel == null) return;

        narratorLabel.getStyle().fontColor = Color.BLACK;

        Drawable whiteBg = makeSolidDrawable(Color.WHITE,
                (int) (PANEL_W - 40),
                60);

        footer.setBackground(whiteBg);

        narratorLabel.setAlignment(Align.center);
        narratorLabel.setWrap(false);
        narratorLabel.setEllipsis(true);
        narratorLabel.setFontScale(1.1f);

        narratorLabel.setWidth(500f);

        narratorLabel.invalidateHierarchy();

    }

    /**
     * Restores the footer and narrator label to their normal pre-fight state.
     * Removes the "ENCOURAGE!" button and resets styles and layout.
     */
    private void resetPostFightUI() {
        if (footer == null || narratorLabel == null) return;

        footer.clear();

        Drawable defaultBg = makeSolidDrawable(Color.BLACK, (int) PANEL_W, 40);
        footer.setBackground(defaultBg);

        narratorLabel.getStyle().fontColor = Color.CYAN;
        narratorLabel.setFontScale(1.6f);
        narratorLabel.setAlignment(Align.center);
        narratorLabel.setWrap(false);
        narratorLabel.setText("");

        footer.center();
        footer.add(narratorLabel).expandX().center();

        footer.invalidateHierarchy();
    }

    private void showTypewriterText(Label label, String fullText, float interval) {
        if (typewriterTask != null) {
            typewriterTask.cancel(); // stop any previous typewriter effect
        }

        final int[] i = {0};

        label.setText("");
        final String full = fullText;
        typewriterTask = new Timer.Task() {
            @Override
            public void run() {
                if (i[0] < full.length()) {
                    label.setText(full.substring(0, i[0] + 1)); // append only up to current index
                    i[0]++;
                } else {
                    cancel();
                    typewriterTask = null;
                }
            }
        };


        Timer.schedule(typewriterTask, 0, interval);
    }


    private void showTypewriterTextChain(String[] lines, float interval, Runnable onComplete) {
        if (lines.length == 0) {
            onComplete.run();
            return;
        }

        showTypewriterText(narratorLabel, lines[0], interval);

        float delay = lines[0].length() * interval + 0.5f; // extra pause
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                showTypewriterTextChain(
                        java.util.Arrays.copyOfRange(lines, 1, lines.length),
                        interval,
                        onComplete
                );
            }
        }, delay);
    }

    private void setScreenVisible(Actor actor, boolean visible) {
        if (actor != null) actor.setVisible(visible);
    }

    private TextButton makeButton(String text, Runnable onClick) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                onClick.run();
            }
        });
        return button;
    }

    private Label makeLabel(String text, Color color, float scale) {
        Label.LabelStyle style = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        style.fontColor = color;
        Label label = new Label(text, style);
        label.setFontScale(scale);
        return label;
    }

    private Image makeImage(Texture tex, Color color, float w, float h, float x, float y) {
        Image img = new Image(new TextureRegionDrawable(new TextureRegion(tex)));
        img.setSize(w, h);
        img.setPosition(x, y);
        img.setColor(color);
        stage.addActor(img);
        return img;
    }

    private static Texture makeSolidTexture() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Drawable makeSolidDrawable(Color color, int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }


    /**
     * Creates an AnimatedImage fighter from a texture atlas and animation name.
     *
     * @param robot enum of robot type
     * @param x     X position on stage
     * @param y     Y position on stage
     * @return an AnimatedImage ready to be added to the stage
     */
    private AnimatedImage createFighter(Robot robot,
                                        float x, float y) {
        String atlasPath = robot.getAtlas();
        // Load atlas from the resource service
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
        if (atlas == null) {
            throw new IllegalStateException("Atlas not loaded: " + atlasPath);
        }

        // Build looping animation
        Animation<TextureRegion> animation =
                new Animation<>(0.1f, atlas.findRegions("float"), Animation.PlayMode.LOOP);

        // Create the animated actor
        AnimatedImage fighter = new AnimatedImage(animation);
        fighter.setSize(250, 250);
        fighter.setPosition(x, y);

        // Add a simple fade-in effect

        fighter.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                switch (robot) {
                    case DEEP_SPIN -> selectedFighter = Robot.DEEP_SPIN;
                    case GHOST_GPT -> selectedFighter = Robot.GHOST_GPT;
                }
                onFighterSelect();
                entity.getEvents().trigger("robotFighting:choose", robot);
            }
        });
        fighter.setTouchable(Touchable.enabled);
        return fighter;
    }

    /**
     * Returns the actor representing the currently selected fighter.
     *
     * @return the selected fighter actor, or null if none selected
     */
    public Actor getChosenActor() {
        if (selectedFighter == Robot.GHOST_GPT) {
            return competitor1;
        } else if (selectedFighter == Robot.DEEP_SPIN) {
            return competitor2;
        }
        return null;
    }

    /**
     * Returns the opponent actor opposite of the provided fighter.
     *
     * @param fighter the current fighter
     * @return the opposing fighter actor
     */
    public Actor getOtherActor(Actor fighter) {
        if (fighter == competitor2) {
            return competitor1;
        } else if (fighter == competitor1) {
            return competitor2;
        }
        return null;
    }


    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------

    /**
     * Shows all minigame UI panels and activates interactions.
     */
    public void show() {
        active = true;
        for (Actor actor : new Actor[]{frame, background, dimmer, welcomeRoot})
            setScreenVisible(actor, true);
    }

    /**
     * Hides all panels and disables the UI.
     */
    public void hide() {
        active = false;
        for (Actor actor : new Actor[]{frame, background, dimmer, root, welcomeRoot, competitor1, competitor2})
            setScreenVisible(actor, false);
        resetPostFightUI();
        healthBar1.setValue(100f);
        healthBar2.setValue(100f);
    }

    /**
     * Draws the stage and all visible UI elements.
     *
     * @param batch the sprite batch used to draw
     */
    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    /**
     * Disposes of textures and UI actors to free memory.
     */
    @Override
    public void dispose() {
        for (Actor actor : new Actor[]{root, dimmer, frame, background, welcomeRoot})
            if (actor != null) actor.remove();
        if (pixelTex != null) pixelTex.dispose();
        super.dispose();
    }
}
