package com.csse3200.game.physics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.components.HomingPhysicsComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class HomingPhysicsComponentTest {
    @Mock
    PhysicsEngine engine;
    @Mock
    Body body;
    @Mock
    GameTime timeSource;

    private Entity makeEntityWithProjectile(HomingPhysicsComponent projectile) {
        Entity entity = new Entity();
        PhysicsComponent physicsComponent = new PhysicsComponent();
        entity.addComponent(physicsComponent);
        entity.addComponent(projectile);
        entity.create();
        return entity;
    }

    @BeforeEach
    void beforeEach() {
        when(engine.createBody(any())).thenReturn(body);
        PhysicsService service = new PhysicsService(engine);
        ServiceLocator.registerPhysicsService(service);
        when(timeSource.getDeltaTime()).thenReturn(1f);
        ServiceLocator.registerTimeSource(timeSource);
        ServiceLocator.registerEntityService(new EntityService());
    }

    @AfterEach
    void afterEach() {
        ServiceLocator.clear();
        reset(body, engine, timeSource);
    }

    @Test
    void acquiresNearestLivingTargetOnCreate() {
        CameraComponent cameraComponent = mock(CameraComponent.class);
        Camera mockCamera = mock(Camera.class);
        when(mockCamera.unproject(any(Vector3.class)))
                .thenAnswer(invocation -> (Vector3) invocation.getArgument(0));
        when(cameraComponent.getCamera()).thenReturn(mockCamera);
        Entity cameraEntity = new Entity().addComponent(cameraComponent);
        ServiceLocator.getEntityService().register(cameraEntity);
        Entity target = new Entity()
                .addComponent(new CombatStatsComponent(10));
        target.setPosition(5f, 0f);
        ServiceLocator.getEntityService().register(target);
        HomingPhysicsComponent projectile = new HomingPhysicsComponent();
        Entity entity = makeEntityWithProjectile(projectile);
        projectile.fire(new Vector2(1, 0), 5f);
        Assertions.assertEquals(target, projectile.getTargetEntity());
    }

    @Test
    void updateTurnsTowardTarget() {
        CameraComponent cameraComponent = mock(CameraComponent.class);
        Camera mockCamera = mock(Camera.class);
        when(mockCamera.unproject(any(Vector3.class)))
                .thenAnswer(invocation -> (Vector3) invocation.getArgument(0));
        when(cameraComponent.getCamera()).thenReturn(mockCamera);
        Entity cameraEntity = new Entity().addComponent(cameraComponent);
        ServiceLocator.getEntityService().register(cameraEntity);
        Entity target = new Entity()
                .addComponent(new CombatStatsComponent(10));
        target.setPosition(10f, 0f);
        ServiceLocator.getEntityService().register(target);
        HomingPhysicsComponent projectile = new HomingPhysicsComponent();
        Entity entity = makeEntityWithProjectile(projectile);
        projectile.fire(new Vector2(0, 1), 5f);
        projectile.setTargetEntity(target);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0, 1));
        projectile.update();
        verify(body, atLeastOnce()).setLinearVelocity(argThat(v -> v.len() == 5f));
    }

    @Test
    void updateExpiresAfterLifetime() {
        CameraComponent cameraComponent = mock(CameraComponent.class);
        Camera mockCamera = mock(Camera.class);
        when(mockCamera.unproject(any(Vector3.class)))
                .thenAnswer(invocation -> (Vector3) invocation.getArgument(0));
        when(cameraComponent.getCamera()).thenReturn(mockCamera);
        Entity cameraEntity = new Entity().addComponent(cameraComponent);
        ServiceLocator.getEntityService().register(cameraEntity);
        HomingPhysicsComponent projectile = new HomingPhysicsComponent();
        Entity entity = makeEntityWithProjectile(projectile);

        projectile.fire(new Vector2(1, 0), 3f);
        projectile.setLived(10f);
        projectile.update();
        verify(body).setLinearVelocity(new Vector2(0f, 0f));
    }

    @Test
    void setTurnRateUpdatesValue() {
        CameraComponent cameraComponent = mock(CameraComponent.class);
        Camera mockCamera = mock(Camera.class);
        when(mockCamera.unproject(any(Vector3.class)))
                .thenAnswer(invocation -> (Vector3) invocation.getArgument(0));
        when(cameraComponent.getCamera()).thenReturn(mockCamera);
        Entity cameraEntity = new Entity().addComponent(cameraComponent);
        ServiceLocator.getEntityService().register(cameraEntity);
        HomingPhysicsComponent projectile = new HomingPhysicsComponent();
        makeEntityWithProjectile(projectile);
        projectile.setTurnRate(50f);
        Assertions.assertEquals(50f, projectile.getTurnRate());
    }
}