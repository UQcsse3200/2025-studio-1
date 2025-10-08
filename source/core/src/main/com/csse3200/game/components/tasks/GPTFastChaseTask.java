package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Chases a target entity indefinitely and the entity being visible causes a speed increase
 * CAN have added functionality to launch projectiles at the player too if wanted.
 */
public class GPTFastChaseTask extends DefaultTask implements PriorityTask {
    private static final Logger logger = LoggerFactory.getLogger(GPTFastChaseTask.class);
    private final Entity target;
    private final int priority;
    private final Vector2 speed;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;

    // Projectile configurations
    private ProjectileLauncherComponent projectileLauncher = null;
    private GameTime timeSource = null;
    private final float firingCooldown = 3f;
    private float currentCooldown = 3f;
    private Entity shooter = null;

    private Vector2 offsetPos;
    private boolean stuck = false;
    private final float offsetObstacle = 1.0f;

    /**
     * @param target   The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param speed    The speed at which the enemy will chase the player
     */
    public GPTFastChaseTask(Entity target, int priority, Vector2 speed) {
        this.target = target;
        this.priority = priority;
        this.speed = speed;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    /**
     * @param target             The entity to chase.
     * @param priority           Task priority when chasing (0 when not chasing).
     * @param speed              The speed at which the enemy will chase the player
     * @param projectileLauncher the projectile launcher component used to launch projectiles at the player
     * @param shooter            the enemy that is shooting the projectiles
     */
    public GPTFastChaseTask(Entity target, int priority, Vector2 speed,
                            ProjectileLauncherComponent projectileLauncher, Entity shooter) {
        this.target = target;
        this.priority = priority;
        this.speed = speed;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();

        // Projectile launcher
        this.projectileLauncher = projectileLauncher;
        timeSource = ServiceLocator.getTimeSource();
        this.shooter = shooter;
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition(), speed);
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    /**
     * Updates the enemy's movement logic each frame.
     * Handles obstacle detection, stuck detection, and decides whether
     * to chase the player directly or adjust the path around obstacles.
     */
    @Override
    public void update() {
        Vector2 currentPos = owner.getEntity().getCenterPosition();
        Vector2 targetPos = target.getPosition();

        // Detect obstacles in each direction
        boolean up    = hasObstacleUp(currentPos);
        boolean down  = hasObstacleDown(currentPos);
        boolean left  = hasObstacleLeft(currentPos);
        boolean right = hasObstacleRight(currentPos);

        boolean obstaclesNearby = up || down || left || right;
        boolean stuckDetected = movementTask.checkIfStuck() && obstaclesNearby;

        if (stuck && !avoidedObstacles()) {
            // Still trying to move away from last stuck position
            movementTask.setTarget(offsetPos);
        } else if (stuckDetected) {
            // Enemy just got stuck
            stuck = true; // Update flag status

            // Pick a nearby offset position to avoid obstacle
            offsetPos = getOffsetPosition(currentPos, targetPos, up, down, left, right);
            movementTask.setTarget(offsetPos);
        }  else {
            // No obstacles in the way —> move directly toward target
            stuck = false;
            movementTask.setTarget(targetPos);
        }
        // Continue movement task
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }

        FireLasers();
    }

    /**
     * Computes an offset position when stuck, to steer the enemy
     * around obstacles while still moving roughly toward the player.
     *
     * @param currentPos Enemy's current position
     * @param targetPos  Player's position
     * @param up         True if obstacle detected above
     * @param down       True if obstacle detected below
     * @param left       True if obstacle detected left
     * @param right      True if obstacle detected right
     * @return A new Vector2 offset position to move toward
     */
    private Vector2 getOffsetPosition(Vector2 currentPos, Vector2 targetPos,
                                      boolean up, boolean down, boolean left, boolean right) {
        float offsetDistance = 1f;  // How far to sidestep when stuck
        float offsetSteer = 0.01f;  // Small steering adjustment
        float rawX = currentPos.x;
        float rawY = currentPos.y + offsetDistance;

        // World boundaries
        float minX = 0, maxX = 13f, minY = 4f, maxY = 10.5f;
        float X, Y;

        // Determine relative position of target
        boolean targetAbove = targetPos.y > currentPos.y + 0.2;  // target has higher y coordinates
        boolean targetBelow = targetPos.y < currentPos.y - 0.2;  // target has lower y coordinates
        boolean targetLeft = targetPos.x < currentPos.x + 0.2;  // target has higher x coordinates
        boolean targetRight = targetPos.x > currentPos.x - 0.2;  // target has lower x coordinates

        // Adjust movement based on detected obstacles
        if (up && targetAbove) {
            rawX = currentPos.x + offsetDistance;
            if (right) rawX = currentPos.x - offsetDistance;  // obstacle right -> move left
            rawY = currentPos.y - offsetSteer;
        } else if (down && targetBelow) {
            rawX = currentPos.x + offsetDistance;
            if (right) rawX = currentPos.x - offsetDistance;
            rawY = currentPos.y + offsetSteer;
        }
        else if (left && targetLeft) {  // move up and steer right
            rawX = currentPos.x + offsetSteer;
            rawY = currentPos.y + offsetDistance;
            if (up) rawY = currentPos.y - offsetDistance;
        } else if (right && targetRight) {
            rawX = currentPos.x - offsetSteer;
            rawY = currentPos.y + offsetDistance;
            if (up) rawY = currentPos.y - offsetDistance;
        }
        // Clamp position to map bounds
        X = MathUtils.clamp(rawX, minX, maxX);
        Y = MathUtils.clamp(rawY, minY, maxY);
        return new Vector2(X, Y);
    }

    /**
     * If there is a projectile launcher present, fire lasers. If not, this does nothing.
     */
    public void FireLasers() {
        // Projectile launcher related
        if (isTargetVisible() && projectileLauncher != null) {
            currentCooldown += timeSource.getDeltaTime();

            if (currentCooldown >= firingCooldown) {
                currentCooldown = currentCooldown % firingCooldown;

                Vector2 dirToFire = new Vector2(target.getPosition().x - shooter.getPosition().x,
                        target.getPosition().y - shooter.getPosition().y);

                projectileLauncher.fireProjectile(dirToFire,
                        new Vector2(0.2f, 0.8f), new Vector2(0.5f, 0.5f));
            }
        }
    }

    @Override
    public void stop() {
        super.stop();
        movementTask.stop();
    }

    @Override
    public int getPriority() {
        // Only fast chase if the player is visible to the enemy
        if (isTargetVisible()) {
            return priority;
        }
        stuck = false;
        return -1;
    }

    private boolean isTargetVisible() {
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        // If there is an obstacle in the path to the player, not visible.
        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }

    /**
     * Checks whether the enemy has successfully avoided the obstacle
     * by reaching the offset position.
     *
     * @return true if enemy position is very close to the offset target
     */
    private boolean avoidedObstacles() {
        logger.info("Avoided obstacles: {}", owner.getEntity().getPosition().epsilonEquals(offsetPos, 0.01f));
        return owner.getEntity().getPosition().epsilonEquals(offsetPos, 0.01f);
    }

    /**
     * Detects obstacles to the left of the current position using raycasts.
     */
    private boolean hasObstacleLeft(Vector2 currentPos) {
        for (int i = -1; i <= 1; i++) {
            Vector2 toLeft = new Vector2(currentPos).add(-offsetObstacle, i * 0.5f);
            if (physics.raycast(currentPos, toLeft, PhysicsLayer.OBSTACLE, hit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects obstacles to the right of the current position using raycasts.
     */
    private boolean hasObstacleRight(Vector2 currentPos) {
        for (int i = -1; i <= 1; i++) {
            Vector2 toRight = new Vector2(currentPos).add(offsetObstacle, i * 0.5f);
            if (physics.raycast(currentPos, toRight, PhysicsLayer.OBSTACLE, hit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects obstacles above the current position using raycasts.
     */
    private boolean hasObstacleUp(Vector2 currentPos) {
        for (int i = -1; i <= 1; i++) {
            Vector2 toUp = new Vector2(currentPos).add(i * 0.5f, offsetObstacle);
            if (physics.raycast(currentPos, toUp, PhysicsLayer.OBSTACLE, hit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Detects obstacles below the current position using raycasts.
     */
    private boolean hasObstacleDown(Vector2 currentPos) {
        for (int i = -1; i <= 1; i++) {
            Vector2 toDown = new Vector2(currentPos).add(i * 0.5f, -offsetObstacle);
            if (physics.raycast(currentPos, toDown, PhysicsLayer.OBSTACLE, hit)) {
                return true;
            }
        }
        return false;
    }

}
