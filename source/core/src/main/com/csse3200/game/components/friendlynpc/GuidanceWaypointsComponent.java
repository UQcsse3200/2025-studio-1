package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

import java.util.List;

/**
 * 极简“引路”组件（MVP）：
 * - NPC 按顺序前往 waypoints；
 * - 每个路点会等待玩家进入 waitRadius 后再前往下一个；
 * - 通过事件与外部系统（对话/任务）解耦。
 *
 * 监听事件：
 *   - "guide:start"  -> 开始移动
 *   - "guide:pause"  -> 暂停
 *   - "guide:resume" -> 继续
 *   - "guide:reset"  -> 重置到第一个路点并暂停
 *
 * 触发事件：
 *   - "guide:reached" (int index) -> 到达 index 路点（落到点后触发）
 *   - "guide:finished"            -> 抵达最后一个路点
 */
public class GuidanceWaypointsComponent extends Component {
    private final Entity player;
    private final List<Vector2> waypoints;
    private final float waitRadius;    // 玩家需接近当前路点到这个半径内，才允许出发去下一站
    private final float moveSpeed;     // 平滑移动速度（teleportStep=false 时生效）
    private final boolean teleportStep; // true=每步瞬移；false=平滑移动

    private int index = 0;           // 当前“停靠”的路点；将前往 index+1
    private boolean started = false;
    private static final float EPS = 0.05f; // 认为已到达的距离阈值（平滑移动时）

    /**
     * @param player       玩家实体，用于判断是否跟上；可为 null（则不等待玩家）
     * @param waypoints    路点列表（建议 >=1）
     * @param waitRadius   等待半径（玩家到当前路点 <= 该半径才继续）
     * @param moveSpeed    平滑移动速度（单位/秒）
     * @param teleportStep true=跳点；false=平滑移动
     */
    public GuidanceWaypointsComponent(Entity player,
                                      List<Vector2> waypoints,
                                      float waitRadius,
                                      float moveSpeed,
                                      boolean teleportStep) {
        this.player = player;
        this.waypoints = waypoints;
        this.waitRadius = waitRadius;
        this.moveSpeed = moveSpeed;
        this.teleportStep = teleportStep;
    }

    @Override
    public void create() {
        // 事件接线（明天对话组件可以直接触发这些事件）
        if (getEntity() != null && getEntity().getEvents() != null) {
            getEntity().getEvents().addListener("guide:start", this::onStart);
            getEntity().getEvents().addListener("guide:pause", this::onPause);
            getEntity().getEvents().addListener("guide:resume", this::onResume);
            getEntity().getEvents().addListener("guide:reset", this::onReset);
        }

        // 可选：让 NPC 初始就站在第一个路点（避免出生坐标不一致）
        // if (waypoints != null && !waypoints.isEmpty()) {
        //   getEntity().setPosition(waypoints.get(0));
        // }
    }

    private void onStart() { started = true; }
    private void onPause() { started = false; }
    private void onResume() { started = true; }
    private void onReset() { index = 0; started = false; }

    @Override
    public void update() {
        if (!started) return;
        if (waypoints == null || waypoints.size() < 2) return;
        if (index >= waypoints.size() - 1) return; // 已到终点

        Vector2 curr = waypoints.get(index);
        Vector2 next = waypoints.get(index + 1);

        // 等玩家到当前路点附近
        if (player != null && player.getPosition().dst(curr) > waitRadius) {
            return;
        }

        Entity npc = getEntity();

        if (teleportStep) {
            // 稳定版本：直接跳到下一站
            npc.setPosition(next);
            index++;
            emitReached();
            if (index >= waypoints.size() - 1) emitFinished();
            return;
        }

        // 平滑移动
        Vector2 pos = npc.getPosition();
        Vector2 delta = new Vector2(next).sub(pos);
        float dist = delta.len();

        if (dist <= EPS) {
            npc.setPosition(next);
            index++;
            emitReached();
            if (index >= waypoints.size() - 1) emitFinished();
            return;
        }

        float step = moveSpeed * Gdx.graphics.getDeltaTime();
        if (step >= dist) {
            npc.setPosition(next);
            index++;
            emitReached();
            if (index >= waypoints.size() - 1) emitFinished();
        } else {
            delta.nor().scl(step);
            npc.setPosition(pos.add(delta));
        }
    }

    private void emitReached() {
        if (getEntity() != null && getEntity().getEvents() != null) {
            getEntity().getEvents().trigger("guide:reached", index); // 已经落在 index 这个点
        }
    }

    private void emitFinished() {
        if (getEntity() != null && getEntity().getEvents() != null) {
            getEntity().getEvents().trigger("guide:finished");
        }
    }
}