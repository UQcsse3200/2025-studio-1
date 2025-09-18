package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.AITaskComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(GameExtension.class)
class DashAttackTaskTest {
    @BeforeEach
    void beforeEach() {
        // Mock rendering, physics, game time
        RenderService renderService = new RenderService();
        renderService.setDebug(mock(DebugRenderer.class));
        ServiceLocator.registerRenderService(renderService);
        GameTime gameTime = mock(GameTime.class);
        when(gameTime.getDeltaTime()).thenReturn(20 / 1000f);
        when(gameTime.getTime()).thenReturn(0L);
        ServiceLocator.registerTimeSource(gameTime);
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void shouldDashTowardsTarget() {
        Entity target = new Entity();
        target.setPosition(10f, 10f);

        AITaskComponent ai = new AITaskComponent()
                .addTask(new DashAttackTask(target, 10, new Vector2(2, 2), 1000, 1000));
        Entity entity = makePhysicsEntity().addComponent(ai);
        entity.create();
        entity.setPosition(0f, 0f);

        float initialDistance = entity.getPosition().dst(target.getPosition());
        // Run the game for a few cycles
        for (int i = 0; i < 200; i++) {
            when(ServiceLocator.getTimeSource().getTime()).thenReturn(i * 20L);
            entity.earlyUpdate();
            entity.update();
            ServiceLocator.getPhysicsService().getPhysics().update();
        }
        float newDistance = entity.getPosition().dst(target.getPosition());
        assertTrue(newDistance < initialDistance);
    }

    @Test
    void shouldNotDashDuringCooldown() {
        Entity target = new Entity();
        target.setPosition(10f, 10f);

        AITaskComponent ai = new AITaskComponent()
                .addTask(new DashAttackTask(target, 10, new Vector2(2, 2), 1000, 1000));
        Entity entity = makePhysicsEntity().addComponent(ai);
        entity.create();
        entity.setPosition(0f, 0f);

        float initialDistance = entity.getPosition().dst(target.getPosition());
        // Run the game for a few cycles
        for (int i = 0; i < 100; i++) {
            when(ServiceLocator.getTimeSource().getTime()).thenReturn(i * 20L);
            entity.earlyUpdate();
            entity.update();
            ServiceLocator.getPhysicsService().getPhysics().update();
        }
        float newDistance = entity.getPosition().dst(target.getPosition());
        assertTrue(newDistance >= initialDistance);
    }

    private Entity makePhysicsEntity() {
        return new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent());
    }
}
