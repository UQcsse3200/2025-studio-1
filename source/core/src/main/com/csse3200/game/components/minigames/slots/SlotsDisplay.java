package com.csse3200.game.components.minigames.slots;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.screens.ItemScreenDisplay;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import java.util.Random;

public class SlotsDisplay extends UIComponent {

    // Panel sizing/colors (mirrors RobotFighting style)
    private static final float PANEL_W = 1280f;
    private static final float PANEL_H = 720f;
    private static final Color PANEL_COLOR = Color.valueOf("0B132B");
    private static final Color TITLE_COLOR = Color.valueOf("00E5FF");
    private static final Color GOLD = Color.valueOf("FFD54F");

    private Table root;
    private Image frame;
    private Image dimmer;
    private Image background;
    private Texture pixelTex;

    private Label currencyLabel;
    private TextField betInput;
    private Label rowLabel;
    private Label resultLabel;

    // Simple internal balance to mirror your console version.
    private int balance = 100;

    // Slot machine state
    private final String[] symbols = {"🍒", "🍉", "🍋", "🔔", "⭐"};
    private final Random random = new Random();

    // Optional popup (kept for parity with other UIs)
    private ItemScreenDisplay itemPopup;

    @Override
    public void create() {
        super.create();

        itemPopup = new ItemScreenDisplay();
        entity.addComponent(itemPopup);

        buildBackdrop();
        buildRootTable();
        addHeader();
        buildMain();
        addFooter();
        hide();
    }

    private void buildBackdrop() {
        pixelTex = makeSolidTexture(Color.WHITE);

        dimmer = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        dimmer.setFillParent(true);
        dimmer.setColor(0f, 0f, 0f, 0.6f);
        stage.addActor(dimmer);

        frame = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        frame.setSize(PANEL_W + 8, PANEL_H + 8);
        frame.setPosition((stage.getWidth() - frame.getWidth()) / 2f,
                (stage.getHeight() - frame.getHeight()) / 2f);
        frame.setColor(Color.BLACK);
        stage.addActor(frame);

        background = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        background.setSize(PANEL_W, PANEL_H);
        background.setPosition((stage.getWidth() - background.getWidth()) / 2f,
                (stage.getHeight() - background.getHeight()) / 2f);
        background.setColor(PANEL_COLOR);
        stage.addActor(background);
    }

    private void buildRootTable() {
        root = new Table();
        root.setSize(PANEL_W, PANEL_H);
        root.setPosition(background.getX(), background.getY());
        root.top().pad(24f);
        root.defaults().pad(10f);
        stage.addActor(root);
    }

    private void addHeader() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TITLE_COLOR;

        Label title = new Label("Java Slots", titleStyle);
        title.setFontScale(1.8f);
        root.add(title).padBottom(8f).row();

        Image divider = new Image(new TextureRegionDrawable(new TextureRegion(pixelTex)));
        divider.setColor(1f, 1f, 1f, 0.08f);
        root.add(divider).width(PANEL_W - 40f).height(2f).padBottom(8f).row();
    }

    private void buildMain() {
        // Bet row
        Table betRow = new Table();

        Label betLabel = new Label("Bet:", skin);
        betInput = new TextField("", skin);
        betInput.setMessageText("Enter bet amount");
        betInput.setAlignment(Align.center);

        TextButton minusBtn = new TextButton("-10", skin);
        TextButton plusBtn = new TextButton("+10", skin);
        minusBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                adjustBet(-10);
            }
        });
        plusBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                adjustBet(10);
            }
        });

        betRow.add(betLabel).padRight(8f);
        betRow.add(minusBtn).width(80).height(48).padRight(8f);
        betRow.add(betInput).width(160).padRight(8f);
        betRow.add(plusBtn).width(80).height(48);

        root.add(betRow).padTop(10f).row();

        // Spin button
        TextButton spinBtn = new TextButton("Spin", skin);
        spinBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                onSpin();
            }
        });
        root.add(spinBtn).width(220f).height(64f).padTop(10f).row();

        // Row display
        rowLabel = new Label("— | — | —", skin);
        rowLabel.setFontScale(1.4f);
        root.add(rowLabel).padTop(16f).row();

        // Result/payout display
        resultLabel = new Label("Place a bet and spin!", skin);
        root.add(resultLabel).padTop(10f).row();
    }

    private void addFooter() {
        Label.LabelStyle balStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        balStyle.fontColor = GOLD;
        currencyLabel = new Label("", balStyle);
        currencyLabel.setFontScale(1.2f);
        updateBalanceLabel();

        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                hide();
            }
        });

        Stack footerStack = new Stack();
        Table centerRow = new Table();
        centerRow.center();
        centerRow.add(closeBtn).expandX().center();

        Table rightRow = new Table();
        rightRow.add(currencyLabel).expandX().right().padRight(12f);

        footerStack.add(centerRow);
        footerStack.add(rightRow);

        root.add(footerStack).growX().padTop(12f).row();
    }

    private void onSpin() {
        int bet = parseBet();

        if (bet <= 0) {
            resultLabel.setText("Bet must be greater than 0.");
            return;
        }
        if (bet > balance) {
            resultLabel.setText("Insufficient funds.");
            return;
        }

        balance -= bet;
        updateBalanceLabel();

        String[] row = spinRow();
        rowLabel.setText(String.join(" | ", row));
        int payout = getPayout(row, bet);

        if (payout > 0) {
            balance += payout;
            resultLabel.setText("You won $" + payout + "!");
        } else {
            resultLabel.setText("Sorry, you lost this round.");
        }
        updateBalanceLabel();
    }

    private int parseBet() {
        String s = betInput.getText().trim();
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void adjustBet(int delta) {
        int current = parseBet();
        int next = Math.max(0, current + delta);
        betInput.setText(String.valueOf(next));
    }

    private void updateBalanceLabel() {
        currencyLabel.setText("Balance: $" + balance);
    }

    private String[] spinRow() {
        String[] row = new String[3];
        for (int i = 0; i < 3; i++) {
            row[i] = symbols[random.nextInt(symbols.length)];
        }
        return row;
    }

    private int getPayout(String[] row, int bet) {
        // Triple match
        if (row[0].equals(row[1]) && row[1].equals(row[2])) {
            return switch (row[0]) {
                case "🍒" -> bet * 3;
                case "🍉" -> bet * 4;
                case "🍋" -> bet * 5;
                case "🔔" -> bet * 10;
                case "⭐" -> bet * 20;
                default -> 0;
            };
        }
        // Two-in-a-row (left pair)
        else if (row[0].equals(row[1])) {
            return switch (row[0]) {
                case "🍒" -> bet * 2;
                case "🍉" -> bet * 3;
                case "🍋" -> bet * 4;
                case "🔔" -> bet * 5;
                case "⭐" -> bet * 10;
                default -> 0;
            };
        }
        // Two-in-a-row (right pair)
        else if (row[1].equals(row[2])) {
            return switch (row[1]) {
                case "🍒" -> bet * 2;
                case "🍉" -> bet * 3;
                case "🍋" -> bet * 4;
                case "🔔" -> bet * 5;
                case "⭐" -> bet * 10;
                default -> 0;
            };
        }
        return 0;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }


    public Stage getStage() {
        return stage;
    }

    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        if (frame != null) frame.setVisible(true);
        if (background != null) background.setVisible(true);
        if (root != null) root.setVisible(true);
        if (dimmer != null) dimmer.setVisible(true);

        betInput.setText("10"); // nice default
    }

    public void hide() {
        ServiceLocator.getTimeSource().setPaused(false);
        if (frame != null) frame.setVisible(false);
        if (background != null) background.setVisible(false);
        if (root != null) root.setVisible(false);
        if (dimmer != null) dimmer.setVisible(false);
    }

    @Override
    public void dispose() {
        if (root != null) { root.remove(); root = null; }
        if (dimmer != null) { dimmer.remove(); dimmer = null; }
        if (frame != null) { frame.remove(); frame = null; }
        if (background != null) { background.remove(); background = null; }
        if (pixelTex != null) { pixelTex.dispose(); pixelTex = null; }
        if (itemPopup != null) { itemPopup.dispose(); itemPopup = null; }
        super.dispose();
    }

    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
