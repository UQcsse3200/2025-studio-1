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
        // Base kiosk with hitbox/interact set up by your factory
        Entity e = InteractableStationFactory.createBaseStation();

        // 1) Add the display first and keep a reference
        SlotsDisplay display = new SlotsDisplay();
        e.addComponent(display);

        // 2) Add a texture that actually exists in your assets and is preloaded
        TextureRenderComponent tex = new TextureRenderComponent("images/slots_kiosk.png");
        e.addComponent(tex);
        tex.scaleEntity();            // optional: match your pixels-per-unit

        // 3) (Optional) hotkey: press '9' to toggle the UI
        e.addComponent(new SlotsHotkeyComponent(display));

        // Ensure it’s interactable via your existing prompt/key
        e.setInteractable(true);

        // If your engine doesn’t auto-call create() when spawning, do it here:
        // e.create();

        return e;
    }


    public Entity getGameEntity() {
        return gameEntity;
    }
}
