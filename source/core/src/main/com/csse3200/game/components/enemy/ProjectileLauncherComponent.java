package com.csse3200.game.components.enemy;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class containing methods that allow entities to fire projectiles essentially.
 */
public class ProjectileLauncherComponent extends Component {
    private long timeSinceFiring;
    private static final Logger projectileLogger = LoggerFactory.getLogger(ProjectileLauncherComponent.class);
    private static GameArea forestGameArea;
    private Entity target;

    /**
     * Constructor for the class.
     * @param area The area, such as ForestGameArea, in which the entity is residing in.
     * @param target The target entity, that this component's entity wants to hit.
     */
    public ProjectileLauncherComponent(GameArea area, Entity target)
    {
        forestGameArea = area;
        this.target = target;
    }

    /**
     * Fires a projectile, classified as an enemy projectile, in the direction specified.
     * @param directionToFire The direction to fire in
     * @param offset Offset (from the center) where the projectile is fired
     * @param scale The size of the projectile. "x" value represents width, and "y" value represents height.\
     */
    public void FireProjectile(Vector2 directionToFire, Vector2 offset, Vector2 scale)
    {
        WeaponsStatsComponent weapon = entity.getComponent(WeaponsStatsComponent.class);
        Entity projectile = forestGameArea.spawnEnemyProjectile(directionToFire, weapon);
        Vector2 pos = new Vector2(getEntity().getPosition().x + offset.x,
                                getEntity().getPosition().y + offset.y);
        projectile.setPosition(pos);
        projectile.scaleWidth(scale.x);
        projectile.scaleHeight(scale.y);
    }

    /**
     * Fires multiple laser projectiles, classified as enemy projectiles, at once
     * @param amount The amount of projectiles to fire in one go
     * @param angleDifferences The angle differences, in degrees, between lasers. For example, passing in 10 means
     *                          10 degree difference in the rotation of each laser projectile.
     * @param directionToFire The direction to fire at.
     * @param offset Offset (from the center) where the projectile is fired
     * @param scale The size of the projectile. "x" value represents width, and "y" value represents height.
     */
    public void FireProjectileMultishot(int amount, float angleDifferences,
                                            Vector2 directionToFire, Vector2 offset, Vector2 scale)
    {
        directionToFire = directionToFire.rotateDeg(-angleDifferences * ((float)amount/2));

            for (int i = 0; i < amount; i++)
            {
                FireProjectile(directionToFire, offset, scale);
                directionToFire.rotateDeg(angleDifferences);
            }
    }

    /**
     * A quick burst of repeated laser projectile firings. Note that all lasers will head in the same direction, even if
     * the player has moved during the burst duration.
     * @param burstAmount The amount of projectiles to fire in a burst.
     * @param timeBetweenShots The time, in seconds, between each laser fired within one burst sequence.
     * @param directionToFire The direction to fire at.
     * @param offset Offset (from the center) where the projectile is fired
     * @param scale The size of the projectile. "x" value represents width, and "y" value represents height.
     */
    public void FireProjectileBurstFire(int burstAmount, float timeBetweenShots,
                                             Vector2 directionToFire, Vector2 offset, Vector2 scale)

    {
        Timer.Task burstFireTask = new Timer.Task() {
            int currentCount = 0;

            @Override
            public void run() {
                // An error would keep occurring with the physics server upon cleanup. Have to check that it no longer
                // exists.
                if (ServiceLocator.getPhysicsService() == null) {
                    cancel(); // stop task if physics no longer exists
                    return;
                }

                FireProjectile(directionToFire, offset, scale);
                currentCount++;
                if (currentCount >= burstAmount) { cancel(); };
            }
        };

        Timer.schedule(burstFireTask, 0f, timeBetweenShots);
    }
}
