package com.csse3200.game.entities;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.projectiles.ProjectileTarget;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class ProjectileFactoryTest {

    @Mock
    PhysicsEngine engine;
    @Mock
    Body body;
    static Entity pistolBullet;
    static HeadlessApplication app;


    @BeforeEach
    void setUp() {
        app = new HeadlessApplication(new ApplicationAdapter() {
        }, new HeadlessApplicationConfiguration());

        // Load Box2D native libs
        Box2D.init();

        Gdx.files = mock(Files.class);
        Entity player = new Entity();
        ServiceLocator.registerPlayer(player);
        player.addComponent(mock(InventoryComponent.class));

        PhysicsEngine physicsEngine = mock(PhysicsEngine.class);
        PhysicsService physicsService = new PhysicsService(physicsEngine);
        ServiceLocator.registerPhysicsService(physicsService);

        ResourceService resourceService = mock(ResourceService.class);
        ServiceLocator.registerResourceService(resourceService);
        Texture texture = mock(Texture.class);
        when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);

        RenderService renderService = mock(RenderService.class);
        ServiceLocator.registerRenderService(renderService);

        Entity pistol = WeaponsFactory.createWeapon(Weapons.PISTOL);
        when(player.getComponent(InventoryComponent.class).getCurrSlot()).thenReturn(pistol);
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

    @Test
    void rocketBulletIsCorrect() {
        WeaponsStatsComponent wsc = mock(WeaponsStatsComponent.class);
        when(wsc.getRocket()).thenReturn(true);
        Entity projectile = ProjectileFactory.createPistolBullet(wsc);
        assertEquals(projectile.getComponent(TextureRenderWithRotationComponent.class).getTexturePath(),
                "images/rocket.png");
    }

    @Test
    void waterBulletIsCorrect() {
        WeaponsStatsComponent wsc = mock(WeaponsStatsComponent.class);
        when(wsc.getRocket()).thenReturn(false);
        Entity waterGun = new Entity().addComponent(new BulletEnhancerComponent());
        when(ServiceLocator.getPlayer().getComponent(InventoryComponent.class).getCurrSlot()).thenReturn(waterGun);

        Entity projectile = ProjectileFactory.createPistolBullet(wsc);
        assertEquals(projectile.getComponent(TextureRenderWithRotationComponent.class).getTexturePath(),
                "images/waterBullet.png");
    }


}
