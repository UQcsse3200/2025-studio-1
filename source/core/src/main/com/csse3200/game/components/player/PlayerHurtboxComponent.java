package com.csse3200.game.components.player;

import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;

public class PlayerHurtboxComponent extends ColliderComponent {
    public PlayerHurtboxComponent() {
        super();
        this.setLayer(PhysicsLayer.PLAYER);
    }
}
