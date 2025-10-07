package com.csse3200.game.effects;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.player.InventoryComponent;

public class DoubleProcessorsEffect extends Effect {
    private final float duration;
    private float elapsed = 0f;
    private boolean active = false;

    public DoubleProcessorsEffect(float duration) {
        this.duration = duration;
    }

    @Override
    public boolean apply(Entity entity) {
        if (entity == null) return false;

        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (inventory != null) {
            inventory.setDoubleProcessors(true);
        }

        this.active = true;
        this.elapsed = 0f;
        return true;
    }

    public void update(float dt) {
        if (!active) return;

        elapsed += dt;
        if (elapsed >= duration) {
            active = false;
        }
    }

    public boolean isActive() {
        return active;
    }
}
