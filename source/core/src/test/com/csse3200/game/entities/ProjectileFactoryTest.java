package com.csse3200.game.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.entities.configs.projectiles.PistolBulletConfig;
import com.csse3200.game.entities.configs.projectiles.ProjectileConfig;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProjectileFactoryTest {

    @Mock
    PhysicsEngine engine;
    @Mock
    Body body;
    @Mock
    static ProjectileConfig configs;
    @Mock
    static PistolBulletConfig pistolBulletConfig;



    @BeforeEach
    void prepare() {
        when(this.engine.createBody(any())).thenReturn(this.body);
        PhysicsService service = new PhysicsService(engine);
        ServiceLocator.registerPhysicsService(service);
        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);
        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
    }

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {

        Gdx.files = mock(Files.class);
        when(Gdx.files.internal(anyString())).thenReturn(mock(FileHandle.class));

        pistolBulletConfig = new PistolBulletConfig();
        pistolBulletConfig.baseAttack = 8;
        pistolBulletConfig.health = 10000;
        pistolBulletConfig.speed = 3.0f;

        configs = new ProjectileConfig();
        configs.pistolBullet = pistolBulletConfig;

        Field configsField = ProjectileFactory.class.getDeclaredField("configs");
        configsField.setAccessible(true);
        configsField.set(null, configs);
    }


   @Test
    void correctPistolBulletComponents() {
        Entity bullet = ProjectileFactory.createPistolBullet();
        Assert.assertNotNull(bullet.getComponent(PhysicsComponent.class));
        Assert.assertNotNull(bullet.getComponent(PhysicsProjectileComponent.class));
        Assert.assertNotNull(bullet.getComponent(CombatStatsComponent.class));
        Assert.assertNotNull(bullet.getComponent(TextureRenderComponent.class));
        Assert.assertNotNull(bullet.getComponent(ColliderComponent.class));
        Assert.assertNotNull(bullet.getComponent(HitboxComponent.class));
        Assert.assertNotNull(bullet.getComponent(TouchAttackComponent.class));
    }


}
