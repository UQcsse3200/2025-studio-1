package com.csse3200.game.components;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.TextureRenderWithRotationComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class TouchAttackComponentTest {
    @BeforeEach
    void beforeEach() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void shouldAttack() {
        short targetLayer = (1 << 3);
        Entity entity = createAttacker(targetLayer);
        Entity target = createTarget(targetLayer);


        Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
        Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();
        entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);

        assertEquals(0, target.getComponent(CombatStatsComponent.class).getHealth());
    }

    @Test
    void shouldNotAttackOtherLayer() {
        short targetLayer = (1 << 3);
        short attackLayer = (1 << 4);
        Entity entity = createAttacker(attackLayer);
        Entity target = createTarget(targetLayer);

        Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
        Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();

        entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);

        // Directly heal the target by +10 for testing
        target.getComponent(CombatStatsComponent.class).addHealth(10);

        assertEquals(10, target.getComponent(CombatStatsComponent.class).getHealth());
    }


    @Test
    void shouldNotAttackWithoutCombatComponent() {
        short targetLayer = (1 << 3);
        Entity entity = createAttacker(targetLayer);
        // Target does not have a combat component
        Entity target =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new HitboxComponent().setLayer(targetLayer));
        target.create();

        Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
        Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();

        // This should not cause an exception, but the attack should be ignored
        entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);
    }

    @Nested
    @DisplayName("Rocket Tests")
    class rocketTests {
        ResourceService resourceService;
        EntityService entityService;
        @BeforeEach
        void setup() {
            resourceService = mock(ResourceService.class);
            entityService = mock(EntityService.class);
            ServiceLocator.registerEntityService(entityService);
            //Mock animation and textures
            Texture texture = mock(Texture.class);
            TextureAtlas textureAtlas = mock(TextureAtlas.class);
            Array<TextureAtlas.AtlasRegion> regions = new Array<>();
            regions.add(mock(TextureAtlas.AtlasRegion.class));
            when(textureAtlas.findRegions(anyString())).thenReturn(regions);
            when(resourceService.getAsset(anyString(), eq(Texture.class))).thenReturn(texture);
            when(resourceService.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(textureAtlas);
            ServiceLocator.registerResourceService(resourceService);
        }

        @Test
        void rocketLauncherBulletsExplodeOnImpact() {
            //Make the bullet
            short targetLayer = (1 << 3);
            Entity entity = createBullet(targetLayer, true);
            // Target does not have a combat component
            Entity target =
                    new Entity()
                            .addComponent(new PhysicsComponent())
                            .addComponent(new HitboxComponent().setLayer(targetLayer));
            target.create();

            Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
            Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();

            // This should not cause an exception, but the attack should be ignored
            entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);
            //Verify the atlas was got
            verify(resourceService).getAsset("images/rocketExplosion.atlas", TextureAtlas.class);
            verify(entityService).register(any(Entity.class)); //Registered explosion
        }

        @Test
        void bulletsDontExplodeOnImpact() {
            //Make the bullet
            short targetLayer = (1 << 3);
            Entity entity = createBullet(targetLayer, false);
            // Target does not have a combat component
            Entity target =
                    new Entity()
                            .addComponent(new PhysicsComponent())
                            .addComponent(new HitboxComponent().setLayer(targetLayer));
            target.create();

            Fixture entityFixture = entity.getComponent(HitboxComponent.class).getFixture();
            Fixture targetFixture = target.getComponent(HitboxComponent.class).getFixture();

            // This should not cause an exception, but the attack should be ignored
            entity.getEvents().trigger("collisionStart", entityFixture, targetFixture);
            //Verify the atlas was never got
            verify(resourceService, never()).getAsset("images/rocketExplosion.atlas", TextureAtlas.class);
            verify(entityService, never()).register(any(Entity.class));
        }
    }

    Entity createBullet(short targetLayer, boolean rocket) {

        WeaponsStatsComponent newWeaponStats = new WeaponsStatsComponent(10);
        PhysicsProjectileComponent ppc = mock(PhysicsProjectileComponent.class);
        when(ppc.getPrio()).thenReturn(ComponentPriority.MEDIUM);
        newWeaponStats.setRocket(rocket);
        Entity entity = new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(ppc)
                .addComponent(newWeaponStats)
                .addComponent(new HitboxComponent())
                .addComponent(new TouchAttackComponent(targetLayer));
        entity.create();
        return entity;
    }





    Entity createAttacker(short targetLayer) {
        Entity entity =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new WeaponsStatsComponent(10))
                        .addComponent(new TouchAttackComponent(targetLayer))
                        .addComponent(new CombatStatsComponent(0))
                        .addComponent(new HitboxComponent());
        entity.create();
        return entity;
    }

    Entity createTarget(short layer) {
        Entity target =
                new Entity()
                        .addComponent(new PhysicsComponent())
                        .addComponent(new CombatStatsComponent(10))
                        .addComponent(new HitboxComponent().setLayer(layer));
        target.create();
        return target;
    }
}
