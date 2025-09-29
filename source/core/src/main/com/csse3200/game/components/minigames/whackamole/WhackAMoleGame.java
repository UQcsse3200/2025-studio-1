package com.csse3200.game.components.minigames.whackamole;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.rendering.TextureRenderComponent;

public class WhackAMoleGame {
    private final Entity gameEntity;
    private final WhackAMoleDisplay display;
    private boolean shown = false;

    public WhackAMoleGame() {
        gameEntity = initGameEntity();
        display = gameEntity.getComponent(WhackAMoleDisplay.class);
        gameEntity.getEvents().addListener("interact", this::toggle);
    }

    private void toggle() {
        if (shown) {
            display.hide();
        } else {
            display.show();
        }
        shown = !shown;
    }

    private Entity initGameEntity() {
        Entity game = InteractableStationFactory.createBaseStation();
        game.addComponent(new WhackAMoleDisplay());
        game.addComponent(new TextureRenderComponent("images/mole.png"));
        game.setInteractable(true);
        return game;
    }

    public Entity getGameEntity() {
        return gameEntity;
    }
}