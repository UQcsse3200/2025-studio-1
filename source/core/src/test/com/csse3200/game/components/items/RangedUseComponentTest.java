package com.csse3200.game.components.items;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

// ------------------- NEEDS TO BE FIXED ----------------------------
/*
@DisplayName("RangedUseComponent Testing")
class RangedUseComponentTest {
    private RangedUseComponent rangedUseComponent;
    private Entity testWeapon;
    private Entity testConsumable;
    private Entity camera;

    @BeforeEach
    void setUp() {
        rangedUseComponent = new RangedUseComponent();
        camera = RenderFactory.createCamera();
        EntityService entityService = new EntityService();
        ResourceService resourceService = new ResourceService();
        ServiceLocator.registerEntityService(entityService);
        ServiceLocator.registerResourceService(resourceService);
        ServiceLocator.getEntityService().register(camera);

        String[] texturesNeeded = {"images/electric_zap.png", "images/rifle.png", "images/lightning_bottle.png",
                "images/round.png"};
        String[] soundNeeded = {"sounds/Impact4.ogg"};

        ServiceLocator.getResourceService().loadTextures(texturesNeeded);
        ServiceLocator.getResourceService().loadSounds(soundNeeded);

        ServiceLocator.getResourceService().loadAll();

        testWeapon = WeaponsFactory.createWeapon(Weapons.RIFLE);
        testConsumable = ConsumableFactory.createConsumable(Consumables.LIGHTNING_IN_A_BOTTLE);
    }

    @Test
    @DisplayName("Ranged Use Plays Default Sound")
    void RangedUsedPlaysDefaultSound() {
        rangedUseComponent.setEntity(testWeapon);
        rangedUseComponent.playAttackSound();
        Sound defaultSound = ServiceLocator.getResourceService()
                .getAsset("sounds/Impact4.ogg", Sound.class);
        assertTrue(defaultSound.play() != -1);
    }

    @Test
    @DisplayName("Finding camera")
    void CanFindCamera() {
        assertNotNull(rangedUseComponent.getActiveCamera());
        Camera cam = camera.getComponent(CameraComponent.class).getCamera();
        assertEquals(cam, rangedUseComponent.getActiveCamera());
    }

    @Nested
    @DisplayName("Projectile Creation And Firing")
    class ProjectileInitialisation {

        @Test
        @DisplayName("Create Weapon Projectile")
        void createProjectile() {
            rangedUseComponent.setEntity(testWeapon);
            WeaponsStatsComponent weaponStats = testWeapon.getComponent(WeaponsStatsComponent.class);
            String texture = testWeapon.getComponent(ItemComponent.class).getTexture();
            Entity projectile = rangedUseComponent.createProjectileEntity(weaponStats, texture);

            assertNotNull(projectile);
        }

        @Test
        @DisplayName("Create Consumable Projectile")
        void createBombProjectile() {
            rangedUseComponent.setEntity(testConsumable);
            WeaponsStatsComponent weaponStats = testConsumable.getComponent(WeaponsStatsComponent.class);
            String texture = testConsumable.getComponent(ItemComponent.class).getTexture();
            Entity projectile = rangedUseComponent.createProjectileEntity(weaponStats, texture);

            assertNotNull(projectile);
        }
    }

}

 */
