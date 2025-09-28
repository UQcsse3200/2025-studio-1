package com.csse3200.game.components.minigames.slots;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.rendering.TextureRenderComponent;

public class SlotsGame {
    private final Entity gameEntity;
    private final SlotsDisplay display;
    private boolean shown = false;

    public SlotsGame() {
        gameEntity = initGameEntity();
        display = gameEntity.getComponent(SlotsDisplay.class);

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
        Entity e = InteractableStationFactory.createBaseStation();

        SlotsDisplay display = new SlotsDisplay();
        e.addComponent(display);

        TextureRenderComponent tex = new TextureRenderComponent("images/slots_kiosk.png");
        e.addComponent(tex);
        tex.scaleEntity();


        e.setInteractable(true);


        return e;
    }


    public Entity getGameEntity() {
        return gameEntity;
    }
}
