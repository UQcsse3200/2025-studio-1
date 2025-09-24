package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class BossFuryTaskTest {

    @BeforeEach
    void setupServices() {
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);

        GameTime gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(20f / 1000f);
        ServiceLocator.registerTimeSource(gameTime);

        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void triggersOnce_IncreasesAttack_AndScalesUp() {
        Entity boss = makeBossEntity(
                1000,
                5,
                1.0f,
                1.3f,
                3,
                0.45f
        );

        WeaponsStatsComponent w = boss.getComponent(WeaponsStatsComponent.class);
        assertEquals(5, w.getBaseAttack());
        assertEquals(1f, boss.getScale().x, 1e-3);
        assertEquals(1f, boss.getScale().y, 1e-3);

        boss.update();
        assertEquals(8, w.getBaseAttack());
        assertEquals(1.5f, boss.getScale().x, 1e-3);
        assertEquals(1.5f, boss.getScale().y, 1e-3);

        boss.update();
        assertEquals(8, w.getBaseAttack());
        assertEquals(1.5f, boss.getScale().x, 1e-3);
        assertEquals(1.5f, boss.getScale().y, 1e-3);
    }

    @Test
    void doesNotTrigger_WhenAboveThreshold() {
        Entity boss = makeBossEntity(
                1000,
                5,
                0.0f,
                1.3f,
                3,
                0.45f
        );

        WeaponsStatsComponent w = boss.getComponent(WeaponsStatsComponent.class);
        boss.update();
        assertEquals(5, w.getBaseAttack());
        assertEquals(1f, boss.getScale().x, 1e-3);
        assertEquals(1f, boss.getScale().y, 1e-3);
    }

    private Entity makeBossEntity(int hp,
                                  int baseAttack,
                                  float thresholdRatio,
                                  float speedMultiplier,
                                  int damageBonus,
                                  Float newCooldown) {
        Entity boss = new Entity();
        boss.setScale(1f, 1f);

        boss.addComponent(new PhysicsComponent());
        boss.addComponent(new ColliderComponent());
        boss.addComponent(new PhysicsMovementComponent(new Vector2(2.5f, 2.5f)));
        boss.addComponent(new CombatStatsComponent(hp));
        boss.addComponent(new WeaponsStatsComponent(baseAttack));
        boss.addComponent(new BossFuryTask(thresholdRatio, speedMultiplier, damageBonus, newCooldown));

        boss.create();
        return boss;
    }
}
