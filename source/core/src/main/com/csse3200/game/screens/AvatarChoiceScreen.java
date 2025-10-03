package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;

import java.util.List;

/**
 * This class defines the screen that the player will choose the avatar from.
 * Keys to be used:
 * esc: to leave the screen back to main menu
 * enter: to confirm the selection of an avtar
 * left/right arrow keys: to change selection
 */
public class AvatarChoiceScreen extends BaseScreen {
    private Stage stage;
    private Skin skin;

    private Label nameLabel;
    private Label statsLabel;

    private Table cardRow;
    private List<Avatar> avatars;
    private int selectedIndex = 0;
    static final String BgImage  = "images/menu_background.png";
    static final String SkinPath = "uiskin.json";


    static final float RootPadding            = 30f;
    static final float DefaultGap        = 20f;
    static final float SmallGap           = 10f;

    static final float TitleFontScale    = 3f;
    static final float NameFontScale     = 2f;
    static final int RootColumnSpan     = 2;

    static final float CardRowCellPad   = 10f;
    static final float CardInternalPad   = 5f;

    static final float CardImageSize     = 180f;
    static final float CardWidth          = 200f;
    static final float CardHeight         = 240f;

    static final float ScrollHeight       = 300f;
    static final float InfoPanelWidth    = 420f;
    static final float StatsLabelLWidth   = 380f;

    private Array<Texture> loadedTextures = new Array<>();


    public AvatarChoiceScreen(GdxGame game) {
        super(game, BgImage);
    }

    @Override
    protected Entity createUIScreen(Stage stage) {
        this.stage = stage;
        this.skin = new Skin(Gdx.files.internal(SkinPath));
        if (loadedTextures == null) {
            loadedTextures = new Array<>();
        }

        avatars = AvatarRegistry.getAll();

        Table root = new Table();
        root.setFillParent(true);
        root.pad(RootPadding);
        stage.addActor(root);

        // Title
        Label titleLabel = new Label("Choose Your Avatar", skin, "default");
        titleLabel.setAlignment(Align.center);
        titleLabel.setFontScale(TitleFontScale);

        // Cards row inside a scroll pane
        cardRow = new Table();
        cardRow.defaults().pad(SmallGap);
        buildCards();

        ScrollPane scrollPane = new ScrollPane(cardRow, skin);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFlickScroll(true);
        scrollPane.setSmoothScrolling(true);

        // Stats panel (right or below)
        nameLabel = new Label("", skin, "default");
        nameLabel.setFontScale(NameFontScale);
        statsLabel = new Label("", skin, "default");
        statsLabel.setWrap(true);

        Table infoCol = new Table();
        infoCol.add(nameLabel).left().row();
        infoCol.add(statsLabel).width(StatsLabelLWidth).left().top().padTop(CardRowCellPad).row();

        root.add(titleLabel).colspan(RootColumnSpan).center().padBottom(DefaultGap).row();
        root.add(scrollPane).growX().height(ScrollHeight).colspan(RootColumnSpan).padBottom(DefaultGap).row();
        root.add(new Label("", skin)).growX(); // spacer
        root.add(infoCol).width(InfoPanelWidth).top();

        stage.setKeyboardFocus(root);
        Gdx.input.setInputProcessor(stage);

        updateSelection(0);

        Entity ui = new Entity();
        ui.addComponent(new InputDecorator(stage, 10));
        return ui;
    }

    /**
     * loops through the list of avatars that are registered and adds them to the screen
     */
    private void buildCards() {
        cardRow.clearChildren();
        for (int i = 0; i < avatars.size(); i++) {
            final int index = i;
            Avatar a = avatars.get(i);

            Texture tex = new Texture(Gdx.files.internal(a.texturePath()));
            loadedTextures.add(tex);
            Drawable drawable = new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(tex));

            Image img = new Image(drawable);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            img.setSize(CardImageSize, CardImageSize);

            Label name = new Label(a.displayName(), skin);
            name.setAlignment(Align.center);

            Table card = new Table(skin);
            card.defaults().pad(CardInternalPad);
            card.setBackground("default-round");
            card.add(img).size(CardImageSize, CardImageSize).row();
            card.add(name).width(CardImageSize).center();

            // Click selects
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    updateSelection(index);
                }
            });
            cardRow.add(card).width(CardWidth).height(CardHeight);
        }
    }

    /**
     * switch focus of the avatar based on player's choice
     * @param newIndex the position of the avatar that the player has selected
     */
    private void updateSelection(int newIndex) {
        final int n = cardRow.getChildren().size;
        if (n == 0) return;

        // wrap new index
        int wrapped = ((newIndex % n) + n) % n;

        // clear the previously selected card by index (not by stale reference)
        if (selectedIndex >= 0 && selectedIndex < n) {
            Table prev = (Table) cardRow.getChildren().get(selectedIndex);
            prev.setColor(1f, 1f, 1f, 1f); // reset to white
        }

        // apply highlight to the new one
        selectedIndex = wrapped;
        Table now = (Table) cardRow.getChildren().get(selectedIndex);
        now.setColor(1.0f, 1.0f, 0.6f, 1.0f);

        // update the details
        Avatar a = avatars.get(selectedIndex);
        nameLabel.setText(a.displayName());
        statsLabel.setText(
                "Health: " + a.baseHealth() + "\n" +
                        "Damage: " + a.baseDamage() + "\n" +
                        "Move Speed: " + a.moveSpeed() + "\n\n"
        );
        statsLabel.setFontScale(NameFontScale);
    }

    @Override
    public void render(float delta) {
        super.render(delta);
        // Keyboard: left/right to change, ENTER to select, ESC to back/skip
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            updateSelection(selectedIndex - 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            updateSelection(selectedIndex + 1);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            Avatar chosen = avatars.get(selectedIndex);
            AvatarRegistry.set(chosen);
            game.setScreen(new StoryScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(GdxGame.ScreenType.MAIN_MENU);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (Texture t : loadedTextures) t.dispose();
    }
}
