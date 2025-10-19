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

import static org.mockito.Mockito.*;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.Graphics;

import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.csse3200.game.physics.PhysicsService;

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
        owner.setPosition(new Vector2(0, 0));
        Entity target = new Entity();
        target.setPosition(new Vector2(1, 0));
        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(null);
        when(resources.loadForMillis(anyInt())).thenReturn(false);

        owner.create();

        // First update -> attempt load, set backoff
        atk.update();
        verify(resources, times(1)).loadTextureAtlases(any());

        // While backoff active, updating again should not try to load/register again
        when(time.getDeltaTime()).thenReturn(0.1f); // less than 0.25f backoff
        atk.update();

        verify(resources, times(1)).loadTextureAtlases(any());
        verify(entities, never()).register(any());
    }

    @Test
    void bossattack1tests_projectileWiring_basicComponentsPresent() {
        Entity owner = new Entity();
        owner.setPosition(new Vector2(4, 5));
        Entity target = new Entity();
        target.setPosition(new Vector2(5, 5));
        owner.addComponent(new WeaponsStatsComponent(9)); // projectile should carry 3

        EnemyMudBallAttackComponent atk = addAttack(owner, target);

        TextureAtlas atlas = mock(TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        regions.add(mock(TextureAtlas.AtlasRegion.class));
        when(resources.getAsset(anyString(), eq(TextureAtlas.class))).thenReturn(atlas);
        when(atlas.findRegions("boss3_attack_cpu")).thenReturn(regions);

        owner.create();

        // Capture the entity registered
        doAnswer(inv -> {
            Entity proj = inv.getArgument(0);
            assertNotNull(proj.getComponent(EnemyProjectileMovementComponent.class));
            assertNotNull(proj.getComponent(com.csse3200.game.physics.components.HitboxComponent.class));
            assertNotNull(proj.getComponent(com.csse3200.game.physics.components.ColliderComponent.class));
            assertNotNull(proj.getComponent(com.csse3200.game.components.CombatStatsComponent.class));

            WeaponsStatsComponent w = proj.getComponent(WeaponsStatsComponent.class);
            assertNotNull(w, "projectile should have WeaponsStatsComponent");
            assertEquals(3, w.getBaseAttack(), "projectile damage should be base/3 from owner");

            Vector2 p = proj.getPosition();
            assertNotNull(p, "projectile position should be set");
            assertTrue(p.dst2(owner.getCenterPosition()) < 1e-3,
                    "projectile should spawn near owner's center position");
            return null;
        }).when(entities).register(any(Entity.class));

        atk.update();

        verify(entities, times(1)).register(any());
    }
}
