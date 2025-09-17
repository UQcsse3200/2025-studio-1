package com.csse3200.game.components.stations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
     * @param config config for the type of bench
     */
    public StationComponent(BenchConfig config) {
        this.config = config;
    }

    /**
     * Sets if the player is near
     * @param near is the player near
     */
    public void setPlayerNear(boolean near) {
        this.playerNear = near;
    }

    /**
     * Sets the config
     * @param config the config
     */
    public void setConfig(BenchConfig config) {
        this.config = config;
    }

    /**
     * Sets buy prompt
     * @param prompt the buyPrompt
     */
    public void setBuyPrompt(Label prompt) {
        this.buyPrompt = prompt;
    }

    /**
     * Sets the player interacting with the bench
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
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);

        //Font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/ithaca.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 20; // font size in pixels
        BitmapFont font = generator.generateFont(params);
        generator.dispose(); // free generator memory
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.font.getData().setScale(2f);

        buyPrompt = new Label("", labelStyle);
        buyPrompt.setVisible(false);
        buyPrompt.setText(config.promptText);
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);
    }

    /**
     * Updates when the player collides with the station
     * @param me the station
     * @param other the player colliding
     */
    protected void onCollisionStart(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            player = otherEntity;
            playerNear = true;
            otherEntity.getEvents().addListener("interact", this::upgrade);
            buyPrompt.setVisible(true);
            float screenX = ServiceLocator.getRenderService().getStage().getWidth() / 2f;
            float screenY = ServiceLocator.getRenderService().getStage().getHeight() / 2f;
            buyPrompt.setPosition(screenX - 200f, screenY - 100f, Align.bottom);
            buyPrompt.setText(config.promptText);
        }
    }

    /**
     * Updates when the player stops colliding with the station
     * @param me the station
     * @param other the player
     */
    protected void onCollisionEnd(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;
        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            otherEntity.getEvents().removeListener("interact", this::upgrade);

            playerNear = false;
            buyPrompt.setVisible(false);
        }
    }

    /**
     * Triggers the upgrade from the station
     */
    public void upgrade() {
        System.out.println(config.benchType.getString() + " " + playerNear);
        this.config.upgrade(playerNear, player, buyPrompt);
    }
}