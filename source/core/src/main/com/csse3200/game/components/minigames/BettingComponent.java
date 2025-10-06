package com.csse3200.game.components.minigames;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

public class BettingComponent extends UIComponent {

    private static final float PANEL_W = 600f;
    private static final float PANEL_H = 400f;

    private static final Color PANEL_COLOR = Color.OLIVE;
    private static final Color TEXT_COLOR = Color.WHITE;

    private int multiplier;
    private int balance;

    private Table root;
    private Image background;
    private Texture pixelTex;
    private Label balanceLabel, resultLabel;
    private TextField betInput;
    InventoryComponent inventory;
    private int bet;

    public BettingComponent(int multiplier, InventoryComponent inventory) {
        this.multiplier = multiplier;
        this.inventory = inventory;
    }

    @Override
    public void create() {
        balance = inventory.getProcessor();
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
                bet = parseBet();
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
                inventory.addProcessor(-1 * bet);
                updateBalance();
                resultLabel.setColor(TEXT_COLOR);
                resultLabel.setText("Bet placed: $" + bet);
                hide();

                // Event to start blackjack round
                entity.getEvents().trigger("betPlaced");
            }
        });
        root.add(betBtn).width(160).height(50).padBottom(10f).row();
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                hide();
                resultLabel.setText("Bet cancelled.");
                ServiceLocator.getTimeSource().setPaused(false); // resume game if paused
            }
        });
        root.add(closeBtn).width(160).height(50).padBottom(10f).row();

        // Result label
        resultLabel = new Label("Waiting for result...", skin);
        resultLabel.setColor(TEXT_COLOR);
        root.add(resultLabel).padTop(10f).row();

        // Win/Lose listeners
        entity.getEvents().addListener("interact", this::show);
        entity.getEvents().addListener("win", this::onWin);
        entity.getEvents().addListener("tie", this::onTie);
        entity.getEvents().addListener("lose", this::onLose);
        hide();
    }

    void onTie() {
        inventory.addProcessor(bet);
        Dialog dialog = new Dialog("Tie!", skin);
        dialog.text("Tie! You get your money back!");
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void onWin() {
        int winnings = bet * multiplier;
        inventory.addProcessor(winnings);
        updateBalance();

        Dialog dialog = new Dialog("Winner!", skin);
        dialog.text("Congratulations you won $" + (winnings - bet));
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
        //dialog.show(stage);
    }

    void onLose() {
        System.out.println("LOST!");
        Dialog dialog = new Dialog("Loser!", skin);
        dialog.text("You lost $" + bet);
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    private void updateBalance() {
        balance = inventory.getProcessor();
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
        balance = inventory.getProcessor();
        if (root != null) root.setVisible(true);
        if (background != null) background.setVisible(true);
    }

    public void hide() {
        if (root != null) root.setVisible(false);
        if (background != null) background.setVisible(false);
    }
}
