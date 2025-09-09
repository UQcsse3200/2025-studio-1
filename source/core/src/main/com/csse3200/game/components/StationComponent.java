package com.csse3200.game.components;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.entity.item.ItemComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.services.ServiceLocator;


public class StationComponent extends Component {
    private boolean playerNear = false;
    private Label buyPrompt;

    @Override
    public void create() {
        entity.getEvents().addListener("collisionStart", this::onCollisionStart);
        entity.getEvents().addListener("collisionEnd", this::onCollisionEnd);


        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont(); // or your game’s font

        buyPrompt = new Label("", labelStyle);
        buyPrompt.setVisible(false);
        buyPrompt.setPosition(100, 50);
        ServiceLocator.getRenderService().getStage().addActor(buyPrompt);

    }


    private void onCollisionStart(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;

        Entity otherEntity = userData.entity;
        if (otherEntity.getComponent(PlayerActions.class) != null) {
            playerNear = true;
            buyPrompt.setVisible(true);
            buyPrompt.setText("Press E for upgrade");
        }
    }

    private void onCollisionEnd(Fixture me, Fixture other) {
        Object data = other.getBody().getUserData();
        if (!(data instanceof BodyUserData userData)) return;
        buyPrompt.setVisible(false);

    }

//    @Override
//    public void update() {
//        if (playerNear
//    }
}
