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

/**
 * A LibGDX UI component that allows the player to place, confirm, and adjust bets
 * in a mini-game. Handles user interactions, displays balance and bet information,
 * and reacts to game events such as win, lose, or tie.
 *
 * <p>The component pauses the game when shown and resumes it when closed.</p>
 */
public class BettingComponent extends UIComponent {

    private static final float PANEL_W = 600f;
    private static final float PANEL_H = 400f;
    private static final Color PANEL_COLOR = Color.DARK_GRAY;
    private static final Color TEXT_COLOR = Color.WHITE;

    private final BettingLogic logic;
    private Table root;
    private Image background;
    private Texture pixelTex;
    private Label balanceLabel, resultLabel;
    private TextField betInput;

    /**
     * Creates a new {@code BettingComponent}.
     *
     * @param multiplier the payout multiplier (e.g., 2 for double winnings)
     * @param inventory  the player's {@link InventoryComponent}, used for managing balance
     */
    public BettingComponent(int multiplier, InventoryComponent inventory) {
        this.logic = new BettingLogic(multiplier, inventory);
    }

    /**
     * Creates a 1x1 solid color texture used for panel backgrounds.
     *
     * @param color the color of the texture
     * @return a new {@link Texture} object
     */
    private static Texture makeSolidTexture(Color color) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // === UI Initialization ===

    /**
     * Initializes the betting UI, sets up the layout, event listeners, and game event bindings.
     */
    @Override
    public void create() {
        super.create();
        pixelTex = makeSolidTexture(Color.WHITE);

        setupBackground();
        setupRootTable();
        addTitle();
        addBalanceDisplay();
        addBetRow();
        addConfirmButton();
        addCloseButton();
        addResultLabel();
        setupEventListeners();

        hide();
    }

    private void setupBackground() {
        background = new Image(pixelTex);
        background.setSize(PANEL_W, PANEL_H);
        background.setColor(PANEL_COLOR);
        background.setPosition((stage.getWidth() - PANEL_W) / 2f, (stage.getHeight() - PANEL_H) / 2f);
        stage.addActor(background);
    }

    private void setupRootTable() {
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);
    }

    private void addTitle() {
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        titleStyle.fontColor = TEXT_COLOR;
        Label title = new Label("Place Your Bet", titleStyle);
        title.setFontScale(1.4f);
        root.add(title).padBottom(10f).row();
    }

    private void addBalanceDisplay() {
        balanceLabel = new Label("Balance: $" + logic.getBalance(), skin);
        balanceLabel.setColor(TEXT_COLOR);
        root.add(balanceLabel).padBottom(10f).row();
    }

    private void addBetRow() {
        Table betRow = new Table();

        Label betLabel = new Label("Bet:", skin);
        betLabel.setColor(TEXT_COLOR);

        betInput = new TextField("", skin);
        betInput.setMessageText("Enter bet");
        betInput.setAlignment(Align.center);

        TextButton minusBtn = new TextButton("-10", skin);
        TextButton plusBtn = new TextButton("+10", skin);

        minusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                adjustBet(-10);
            }
        });

        plusBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent e, Actor a) {
                adjustBet(10);
            }
        });

        betRow.add(betLabel).padRight(6f);
        betRow.add(minusBtn).width(60).padRight(6f);
        betRow.add(betInput).width(100).padRight(6f);
        betRow.add(plusBtn).width(60);

        root.add(betRow).padBottom(10f).row();
    }

    private void addConfirmButton() {
        TextButton betBtn = new TextButton("Confirm Bet", skin);
        betBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                handleConfirmBet();
            }
        });
        root.add(betBtn).width(160).height(50).padBottom(10f).row();
    }

    private void handleConfirmBet() {
        try {
            int betAmount = Integer.parseInt(betInput.getText().trim());
            logic.placeBet(betAmount);
            updateBalance();
            resultLabel.setColor(TEXT_COLOR);
            resultLabel.setText("Bet placed: $" + logic.getBet());
            hide();
            entity.getEvents().trigger("betPlaced");
        } catch (IllegalArgumentException e) {
            resultLabel.setColor(Color.RED);
            resultLabel.setText(e.getMessage());
        }
    }

    private void addCloseButton() {
        TextButton closeBtn = new TextButton("Close", skin);
        closeBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                ServiceLocator.getTimeSource().setPaused(false);
                hide();
                resultLabel.setText("Bet cancelled.");
            }
        });
        root.add(closeBtn).width(160).height(50).padBottom(10f).row();
    }

    private void addResultLabel() {
        resultLabel = new Label("Waiting for result...", skin);
        resultLabel.setColor(TEXT_COLOR);
        root.add(resultLabel).padTop(10f).row();
    }

    private void setupEventListeners() {
        entity.getEvents().addListener("interact", this::show);
        entity.getEvents().addListener("win", this::onWin);
        entity.getEvents().addListener("tie", this::onTie);
        entity.getEvents().addListener("lose", this::onLose);
        entity.getEvents().addListener("split", this::split);
        entity.getEvents().addListener("double", this::doubleDown);
        entity.getEvents().addListener("doubleLose", this::doubleLose);
        entity.getEvents().addListener("doubleWin", this::doubleWin);
        entity.getEvents().addListener("doubleTie", this::doubleTie);
    }

    // === Game-specific actions ===

    void split() {
        if (logic.canDouble()) {
            logic.split();
            entity.getEvents().trigger("splitSuccess");
        } else {
            Dialog dialog = new Dialog("Cannot split", skin);
            dialog.text("Not enough funds");
            dialog.button("OK");
            ServiceLocator.getRenderService().getStage().addActor(dialog);
            dialog.show(ServiceLocator.getRenderService().getStage());
        }
    }

    void doubleDown() {
        if (logic.canDouble()) {
            logic.doubleBet();
            entity.getEvents().trigger("doubleSuccess");
        } else {
            Dialog dialog = new Dialog("Cannot double", skin);
            dialog.text("Not enough funds");
            dialog.button("OK");
            ServiceLocator.getRenderService().getStage().addActor(dialog);
            dialog.show(ServiceLocator.getRenderService().getStage());
        }
    }

    void doubleLose() {
        updateBalance();
        Dialog dialog = new Dialog("Double Loser!", skin);
        dialog.text("You lost $" + (logic.getBet() * 2));
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void doubleWin() {
        logic.doubleWin();
        Dialog dialog = new Dialog("Double Winner!", skin);
        dialog.text("Congratulations you won $" + logic.calculateWinnings());
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void doubleTie() {
        logic.doubleTie();
        updateBalance();
        Dialog dialog = new Dialog("Tie!", skin);
        dialog.text("Tie! You get your money back!");
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void onWin() {
        logic.onWin();
        updateBalance();
        Dialog dialog = new Dialog("Winner!", skin);
        dialog.text("Congratulations you won $" + (logic.calculateWinnings() - logic.getBet()));
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void onTie() {
        logic.onTie();
        updateBalance();
        Dialog dialog = new Dialog("Tie!", skin);
        dialog.text("Tie! You get your money back!");
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    void onLose() {
        logic.onLose();
        updateBalance();
        Dialog dialog = new Dialog("Loser!", skin);
        dialog.text("You lost $" + logic.getBet());
        dialog.button("OK");
        ServiceLocator.getRenderService().getStage().addActor(dialog);
        dialog.show(ServiceLocator.getRenderService().getStage());
    }

    // === UI helper methods ===

    private void adjustBet(int delta) {
        logic.adjustBet(delta);
        betInput.setText(String.valueOf(logic.getBet()));
    }

    private void updateBalance() {
        balanceLabel.setText("Balance: $" + logic.getBalance());
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

    /**
     * Shows the betting panel and pauses the game.
     */
    public void show() {
        ServiceLocator.getTimeSource().setPaused(true);
        if (root != null) root.setVisible(true);
        if (background != null) background.setVisible(true);
    }

    /**
     * Hides the betting panel and background.
     */
    public void hide() {
        if (root != null) root.setVisible(false);
        if (background != null) background.setVisible(false);
    }
}
