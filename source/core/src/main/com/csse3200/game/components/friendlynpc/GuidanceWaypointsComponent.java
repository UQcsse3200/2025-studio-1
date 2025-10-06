package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import java.util.List;

/**
 * A simple guide component:
 * - Moves an NPC through given waypoints in order.
 * - Waits for the player to get close before moving to the next point.
 * - Can either teleport or move smoothly.
 * - Controlled by events.
 *
 * Listens for:
 *   "guide:start"  → start moving
 *   "guide:pause"  → pause
 *   "guide:resume" → resume
 *   "guide:reset"  → reset to the first waypoint
 *
 * Emits:
 *   "guide:reached" (index) → when reaching a waypoint
 *   "guide:finished"        → when reaching the last waypoint
 */
public class GuidanceWaypointsComponent extends Component {
    private final Entity player;
    private final List<Vector2> waypoints;
    private final float waitRadius;
    private final float moveSpeed;
    private final boolean teleportStep;

    private int index = 0;
    private boolean started = false;
    private static final float EPS = 0.05f;

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
        if (getEntity() != null && getEntity().getEvents() != null) {
            getEntity().getEvents().addListener("guide:start", this::onStart);
            getEntity().getEvents().addListener("guide:pause", this::onPause);
            getEntity().getEvents().addListener("guide:resume", this::onResume);
            getEntity().getEvents().addListener("guide:reset", this::onReset);
        }
    }

    private void onStart() { started = true; }
    private void onPause() { started = false; }
    private void onResume() { started = true; }
    private void onReset() { index = 0; started = false; }

    @Override
    public void update() {
        if (!started) return;
        if (waypoints == null || waypoints.size() < 2) return;
        if (index >= waypoints.size() - 1) return;

        Vector2 curr = waypoints.get(index);
        Vector2 next = waypoints.get(index + 1);

        // Wait until the player is close enough to the current waypoint
        if (player != null && player.getPosition().dst(curr) > waitRadius) return;

        Entity npc = getEntity();

        if (teleportStep) {
            npc.setPosition(next);
            index++;
            emitReached();
            if (index >= waypoints.size() - 1) emitFinished();
            return;
        }

        // Smooth movement
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
            getEntity().getEvents().trigger("guide:reached", index);
        }
    }

    private void emitFinished() {
        if (getEntity() != null && getEntity().getEvents() != null) {
            getEntity().getEvents().trigger("guide:finished");
        }
    }
}