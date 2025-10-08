package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * Simple helper that checks whether the player is within a given range.
 * Used to gate dialogue interaction.
 */
public class NpcProximityGateComponent extends Component {
    private final Entity player;
    private final float radius;

    public NpcProximityGateComponent(Entity player, float radius) {
        this.player = player;
        this.radius = radius;
    }

    /**
     * Returns true if the player is within the radius of this NPC.
     */
    public boolean isPlayerInRange() {
        if (player == null) return false;
        float distance = player.getCenterPosition().dst(entity.getCenterPosition());
        return distance <= radius;
    }
}
