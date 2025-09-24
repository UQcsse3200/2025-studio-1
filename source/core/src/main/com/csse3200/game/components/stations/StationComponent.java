package com.csse3200.game.components.stations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;


public class StationComponent extends Component {
    private boolean playerNear = false;
    private Entity player = null;
    private Label buyPrompt;
    public BenchConfig config;

    /**
     * Initialise the station component
     *
     * @param config config for the type of bench
     */
    public StationComponent(BenchConfig config) {
        this.config = config;
    }

    /**
     * Sets if the player is near
     *
     * @param near is the player near
     */
    public void setPlayerNear(boolean near) {
        this.playerNear = near;
    }

    /**
     * Sets the config
     *
     * @param config the config
     */
    public void setConfig(BenchConfig config) {
        this.config = config;
    }

    /**
     * Sets buy prompt
     *
     * @param prompt the buyPrompt
     */
    public void setBuyPrompt(Label prompt) {
        this.buyPrompt = prompt;
    }

    /**
     * Sets the player interacting with the bench
     *
     * @param player interacting with the bench
     */
    public void setPlayer(Entity player) {
        this.player = player;
    }

    /**
     *
     * @return is the player near
     */
    public boolean isPlayerNear() {
        return this.playerNear;
    }

    /**
     *
     * @return the player interacting
     */
    public Entity getPlayer() {
        return this.player;
    }

    /**
     *
     * @return the buyPrompt
     */
    public Label getBuyPrompt() {
        return this.buyPrompt;
    }

    /**
     *
     * @return the config for the bench
     */
    public BenchConfig getConfig() {
        return this.config;
    }

    /**
     *
     * @return the bench price
     */
    public int getPrice() {
        return this.config.getPrice();
    }

    @Override
    public void create() {
        setPlayer(ServiceLocator.getPlayer());
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);
        ServiceLocator.getPlayer().getEvents().addListener("interact", this::upgrade);

        //This part is AI generated
        // Font setup with mipmapping for smoother scaling
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/ithaca.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params = new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 28; // base font size
        params.minFilter = Texture.TextureFilter.Linear;  // smooth scaling
        params.magFilter = Texture.TextureFilter.Linear;
        params.borderColor = Color.BLACK;
        params.borderWidth = 2; // gives outline for readability
        params.shadowColor = new Color(0, 0, 0, 0.6f);
        params.shadowOffsetX = 2;
        params.shadowOffsetY = 2;

        BitmapFont font = generator.generateFont(params);
        generator.dispose();

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;

        // Add a background (9-patch image or simple tinted drawable)
        Texture bgTexture = new Texture(Gdx.files.internal("images/textbg.png"));
        NinePatch ninePatch = new NinePatch(bgTexture, 10, 10, 10, 10);
        labelStyle.background = new NinePatchDrawable(ninePatch);

        buyPrompt = new Label(config.promptText, labelStyle);
        buyPrompt.setVisible(false);
        buyPrompt.setAlignment(Align.center); // center text inside background
        buyPrompt.setSize(500f, 100f);   // set box size
        buyPrompt.setWrap(true);        // wrap text if needed
        buyPrompt.setOrigin(Align.center);
        buyPrompt.setPosition(
                ServiceLocator.getRenderService().getStage().getWidth() / 2f,
                ServiceLocator.getRenderService().getStage().getHeight() / 2f, // bottom quarter of screen
                Align.center
        );
        buyPrompt.setTouchable(Touchable.disabled); // don't block clicks

        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);
    }


    /**
     * Updates when the player collides with the station
     *
     * @param me    the station
     * @param other the player colliding
     */
    protected void onCollisionStart(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            player = otherEntity;
            playerNear = true;
            buyPrompt.setVisible(true);
            buyPrompt.setText(config.promptText);
        }
    }

    /**
     * Updates when the player stops colliding with the station
     *
     * @param me    the station
     * @param other the player
     */
    protected void onCollisionEnd(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;
        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            playerNear = false;
            buyPrompt.setVisible(false);
        }
    }

    /**
     * Triggers the upgrade from the station
     */
    public void upgrade() {
        this.config.upgrade(playerNear, player, buyPrompt);
    }
}