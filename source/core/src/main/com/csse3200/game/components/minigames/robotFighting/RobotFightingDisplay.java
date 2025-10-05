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
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.screens.ItemScreenDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

/**
 * Display class for the "Clanker Royale" minigame.
 * Handles UI transitions: welcome → betting → main game.
 */
public class RobotFightingDisplay extends UIComponent {
    // --- UI Constants ---
    private static final float PANEL_W = 1280f;
    private static final float PANEL_H = 720f;
    private static final float CELL_W = 150f;
    private static final float CELL_H = 180f;

    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");

    // --- UI Elements ---
    private Table root, welcomeRoot, betRoot, footer;
    private Image frame, dimmer, background;
    private Texture pixelTex;
    private Label currencyLabel, narratorLabel, dialogueLabel;
    private TextField betInput;
    private ItemScreenDisplay itemPopup;
    private AnimatedImage competitor1;
    private AnimatedImage competitor2;

    // --- Game Elements ---
    private Robot selectedFighter;

    // --- References ---
    private final GameArea game = ServiceLocator.getGameArea();

    @Override
    public void create() {
        super.create();
        itemPopup = new ItemScreenDisplay();
        entity.addComponent(itemPopup);

        buildBackdrop();
        buildWelcomeScreen();
        buildBetScreen();
        buildMainUI();

        subscribeCurrencyUpdates();
        hide();
    }

    // ------------------------------------------------------------------------
    // UI Construction
    // ------------------------------------------------------------------------

    private void buildBackdrop() {
        pixelTex = makeSolidTexture(Color.WHITE);

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

    private void buildBetScreen() {
        betRoot = new Table();
        betRoot.setSize(PANEL_W, PANEL_H);
        betRoot.setPosition(background.getX(), background.getY());
        betRoot.center().pad(20);

        Label title = makeLabel("Place Your Bet", TITLE_COLOR, 1.8f);
        currencyLabel = makeLabel("", GOLD, 1.2f);
        updateBalanceLabel(currencyLabel);

        betInput = new TextField("", skin);
        betInput.setMessageText("Enter bet amount");
        betInput.setAlignment(Align.center);
        betInput.setDisabled(true);

        TextButton plus10Btn = makeButton("+10", () -> adjustBet(10));
        TextButton minus10Btn = makeButton("-10", () -> adjustBet(-10));
        TextButton confirmBtn = makeButton("Confirm Bet", this::onConfirmBet);

        Table betControls = new Table();
        betControls.add(minus10Btn).width(80).height(50).padRight(10);
        betControls.add(betInput).width(150).padRight(10);
        betControls.add(plus10Btn).width(80).height(50);

        betRoot.add(title).padBottom(20).row();
        betRoot.add(betControls).padBottom(20).row();
        betRoot.add(confirmBtn).width(200).height(60).padBottom(20).row();
        betRoot.add(currencyLabel).expand().bottom().right().pad(10).row();

        stage.addActor(betRoot);
        betRoot.setVisible(false);
    }

    private void buildMainUI() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(20);
        root.defaults().pad(10);

        Label title = makeLabel("Clanker Royale", TITLE_COLOR, 1.8f);
        Image divider = makeImage(pixelTex, new Color(1f, 1f, 1f, 0.08f),
                PANEL_W - 40f, 2f, 0, 0);

        root.add(title).padBottom(10).row();
        root.add(divider).padBottom(8).row();

        Table grid = new Table();
        grid.defaults().pad(12).size(CELL_W, CELL_H).uniform(true);
        root.add(grid).row();

        narratorLabel = makeLabel("", Color.CYAN, 1.6f);
        narratorLabel.setAlignment(Align.center);

        footer = new Table();
        TextureRegionDrawable bg = new TextureRegionDrawable(makeSolidTexture(Color.BLACK));
        footer.setBackground(bg);
        footer.center();
        footer.add(narratorLabel).expandX().center();

        root.add().expandY().row();
        root.add(footer).growX().padTop(8f).padBottom(10f).bottom().row();
        stage.addActor(root);
        root.setVisible(false);
    }

    // ------------------------------------------------------------------------
    // Event Handlers
    // ------------------------------------------------------------------------

    private void onStartGame() {
        setScreenVisible(welcomeRoot, false);
        setScreenVisible(betRoot, true);
    }

    private void onConfirmBet() {
        int betAmount = parseIntOrZero(betInput.getText());
        System.out.println("Player bet: " + betAmount);

        setScreenVisible(betRoot, false);
        setScreenVisible(root, true);

        narrateMain();
    }

    private void onFighterSelect() {

    }

    private void adjustBet(int delta) {
        int currentBet = parseIntOrZero(betInput.getText());
        int newBet = Math.max(0, currentBet + delta);
        betInput.setText(String.valueOf(newBet));

        int balance = getBalance();
        currencyLabel.setText("Balance Remaining: $" + Math.max(0, balance - newBet));
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

    private void spawnFighters() {
        float arenaX = background.getX();
        float arenaY = background.getY();
        float arenaWidth = background.getWidth();
        float arenaHeight = background.getHeight();

        // Vertically center the fighters relative to the background
        float fighterHeight = 300f;
        float arenaCenterY = arenaY + (arenaHeight - fighterHeight) / 2f;

        competitor1 = createFighter(
                Robot.DEEP_SPIN,
                "float",
                arenaX + arenaWidth - 350f,  // near the right edge
                arenaCenterY,
                300,
                300
        );

        competitor2 = createFighter(
                Robot.GHOST_GPT,
                "float",
                arenaX + 50f,                // near the left edge
                arenaCenterY,
                300,
                300
        );

        stage.addActor(competitor1);
        stage.addActor(competitor2);
    }

    // ------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------

    private int getBalance() {
        var inv = game.getPlayer().getComponent(InventoryComponent.class);
        return (inv != null) ? inv.getProcessor() : 0;
    }

    private void updateBalanceLabel(Label label) {
        label.setText("Balance Remaining: $" + getBalance());
    }

    private int parseIntOrZero(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void showTypewriterText(Label label, String fullText, float interval) {
        label.setText("");
        char[] chars = fullText.toCharArray();
        final int[] i = {0};

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (i[0] < chars.length) {
                    label.setText(label.getText() + String.valueOf(chars[i[0]++]));
                } else cancel();
            }
        }, 0, interval);
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

    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Creates an AnimatedImage fighter from a texture atlas and animation name.
     *
     * @param robot         enum of robot type
     * @param animName      name of the animation region inside the atlas
     * @param x             X position on stage
     * @param y             Y position on stage
     * @param width         desired display width
     * @param height        desired display height
     * @return an AnimatedImage ready to be added to the stage
     */
    private AnimatedImage createFighter(Robot robot, String animName,
                                        float x, float y, float width, float height) {
        String atlasPath = robot.getAtlas();
        // Load atlas from the resource service
        TextureAtlas atlas = ServiceLocator.getResourceService().getAsset(atlasPath, TextureAtlas.class);
        if (atlas == null) {
            throw new IllegalStateException("Atlas not loaded: " + atlasPath);
        }

        // Build looping animation
        Animation<TextureRegion> animation =
                new Animation<>(0.1f, atlas.findRegions(animName), Animation.PlayMode.LOOP);

        // Create the animated actor
        AnimatedImage fighter = new AnimatedImage(animation);
        fighter.setSize(width, height);
        fighter.setPosition(x, y);

        // Add a simple fade-in effect
        fighter.addAction(Actions.sequence(
                Actions.alpha(0f),
                Actions.fadeIn(1f)
        ));

        fighter.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                switch (robot) {
                    case DEEP_SPIN:
                        selectedFighter = Robot.DEEP_SPIN;
                        break;
                    case GHOST_GPT:
                        selectedFighter = Robot.GHOST_GPT;
                        break;
                }
                onFighterSelect();
            }
        });

        return fighter;
    }


    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------

    public void show() {
        updateBalanceLabel(currencyLabel);
        betInput.setText("0");

        for (Actor actor : new Actor[]{frame, background, dimmer, welcomeRoot})
            setScreenVisible(actor, true);
    }

    public void hide() {
        for (Actor actor : new Actor[]{frame, background, dimmer, root, welcomeRoot, betRoot, competitor1, competitor2})
            setScreenVisible(actor, false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

    @Override
    public void dispose() {
        for (Actor actor : new Actor[]{root, dimmer, frame, background, welcomeRoot, betRoot})
            if (actor != null) actor.remove();
        if (pixelTex != null) pixelTex.dispose();
        if (itemPopup != null) itemPopup.dispose();
        super.dispose();
    }

    private void subscribeCurrencyUpdates() {
        game.getPlayer().getEvents().addListener("updateProcessor", (Integer p) -> {
            if (currencyLabel != null) currencyLabel.setText("Balance Remaining: $" + p);
        });
    }
}
