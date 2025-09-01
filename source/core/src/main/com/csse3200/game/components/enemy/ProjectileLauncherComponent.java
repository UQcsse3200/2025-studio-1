package com.csse3200.game.components.enemy;
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

    private static final String[] textures = new String[] {
            "images/laser_shot.png"
    };

    // area: The area it is living in
    public ProjectileLauncherComponent(ForestGameArea area)
    {
        forestGameArea = area;
    }

    public void FireLaserProjectile()
    {
        Entity laser = forestGameArea.spawnLaserProjectile();
        laser.setPosition(getEntity().getPosition());
    }

    @Override
    public void update() {
        long currentTime = ServiceLocator.getTimeSource().getTime();
        if (currentTime - timeSinceFiring >= 1000L) {
            timeSinceFiring = currentTime;
            FireLaserProjectile();
        }
    }
}
