package com.csse3200.game.components.player;

/**
 * Listens to player action events and validates them.
 * This does not mutate movement/physics; it logs and can be extended to gate actions.
 * <p>
 * Subscribed events (all optional in your codebase):
 * - "moveStart", "move", "moveStop"
 * - "jump", "dash"
 * - "attack", "shoot"
 * - "interact"
 */
public class PlayerActionValidator extends com.csse3200.game.components.Component {
    private static final org.slf4j.Logger L = org.slf4j.LoggerFactory.getLogger("PlayerActionValidator");

    // Keep these aligned with PlayerActions behaviour
    private static final long JUMP_COOLDOWN_MS = 300;  // PlayerActions JUMP_COOLDOWN_MS
    private static final long DASH_MIN_INTERVAL_MS = 150; // anti-spam (PlayerActions has its own long cooldown)

    private long lastJumpMs = -1;
    private long lastDashMs = -1;

    private com.csse3200.game.physics.components.PhysicsComponent phys;

    private static boolean isFinite(float v) {
        return !Float.isNaN(v) && !Float.isInfinite(v);
    }

    @Override
    public void create() {
        phys = entity.getComponent(com.csse3200.game.physics.components.PhysicsComponent.class);

        // Movement
        entity.getEvents().addListener("walk", this::onWalk);               // Vector2
        entity.getEvents().addListener("walkStop", this::onWalkStop);

        // Attempts (mirror PlayerActions)
        entity.getEvents().addListener("jumpAttempt", this::onJumpAttempt);
        entity.getEvents().addListener("sprintAttempt", this::onSprintAttempt);
        entity.getEvents().addListener("dashAttempt", this::onDashAttempt);
        entity.getEvents().addListener("crouchAttempt", this::onCrouchAttempt);
        entity.getEvents().addListener("crouchStop", this::onCrouchStop);

        // Combat / misc
        entity.getEvents().addListener("attack", this::onAttack);
        entity.getEvents().addListener("shoot", this::onShoot);
        entity.getEvents().addListener("reload", this::onReload);
    }

    /* ---------------- movement ---------------- */
    private void onWalk(com.badlogic.gdx.math.Vector2 dir) {
        if (!ok("walk")) return;
        if (dir == null || !isFinite(dir.x) || !isFinite(dir.y)) {
            L.warn("walk invalid vector: {}", dir);
            return;
        }
        if (Math.abs(dir.x) > 1f || Math.abs(dir.y) > 1f) {
            L.debug("walk direction clamped range expected [-1,1], got {}", dir);
        }
    }

    private void onWalkStop() {
        ok("walkStop");
    }

    /* ---------------- attempts ---------------- */
    private void onJumpAttempt() {
        if (!ok("jumpAttempt")) return;
        long now = com.csse3200.game.services.ServiceLocator.getTimeSource().getTime();
        if (lastJumpMs >= 0 && (now - lastJumpMs) < JUMP_COOLDOWN_MS) {
            L.debug("jumpAttempt rate-limited ({}ms since last, need {}ms)", now - lastJumpMs, JUMP_COOLDOWN_MS);
        }
        if (!isGrounded()) {
            // PlayerActions supports double-jump; just inform so you see midair spam patterns
            L.trace("jumpAttempt while midair (double-jump path)");
        }
        lastJumpMs = now;
    }

    private void onSprintAttempt() {
        if (!ok("sprintAttempt")) return;
        if (!isGrounded()) {
            L.trace("sprintAttempt while airborne");
        }
    }

    private void onDashAttempt() {
        if (!ok("dashAttempt")) return;
        long now = com.csse3200.game.services.ServiceLocator.getTimeSource().getTime();
        if (lastDashMs >= 0 && (now - lastDashMs) < DASH_MIN_INTERVAL_MS) {
            L.debug("dashAttempt spammy ({}ms < {}ms)", now - lastDashMs, DASH_MIN_INTERVAL_MS);
        }
        lastDashMs = now;
    }

    private void onCrouchAttempt() {
        if (!ok("crouchAttempt")) return;
        if (!isGrounded()) {
            L.debug("crouchAttempt rejected sanity: airborne");
        }
    }

    private void onCrouchStop() {
        ok("crouchStop");
    }

    /* ---------------- combat/misc ---------------- */
    private void onAttack() {
        ok("attack");
    }

    private void onShoot() {
        ok("shoot");
    }

    private void onReload() {
        ok("reload");
    }

    /* ---------------- helpers ---------------- */
    private boolean ok(String action) {
        var time = com.csse3200.game.services.ServiceLocator.getTimeSource();
        if (time != null && time.isPaused()) {
            L.trace("{} ignored: paused", action);
            return false;
        }
        if (com.csse3200.game.services.ServiceLocator.isTransitioning()) {
            L.trace("{} ignored: transitioning", action);
            return false;
        }

        return true;
    }

    private boolean isGrounded() {
        if (phys == null || phys.getBody() == null) return true; // be permissive if unknown
        return Math.abs(phys.getBody().getLinearVelocity().y) < 0.01f;
    }
}