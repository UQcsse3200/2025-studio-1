package com.csse3200.game.components.cards;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class BettingComponent extends UIComponent {

    private static final float PANEL_W = 600f;
    private static final float PANEL_H = 250f;

    // 🎨 Match BlackjackScreenDisplay colors
    private static final Color PANEL_COLOR = Color.OLIVE;
    private static final Color TEXT_COLOR = Color.WHITE;

    private final int multiplier;
    private int balance = 100;

    private Table root;
    private Image background;
    private Texture pixelTex;
    private Label balanceLabel, resultLabel;
    private TextField betInput;

    public BettingComponent(int multiplier, GameArea area) {
        this.multiplier = multiplier;
    }

    @Override
    public void create() {
        super.create();
        pixelTex = makeSolidTexture(Color.WHITE);

        // Background
        background = new Image(pixelTex);
        background.setSize(PANEL_W, PANEL_H);
        background.setColor(PANEL_COLOR);
        background.setPosition((stage.getWidth() - PANEL_W) / 2f, (stage.getHeight() - PANEL_H) / 2f);
        stage.addActor(background);

        // Root table
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        // Title
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TEXT_COLOR;
        Label title = new Label("Place Your Bet", titleStyle);
        title.setFontScale(1.4f);
        root.add(title).padBottom(10f).row();

        // Balance
        balanceLabel = new Label("Balance: $" + balance, skin);
        balanceLabel.setColor(TEXT_COLOR);
        root.add(balanceLabel).padBottom(10f).row();

        // Bet row
        Table betRow = new Table();
        Label betLabel = new Label("Bet:", skin);
        betLabel.setColor(TEXT_COLOR);

        betInput = new TextField("", skin);
        betInput.setMessageText("Enter bet");
        betInput.setAlignment(Align.center);

        TextButton minusBtn = new TextButton("-10", skin);
        TextButton plusBtn = new TextButton("+10", skin);
        minusBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a){ adjustBet(-10); }
        });
        plusBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent e, Actor a){ adjustBet(10); }
        });

        betRow.add(betLabel).padRight(6f);
        betRow.add(minusBtn).width(60).padRight(6f);
        betRow.add(betInput).width(100).padRight(6f);
        betRow.add(plusBtn).width(60);
        root.add(betRow).padBottom(10f).row();

        // Confirm bet button
        TextButton betBtn = new TextButton("Confirm Bet", skin);
        betBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                int bet = parseBet();
                if (bet <= 0) {
                    resultLabel.setColor(Color.RED);
                    resultLabel.setText("Invalid bet!");
                    return;
                }
                if (bet > balance) {
                    resultLabel.setColor(Color.RED);
                    resultLabel.setText("Not enough funds!");
                    return;
                }
                balance -= bet;
                updateBalance();
                resultLabel.setColor(TEXT_COLOR);
                resultLabel.setText("Bet placed: $" + bet);
                hide();

                // Event to start blackjack round
                entity.getEvents().trigger("betPlaced");
            }
        });
        root.add(betBtn).width(160).height(50).padBottom(10f).row();

        // Result label
        resultLabel = new Label("Waiting for result...", skin);
        resultLabel.setColor(TEXT_COLOR);
        root.add(resultLabel).padTop(10f).row();

        // Win/Lose listeners
        entity.getEvents().addListener("interact", this::show);
        entity.getEvents().addListener("betWin", this::onWin);
        entity.getEvents().addListener("betLose", this::onLose);
        hide();
    }

    private void onWin(int amount) {
        int winnings = amount * multiplier;
        balance += winnings;
        updateBalance();
        resultLabel.setColor(Color.GREEN);
        resultLabel.setText("You won $" + winnings + "!");
    }

    private void onLose(int amount) {
        resultLabel.setColor(Color.RED);
        resultLabel.setText("You lost $" + amount);
    }

    private void updateBalance() {
        balanceLabel.setText("Balance: $" + balance);
    }

    private int parseBet() {
        try {
            return Integer.parseInt(betInput.getText().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void adjustBet(int delta) {
        int current = parseBet();
        int next = Math.max(0, current + delta);
        betInput.setText(String.valueOf(next));
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles rendering
    }

    @Override
    public void dispose() {
        if (root != null) root.remove();
        if (background != null) background.remove();
        if (pixelTex != null) pixelTex.dispose();
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

    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        if (root != null) root.setVisible(true);
        if (background != null) background.setVisible(true);
    }

    public void hide() {
        if (root != null) root.setVisible(false);
        if (background != null) background.setVisible(false);
    }
}
