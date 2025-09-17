package com.csse3200.game.components.player;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.*;
import com.csse3200.game.components.InventoryComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.PistolConfig;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;



@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class PlayerCombatActionsTest {
    Entity player;
    WeaponsStatsComponent weaponStats;
    com.csse3200.game.components.player.InventoryComponent inventory;
    MagazineComponent magazine;
    AmmoStatsComponent ammo;
    PlayerActions playerActions;

    @Mock
    GameTime timeSource;
    @Mock
    ResourceService resourceService;
    @Mock
    Sound sfx;
    @Mock
    PhysicsService physicsService;
    @Mock
    EntityService entityService;

    @Test
    void testShoot() {

        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.registerPhysicsService(physicsService);
        ServiceLocator.registerTimeSource(timeSource);
        ServiceLocator.registerEntityService(entityService);

        when(resourceService.getAsset("sounds/laser_blast.mp3", Sound.class)).thenReturn(sfx);

        ItemComponent itemComponent = mock(ItemComponent.class);
        when(itemComponent.getTexture()).thenReturn(null);

        player = new Entity();
        Entity gun = new Entity();
        magazine = new MagazineComponent(12);
        inventory = new com.csse3200.game.components.player.InventoryComponent(3);
        ammo = new AmmoStatsComponent(1000);
        playerActions = new PlayerActions();
        weaponStats = new WeaponsStatsComponent(9);

        gun.addComponent(itemComponent);
        gun.addComponent(magazine);
        gun.addComponent(weaponStats);

        playerActions.setTimeSinceLastAttack(0.21f);

        Camera mockCamera = mock(Camera.class);
        when(mockCamera.unproject(any(Vector3.class)))
                .thenReturn(new Vector3(100, 100, 0));
        playerActions.setCamera(mockCamera);

        Entity mockBullet = mock(Entity.class);
        PhysicsProjectileComponent projectilePhysics = mock(PhysicsProjectileComponent.class);
        when(mockBullet.getComponent(PhysicsProjectileComponent.class)).thenReturn(projectilePhysics);
        when(mockBullet.getScale()).thenReturn(new Vector2(1, 1));

        player.setPosition(50, 50);
        player.addComponent(inventory);
        player.addComponent(ammo);
        player.addComponent(playerActions);
        inventory.addItem(gun);

        try (MockedStatic<ProjectileFactory> mockedFactory = mockStatic(ProjectileFactory.class)) {
            mockedFactory.when(() -> ProjectileFactory.createPistolBullet(any(WeaponsStatsComponent.class)))
                    .thenReturn(mockBullet);

            playerActions.shoot();
            verify(entityService).register(mockBullet);
        }

        Assertions.assertEquals(11, magazine.getCurrentAmmo());
    }

    @Test
    void testReload() {

        {

            ServiceLocator.registerResourceService(resourceService);
            ServiceLocator.registerPhysicsService(physicsService);
            ServiceLocator.registerTimeSource(timeSource);
            ServiceLocator.registerEntityService(entityService);

            when(resourceService.getAsset("sounds/laser_blast.mp3", Sound.class)).thenReturn(sfx);
            when(resourceService.getAsset("sounds/reload.mp3", Sound.class)).thenReturn(sfx);

            ItemComponent itemComponent = mock(ItemComponent.class);
            when(itemComponent.getTexture()).thenReturn(null);

            player = new Entity();
            Entity gun = new Entity();
            magazine = new MagazineComponent(12);
            inventory = new com.csse3200.game.components.player.InventoryComponent(3);
            ammo = new AmmoStatsComponent(1000);
            playerActions = new PlayerActions();
            weaponStats = new WeaponsStatsComponent(9);

            gun.addComponent(itemComponent);
            gun.addComponent(magazine);
            gun.addComponent(weaponStats);

            playerActions.setTimeSinceLastAttack(0.21f);

            Camera mockCamera = mock(Camera.class);
            when(mockCamera.unproject(any(Vector3.class)))
                    .thenReturn(new Vector3(100, 100, 0));
            playerActions.setCamera(mockCamera);

            Entity mockBullet = mock(Entity.class);
            PhysicsProjectileComponent projectilePhysics = mock(PhysicsProjectileComponent.class);
            when(mockBullet.getComponent(PhysicsProjectileComponent.class)).thenReturn(projectilePhysics);
            when(mockBullet.getScale()).thenReturn(new Vector2(1, 1));

            player.setPosition(50, 50);
            player.addComponent(inventory);
            player.addComponent(ammo);
            player.addComponent(playerActions);
            inventory.addItem(gun);

            for (int i = 0; i < 5; i++) {
                playerActions.setTimeSinceLastAttack(0.21f);
                try (MockedStatic<ProjectileFactory> mockedFactory = mockStatic(ProjectileFactory.class)) {
                    mockedFactory.when(() -> ProjectileFactory.createPistolBullet(any(WeaponsStatsComponent.class)))
                            .thenReturn(mockBullet);


                    playerActions.shoot();

                }
            }
            Assertions.assertEquals(7, magazine.getCurrentAmmo());
            playerActions.reload();
            Assertions.assertEquals(12, magazine.getCurrentAmmo());
            Assertions.assertEquals(995, ammo.getAmmo());
        }

    }


    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }
}
