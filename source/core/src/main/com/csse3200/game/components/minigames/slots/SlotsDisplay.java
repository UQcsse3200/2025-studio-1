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
import com.badlogic.gdx.utils.Scaling;


import java.util.Random;

public class SlotsDisplay extends UIComponent {

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
    private Label.LabelStyle whiteLabelStyle, winLabelStyle, loseLabelStyle;
    private final java.util.Random rng = new java.util.Random();
    private com.badlogic.gdx.scenes.scene2d.ui.Image[] reelImgs = new com.badlogic.gdx.scenes.scene2d.ui.Image[3];

    private int balance = 100;


    private ItemScreenDisplay itemPopup;

    private enum SlotSymbol {
        CHERRY("images/cherry.png",    3,  2),
        WATERMELON("images/watermelon.png", 4, 3),
        LEMON("images/lemon.png",      5,  4),
        BELL("images/bell.png",       10,  5),
        DIAMOND("images/diamond.png", 20, 10);

        final String texturePath;
        final int tripleMult, pairMult;
        SlotSymbol(String path, int triple, int pair) {
            this.texturePath = path; this.tripleMult = triple; this.pairMult = pair;
        }
    }

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
        Label.LabelStyle whiteStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        whiteStyle.fontColor = Color.WHITE;

        Label.LabelStyle winStyle = new Label.LabelStyle(whiteStyle);
        winStyle.fontColor = Color.GREEN;

        Label.LabelStyle loseStyle = new Label.LabelStyle(whiteStyle);
        loseStyle.fontColor = Color.RED;

        Table reels = new Table();
        for (int i = 0; i < 3; i++) {
            Texture tex = ServiceLocator.getResourceService().getAsset("images/cherry.png", Texture.class);
            reelImgs[i] = new Image(new TextureRegionDrawable(new TextureRegion(tex)));
            reelImgs[i].setScaling(Scaling.fit);
            reels.add(reelImgs[i]).size(128, 128).pad(8);
        }
        root.add(reels).padTop(12f).row();

        Table betRow = new Table();
        Label betLabel = new Label("Bet:", whiteStyle);
        betInput = new TextField("", skin);
        betInput.setMessageText("Enter bet amount");
        betInput.setAlignment(Align.center);

        TextButton minusBtn = new TextButton("-10", skin);
        TextButton plusBtn = new TextButton("+10", skin);
        minusBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){ adjustBet(-10);} });
        plusBtn.addListener(new ChangeListener()  { @Override public void changed(ChangeEvent e, Actor a){ adjustBet(10);}  });

        betRow.add(betLabel).padRight(8f);
        betRow.add(minusBtn).width(80).height(48).padRight(8f);
        betRow.add(betInput).width(160).padRight(8f);
        betRow.add(plusBtn).width(80).height(48);
        root.add(betRow).padTop(10f).row();

        TextButton spinBtn = new TextButton("Spin", skin);
        spinBtn.addListener(new ChangeListener() { @Override public void changed(ChangeEvent e, Actor a){ onSpin(); }});
        root.add(spinBtn).width(220f).height(64f).padTop(10f).row();

        resultLabel = new Label("Place a bet and spin!", whiteStyle);
        this.winLabelStyle  = winStyle;
        this.loseLabelStyle = loseStyle;
        this.whiteLabelStyle = whiteStyle;

        root.add(resultLabel).padTop(10f).row();
    }


    private SlotSymbol[] spinRow() {
        SlotSymbol[] vals = SlotSymbol.values(), row = new SlotSymbol[3];
        for (int i = 0; i < 3; i++) row[i] = vals[rng.nextInt(vals.length)];
        return row;
    }

    private void showRow(SlotSymbol[] row) {
        for (int i = 0; i < 3; i++) {
            Texture tex = ServiceLocator.getResourceService().getAsset(row[i].texturePath, Texture.class);
            reelImgs[i].setDrawable(new TextureRegionDrawable(new TextureRegion(tex)));
        }
    }

    private int getPayout(SlotSymbol[] row, int bet) {
        if (row[0] == row[1] && row[1] == row[2]) return bet * row[0].tripleMult;
        if (row[0] == row[1]) return bet * row[0].pairMult;
        if (row[1] == row[2]) return bet * row[1].pairMult;
        return 0;
    }

    private void onSpin() {
        int bet = parseBet();
        if (bet <= 0) { resultLabel.setStyle(loseLabelStyle); resultLabel.setText("Bet must be greater than 0."); return; }
        if (bet > balance) { resultLabel.setStyle(loseLabelStyle); resultLabel.setText("Insufficient funds."); return; }

        balance -= bet; updateBalanceLabel();

        SlotSymbol[] row = spinRow();
        showRow(row);

        int payout = getPayout(row, bet);
        if (payout > 0) {
            balance += payout;
            resultLabel.setStyle(winLabelStyle);
            resultLabel.setText("You won $" + payout + "!");
        } else {
            resultLabel.setStyle(loseLabelStyle);
            resultLabel.setText("Sorry, you lost this round.");
        }
        updateBalanceLabel();
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

        betInput.setText("10");
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
