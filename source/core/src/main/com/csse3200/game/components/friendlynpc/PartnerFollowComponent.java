
package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

/**
 * Keep companions continuously following the
 * player: automatically teleport when distance > 5f,
 * move closer when distance > 1f.
 */
public class PartnerFollowComponent extends Component {
    private final Entity player;
    private static final float STOP_RADIUS = 1.0f;
    private static final float TELEPORT_R  = 5.0f;
    private static final float SPEED       = 8.0f;
    private static final Vector2 TELEPORT_OFFSET = new Vector2(0.8f, 0f);
    private boolean move = true;
    public PartnerFollowComponent(Entity player) {
        this.player = player;
    }

    @Override
    public void update() {
        if (player == null) return;
        if (!move) return;
        float dt = 0.016f;
        try {
            dt = ServiceLocator.getTimeSource().getDeltaTime();
        } catch (Exception ignored) {}

        Vector2 myPos = entity.getPosition();
        Vector2 plPos = player.getPosition();

        Vector2 toPlayer = plPos.cpy().sub(myPos);
        float d2 = toPlayer.len2();

        // Above 5f -> Teleport
        if (d2 > TELEPORT_R * TELEPORT_R) {
            entity.setPosition(plPos.x + TELEPORT_OFFSET.x, plPos.y + TELEPORT_OFFSET.y);
            return;
        }

        // Distance less than 1f -> Stop
        if (d2 <= STOP_RADIUS * STOP_RADIUS) {
            return;
        }

        // Move closer to the player
        if (!toPlayer.isZero()) {
            toPlayer.nor().scl(SPEED * dt);
            entity.setPosition(myPos.x + toPlayer.x, myPos.y + toPlayer.y);
        }
    }
    public void setMove(boolean move) {
        this.move = move;
    }
    public boolean isMove() {
        return move;
    }
}
