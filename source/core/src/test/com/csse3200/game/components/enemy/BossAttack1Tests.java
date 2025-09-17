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
        // Minimal Gdx stub so postRunnable
        Application app = mock(Application.class);
        doAnswer(inv -> {
            ((Runnable) inv.getArgument(0)).run();
            return null;
        })
                .when(app).postRunnable(any(Runnable.class));
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

        atk.update();

        verify(atlas, times(1)).findRegions("boss3_attack_cpu");
        verify(entities, never()).register(any());
    }
}
