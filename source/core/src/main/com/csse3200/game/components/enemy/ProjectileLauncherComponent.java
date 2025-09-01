package com.csse3200.game.components.enemy;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.screens.MainGameScreen;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectileLauncherComponent extends Component {
    private long timeSinceFiring;
    private static final Logger projectileLogger = LoggerFactory.getLogger(ProjectileLauncherComponent.class);
    private static ForestGameArea forestGameArea;
    private Entity target;

    // area: The area it is living in
    public ProjectileLauncherComponent(ForestGameArea area, Entity target)
    {
        forestGameArea = area;
        this.target = target;
    }

    public void FireLaserProjectile(Vector2 directionToFire)
    {
        Entity laser = forestGameArea.spawnLaserProjectile(directionToFire);
        Vector2 laserOffset = new Vector2(0.2f, 0.8f);
        Vector2 pos = new Vector2(getEntity().getPosition().x + laserOffset.x, getEntity().getPosition().y + laserOffset.y);
        laser.setPosition(pos);
    }

    @Override
    public void update() {
        long currentTime = ServiceLocator.getTimeSource().getTime();

        if (currentTime - timeSinceFiring >= 1000L) {
            timeSinceFiring = currentTime;

            Vector2 dirToFire = new Vector2(target.getPosition().x - getEntity().getPosition().x,
            target.getPosition().y - getEntity().getPosition().y);

            FireLaserProjectile(dirToFire);
        }
    }
}
