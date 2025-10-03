package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * 伙伴传送跟随组件：
 * 如果距离目标超过 maxDistance，就瞬移到目标旁边。
 */
public class TeleportFollowComponent extends Component {
    private final Entity target;
    private final float maxDistance;

    public TeleportFollowComponent(Entity target, float maxDistance) {
        this.target = target;
        this.maxDistance = maxDistance;
    }

    @Override
    public void update() {
        if (entity == null || target == null) return;

        Vector2 myPos = entity.getPosition();
        Vector2 targetPos = target.getPosition();

        if (myPos.dst(targetPos) > maxDistance) {
            // 瞬移到玩家旁边（右边偏移 1f）
            entity.setPosition(targetPos.cpy().add(1f, 0f));
        }
    }
}
