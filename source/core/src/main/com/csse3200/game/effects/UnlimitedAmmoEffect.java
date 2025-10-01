package com.csse3200.game.effects;

import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;

public class UnlimitedAmmoEffect extends Effect {
    private final float duration;
    private float elapsed = 0f;
    private boolean active = false;
    private MagazineComponent magazine;
    private final PlayerActions playerActions;


    public UnlimitedAmmoEffect(float duration, PlayerActions playerActions) {
        this.duration = duration;
        this.playerActions = playerActions;
    }

    @Override
    public boolean apply(Entity gun) {
        if (gun == null) return false;

        MagazineComponent mag = gun.getComponent(MagazineComponent.class);
        if (mag == null) return false;
        

        this.magazine = mag;
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
        if (magazine != null) {
            magazine.setCurrentAmmo(magazine.getCurrentAmmo() - 1);
        }
        active = false;
        magazine = null;
    }

    public boolean isActive() {
        return active;
    }
}
