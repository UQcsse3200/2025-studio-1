package com.csse3200.game.physics;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class PhysicsProjectileComponentTest {
    @Mock PhysicsEngine engine;
    @Mock Body body;
    @Mock GameTime timeSource;

    @BeforeEach
    void beforeEach() {
        when(engine.createBody(any())).thenReturn(body);
        PhysicsService service = new PhysicsService(engine);
        ServiceLocator.registerPhysicsService(service);
        // register timesource before calling update()
        when(timeSource.getDeltaTime()).thenReturn(1f);
        ServiceLocator.registerTimeSource(timeSource);
    }

    private Entity makeEntityWithProjectile(PhysicsProjectileComponent
                                                    projectile) {
        Entity entity = new Entity();
        PhysicsComponent physicsComponent = new PhysicsComponent();
        entity.addComponent(physicsComponent);
        entity.addComponent(projectile);
        entity.create();
        return entity;
    }

    // create()
    @Test
    void bodyNoVelocityInitially() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        verify(body, never()).setLinearVelocity(any(Vector2.class));
    }

    @Test
    void bodyConfiguredOnCreate() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        verify(body).setBullet(true);
        verify(body).setGravityScale(0f);
        verify(body).setFixedRotation(true);
        verify(body).setLinearDamping(0f);
    }

    // fire()
    @Test
    void correctVelocity() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        Entity entity = makeEntityWithProjectile(projectile);

        entity.getComponent(PhysicsProjectileComponent.class)
                        .fire(new Vector2(1, 0), 3f);

        verify(body).setLinearVelocity(new Vector2(3f, 0));
    }

    @Test
    void fireNormalisesDirection() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        projectile.fire(new Vector2(5, 0), 2f);
        verify(body).setLinearVelocity(new Vector2(2f, 0f));
    }

    @Test
    void fireMultipleTimesOverridesVelocity() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        projectile.fire(new Vector2(1, 0), 1f);
        projectile.fire(new Vector2(0, 1), 4f);
        verify(body).setLinearVelocity(new Vector2(0f, 4f));
    }

    // update() &/ setLived()
    @Test
    void updateAddsDeltaTimeTriggersExpiry() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        when(timeSource.getDeltaTime()).thenReturn(1f);

        projectile.update();    // lived = 1
        projectile.update();    // lived = 2

        projectile.setLived(5f);
        projectile.update();    // should expire now

        verify(body).setLinearVelocity(new Vector2(0f, 0f));
    }

    @Test
    void projectileDoesNotExpireBeforeLifetime() {
        PhysicsProjectileComponent projectile = new PhysicsProjectileComponent();
        makeEntityWithProjectile(projectile);

        projectile.fire(new Vector2(1, 0), 3f);

        projectile.setLived(2f);
        projectile.update();

        verify(body, never()).setLinearVelocity(new Vector2(0f, 0f));
    }

    @AfterEach
    void afterEach() {
        ServiceLocator.clear();
        reset(body, engine, timeSource);
    }
}
