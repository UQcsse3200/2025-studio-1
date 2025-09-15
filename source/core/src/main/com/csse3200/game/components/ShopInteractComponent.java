package com.csse3200.game.components;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Press 0 near a shop (entity with ShopComponent) to trigger "interact".
 */
public class ShopInteractComponent extends InputComponent {
    private final float rangeMeters;

    public ShopInteractComponent(float rangeMeters) {
        super(5);
        this.rangeMeters = rangeMeters;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode != Input.Keys.NUM_0 && keycode != Input.Keys.NUMPAD_0) return false;

        Entity nearest = null;
        float bestDist2 = rangeMeters * rangeMeters;

        Vector2 p = entity.getCenterPosition();
        for (Entity e : ServiceLocator.getEntityService().getEntities()) {
            ShopManager shop = e.getComponent(ShopManager.class);
            if (shop == null) continue;

            float d2 = e.getCenterPosition().dst2(p);
            if (d2 <= bestDist2) {
                bestDist2 = d2;
                nearest = e;
            }
        }

        if (nearest != null) {
            nearest.getEvents().trigger("interact");
            return true;
        }
        return false;
    }
}
