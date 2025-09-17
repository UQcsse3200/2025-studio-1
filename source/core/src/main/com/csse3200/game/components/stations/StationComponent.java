package com.csse3200.game.components.stations;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;


public class StationComponent extends Component {
    private boolean playerNear = false;
    private Entity player = null;
    private Label buyPrompt;
    private int price = 0;

    /**
     *
     * @return if the player is in the hitbox of the station
     */
    public boolean isPlayerNear() {
        return this.playerNear;
    }

    /**
     * Sets if the player is colliding
     * @param near is the player colliding
     */
    public void setPlayerNear(boolean near) {
        this.playerNear = near;
    }

    /**
     *
     * @return the player if they are colliding, otherwise null
     */
    public Entity getPlayer() {
        return this.player;
    }

    /**
     * Set the colliding player
     * @param player colliding
     */
    public void setPlayer(Entity player) {
        this.player = player;
    }

    /**
     * Sets the buy prompt
     * @param prompt to be set
     */
    public void setBuyPrompt(Label prompt) {
        this.buyPrompt = prompt;
    }

    /**
     *
     * @return the buy prompt for the station
     */
    public Label getBuyPrompt() {
        return this.buyPrompt;
    }

    @Override
    public void create() {
        //Create listeners for collisions
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);

        //Make the font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/ithaca.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter params =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
        params.size = 20;
        BitmapFont font = generator.generateFont(params);
        generator.dispose();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.font.getData().setScale(2f);

        //Make a buy label
        buyPrompt = new Label("", labelStyle);
        buyPrompt.setVisible(false);
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);

    }

    /**
     * Gets the price of the station
     * @return the price
     */
    public int getPrice() {
        return this.price;
    }

    /**
     * Sets the price of the station
     * @param price the price
     */
    public void setPrice(int price) {
        this.price = Math.max(price, 0);
    }

    /**
     * Handles colliding with the station and setting the buy labels/listeners
     * @param me the station
     * @param other entity colliding
     */
    protected void onCollisionStart(Fixture me, Fixture other) {
        //Check it is a body that is colliding
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        //Check if it's the player colliding

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            //
            player = otherEntity;
            playerNear = true;
            otherEntity.getEvents().addListener("interact", this::upgrade);
            //Add the text/position for the buy label
            buyPrompt.setVisible(true);
            buyPrompt.setText("Press E to upgrade for " + this.price);
            buyPrompt.setColor(Color.WHITE);
            Pixmap labelColor = new Pixmap(100, 100, Pixmap.Format.RGB888);
            buyPrompt.getStyle().background = new Image(new Texture(labelColor)).getDrawable();
            Container<Label> wrapped = new Container<>(buyPrompt);
            wrapped.setBackground(new Image(new Texture(labelColor)).getDrawable());
            ServiceLocator.getRenderService().getStage().addActor(wrapped);
            float screenX = ServiceLocator.getRenderService().getStage().getWidth() / 2f;
            float screenY = ServiceLocator.getRenderService().getStage().getHeight() / 4f;
            wrapped.setPosition(screenX, screenY, Align.bottom);
            buyPrompt.layout();
        }
    }

    /**
     * Handles removing the labels when the player leaves
     * @param me the station
     * @param other the entity colliding
     */
    protected void onCollisionEnd(Fixture me, Fixture other) {

        //Remove the label if the player has moved away
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;
        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            playerNear = false;
            buyPrompt.setVisible(false);
        }
    }

    /**
     * Upgrades the player's current item
     */
    public void upgrade() {
        //Check if the player is near
        if (playerNear && player != null) {
            //Check if the player has funds
            if (player.getComponent(InventoryComponent.class).hasProcessor(this.price)) {
                //Get their current item WeaponStatsComponent
                Entity currItem = player.getComponent(InventoryComponent.class).getCurrItem();
                WeaponsStatsComponent currItemStats = currItem.getComponent(WeaponsStatsComponent.class);
                //Check if the weapon can be upgraded
                if (currItemStats != null) {
                    if (!currItemStats.isMaxUpgraded()) {
                        currItemStats.upgrade();
                        buyPrompt.setText("Item has been upgraded");
                        player.getComponent(InventoryComponent.class).addProcessor(-this.price);
                    } else {
                        buyPrompt.setText("Item is already fully upgraded!");
                    }
                } else {
                    buyPrompt.setText("This can't be upgraded");

                }
            } else {
                buyPrompt.setText("You are broke! Fries in the bag!");
            }
        }
    }
}
