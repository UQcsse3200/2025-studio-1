package com.csse3200.game.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;


public class StationComponent extends Component {
    private boolean playerNear = false;
    private Entity player = null;
    private Label buyPrompt;

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
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);

    }


    private void onCollisionStart(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            player = otherEntity;

            playerNear = true;
            otherEntity.getEvents().addListener("interact", this::upgrade);
            buyPrompt.setVisible(true);
            float screenX = ServiceLocator.getRenderService().getStage().getWidth() / 2f;
            float screenY = ServiceLocator.getRenderService().getStage().getHeight() / 2f + 100; // 100 px above bottom\
            buyPrompt.setPosition(screenX - 100f, screenY, Align.bottom);
            buyPrompt.setText("Press E for upgrade");
        }
    }

    private void onCollisionEnd(Fixture me, Fixture other) {

        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;
        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            playerNear = false;
            buyPrompt.setVisible(false);
        }

    }

    public void upgrade() {
        if (playerNear && player != null) {

            Entity currItem = player.getComponent(InventoryComponent.class).getCurrItem();

            WeaponsStatsComponent currItemStats = currItem.getComponent(WeaponsStatsComponent.class);
            if (currItemStats != null) {
                currItemStats.upgrade();
                buyPrompt.setText("Item has been upgraded");
            } else {
                buyPrompt.setText("Item is already fully upgraded!");
            }
        }


    }
}
