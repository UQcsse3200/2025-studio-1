package com.csse3200.game.effects;

import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;

public class AimbotEffect extends Effect {

    private final float duration;
    private float elapsed = 0f;
    private boolean active = false;

    public AimbotEffect(float duration) {
        this.duration = duration;
    }

    @Override
    public boolean apply(Entity gun) {

        if (gun == null) return false;

        MagazineComponent mag = gun.getComponent(MagazineComponent.class);
        if (mag == null) return false;

        this.active = true;
        this.elapsed = 0f;
        return true;
    }

    public void update(float dt) {
        if (!active) return;

        elapsed += dt;
        if (elapsed >= duration) {
            remove();
        }
    }

    private void remove() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }
}
