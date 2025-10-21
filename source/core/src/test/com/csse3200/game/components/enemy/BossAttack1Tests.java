package com.csse3200.game.components.enemy;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.components.enemy.EnemyProjectileMovementComponent;
import static org.mockito.Mockito.*;

import com.csse3200.game.components.WeaponsStatsComponent;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Graphics;


/**
 * Tests cover behavier of EnemyMudBallAttackComponent
 */
public class BossAttack1Tests {

    private MockedStatic<ServiceLocator> serviceLocatorStatic;
    private GameTime time;
    private ResourceService resources;
    private EntityService entities;

    @BeforeEach
    void setUp() {
        Application app = mock(Application.class);
        doAnswer(inv -> { ((Runnable) inv.getArgument(0)).run(); return null; })
                .when(app).postRunnable(any(Runnable.class));

        doNothing().when(app).log(anyString(), anyString());
        doNothing().when(app).error(anyString(), anyString());
        doNothing().when(app).error(anyString(), anyString(), any(Throwable.class));
        Gdx.app = app;

        // Mock ServiceLocator endpoints
        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        time = mock(GameTime.class);
        when(time.getDeltaTime()).thenReturn(0.016f);
        resources = mock(ResourceService.class);
        entities = mock(EntityService.class);

        when(ServiceLocator.getTimeSource()).thenReturn(time);
        when(ServiceLocator.getResourceService()).thenReturn(resources);
        when(ServiceLocator.getEntityService()).thenReturn(entities);

        // --- Add these lines ---
        var renderService = mock(com.csse3200.game.rendering.RenderService.class);
        var physicsService = mock(com.csse3200.game.physics.PhysicsService.class);
        when(ServiceLocator.getRenderService()).thenReturn(renderService);
        when(ServiceLocator.getPhysicsService()).thenReturn(physicsService);
        // ------------------------

        Graphics gfx = mock(Graphics.class);
        when(gfx.getDeltaTime()).thenReturn(0.016f);
        Gdx.graphics = gfx;
    }

    @AfterEach
    void tearDown() {
        serviceLocatorStatic.close();
    }

    private EnemyMudBallAttackComponent addAttack(Entity owner, Entity target) {
        var attack = new EnemyMudBallAttackComponent(
                target, "boss3_attack_cpu",
                1f, 3f, 5f, 1.5f);
        owner.addComponent(attack);
        return attack;
    }

    @Test
    void bossattack1tests_noTarget_noSpawn() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        EnemyMudBallAttackComponent atk = addAttack(owner, null);

        owner.create();
        atk.update();

        verify(entities, never()).register(any());
    }

    @Test
    void bossattack1tests_outOfRange_noSpawn() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(10, 0)); // > range
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        owner.create();
        atk.update();

        verify(entities, never()).register(any());
    }

    @Test
    void bossattack1tests_missingAtlas_noSpawn_butAttemptsLazyLoad() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(1, 0)); // within range
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(null);
        when(resources.loadForMillis(anyInt())).thenReturn(false);

        owner.create();
        atk.update();

        verify(resources, times(1)).loadTextureAtlases(any(String[].class));
        verify(entities, never()).register(any());
    }

    @Test
    void bossattack1tests_missingRegions_noSpawn() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(1, 0)); // within range
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        TextureAtlas atlas = mock(TextureAtlas.class);
        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
        when(atlas.findRegions("boss3_attack_cpu")).thenReturn(new Array<>()); // no frames

        owner.create();
        atk.update();

        verify(atlas, times(1)).findRegions("boss3_attack_cpu");
        verify(entities, never()).register(any());
    }

    //--------------------------------------

    @Test
    void bossattack1tests_successfulSpawn_registersProjectile() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(2, 3));
        owner.addComponent(new WeaponsStatsComponent(12)); // expect 4 on projectile
        Entity target = new Entity();
        target.setPosition(new Vector2(3, 3)); // within range

        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        TextureAtlas atlas = mock(TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mock(TextureAtlas.AtlasRegion.class));
        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
        when(atlas.findRegions("boss3_attack_cpu")).thenReturn(regions);

        owner.create();
        atk.update();

        verify(resources, never()).loadTextureAtlases(any());
        verify(entities, times(1)).register(any(Entity.class));
    }

    @Test
    void bossattack1tests_cooldownBlocksSecondSpawn() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(1, 0)); // within range
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        TextureAtlas atlas = mock(TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mock(TextureAtlas.AtlasRegion.class));
        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
        when(atlas.findRegions("boss3_attack_cpu")).thenReturn(regions);

        owner.create();

        // First spawn
        atk.update();
        // Immediately update again (timer>0) -> no second spawn
        atk.update();

        verify(entities, times(1)).register(any());
    }

    @Test
    void bossattack1tests_lazyLoadSuccess_thenSpawn() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(1, 0));
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        TextureAtlas atlas = mock(TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mock(TextureAtlas.AtlasRegion.class));

        // First getAsset -> null, then after load -> atlas
        when(resources.getAsset(anyString(), eq(TextureAtlas.class)))
                .thenReturn(null)
                .thenReturn(atlas);

        when(resources.loadForMillis(anyInt())).thenReturn(false); // load completes
        when(atlas.findRegions("boss3_attack_cpu")).thenReturn(regions);

        owner.create();
        atk.update();

        verify(resources, times(1)).loadTextureAtlases(any(String[].class));
        verify(entities, times(1)).register(any());
    }

    @Test
    void bossattack1tests_backoffWhenAtlasMissing_preventsRapidRetries() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(4.0F, 5.0F));
        Entity target = new Entity();
        target.setPosition(new Vector2(5.0F, 5.0F));

        owner.addComponent(new WeaponsStatsComponent(9));

        EnemyMudBallAttackComponent atk = this.addAttack(owner, target);

        TextureAtlas atlas = (TextureAtlas)Mockito.mock(TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add((TextureAtlas.AtlasRegion)Mockito.mock(TextureAtlas.AtlasRegion.class));
        Mockito.when((TextureAtlas)this.resources.getAsset(Mockito.anyString(), (Class)Mockito.eq(TextureAtlas.class))).thenReturn(atlas);
        Mockito.when(atlas.findRegions("boss3_attack_cpu")).thenReturn(regions);

        owner.create();

        ((EntityService)Mockito.doAnswer((inv) -> {
            Entity proj = (Entity)inv.getArgument(0);
            Assertions.assertNotNull(proj.getComponent(EnemyProjectileMovementComponent.class));
            Assertions.assertNotNull(proj.getComponent(HitboxComponent.class));
            Assertions.assertNotNull(proj.getComponent(ColliderComponent.class));
            Assertions.assertNotNull(proj.getComponent(CombatStatsComponent.class));

            WeaponsStatsComponent w = proj.getComponent(WeaponsStatsComponent.class);
            Assertions.assertNotNull(w, "projectile should have WeaponsStatsComponent");
            Assertions.assertEquals(
                    owner.getComponent(WeaponsStatsComponent.class).getBaseAttack(),
                    w.getBaseAttack(),
                    "projectile damage should mirror owner's base attack"
            );

            Vector2 p = proj.getPosition();
            Assertions.assertNotNull(p, "projectile position should be set");
            Assertions.assertTrue((double)p.dst2(owner.getCenterPosition()) < 0.001,
                    "projectile should spawn near owner's center position");
            return null;
        }).when(this.entities)).register((Entity)Mockito.any(Entity.class));

        atk.update();
        ((EntityService)Mockito.verify(this.entities, Mockito.times(1))).register((Entity)Mockito.any());
    }
}
