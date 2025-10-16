package com.csse3200.game.components.stations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.services.ServiceLocator;


public class StationComponent extends Component {
    public BenchConfig config;
    private boolean playerNear = false;
    private Entity player = null;
    private Label buyPrompt = ServiceLocator.getPrompt();


    /**
     * Initialise the station component
     *
     * @param config config for the type of bench
     */
    public StationComponent(BenchConfig config) {
        this.config = config;
    }

    /**
     * @return is the player near
     */
    public boolean isPlayerNear() {
        return this.playerNear;
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
     * @return the player interacting
     */
    public Entity getPlayer() {
        return this.player;
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
     * @return the buyPrompt
     */
    public Label getBuyPrompt() {
        return this.buyPrompt;
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
     * @return the config for the bench
     */
    public BenchConfig getConfig() {
        return this.config;
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
     * @return the bench price
     */
    public int getPrice() {
        return this.config.getPrice();
    }

    @Override
    public void create() {
        setPlayer(ServiceLocator.getPlayer());
        entity.getEvents().addListener("enteredInteractRadius", this::onPlayerInRange);
        entity.getEvents().addListener("exitedInteractRadius", this::onPlayerLeftRange);
        entity.getEvents().addListener("interact", this::upgrade);
    }

    protected void onPlayerInRange() {
        playerNear = true;
        buyPrompt.setVisible(true);
        buyPrompt.setText(config.promptText);
    }

    protected void onPlayerLeftRange() {
        playerNear = false;
        buyPrompt.setVisible(false);
    }

    /**
     * Triggers the upgrade from the station
     */
    public void upgrade() {
        this.config.upgrade(playerNear, player, buyPrompt);
    }
}