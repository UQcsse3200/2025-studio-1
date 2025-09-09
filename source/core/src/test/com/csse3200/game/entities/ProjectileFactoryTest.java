package com.csse3200.game.entities;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProjectileFactoryTest {

    @Mock
    PhysicsEngine engine;
    @Mock
    Body body;
    static Entity pistolBullet;


    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {

       Gdx.files = mock(Files.class);
        when(Gdx.files.internal(anyString())).thenReturn(mock(FileHandle.class));

        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        Body physicsBody = mock(Body.class);
        when(physicsEngine.createBody(any())).thenReturn(physicsBody);
        PhysicsService physicsService = new PhysicsService(physicsEngine);
        ServiceLocator.registerPhysicsService(physicsService);

        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);
        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);

        Entity pistol = WeaponsFactory.createWeapon(Weapons.PISTOL);
        pistolBullet = ProjectileFactory.createPistolBullet(
                pistol.getComponent(WeaponsStatsComponent.class)
        );
    }


   @Test
    void correctPistolBulletComponents() {
        assertTrue(pistolBullet.hasComponent(PhysicsComponent.class));
        assertTrue(pistolBullet.hasComponent(PhysicsProjectileComponent.class));
        assertTrue(pistolBullet.hasComponent(WeaponsStatsComponent.class));
        assertTrue(pistolBullet.hasComponent(TextureRenderWithRotationComponent.class));
        assertTrue(pistolBullet.hasComponent(ColliderComponent.class));
        assertTrue(pistolBullet.hasComponent(HitboxComponent.class));
        assertTrue(pistolBullet.hasComponent(TouchAttackComponent.class));
    }
}
