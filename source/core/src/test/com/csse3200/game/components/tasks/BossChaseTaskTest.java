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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(GameExtension.class)
class BossChaseTaskTest {

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
    void shouldNotStartWhenTargetIsAbove() {
        Entity me = makePhysicsEntity();
        me.create();
        me.setPosition(0f, 0f);

        Entity target = new Entity();
        target.setPosition(0f, 2f);

        BossChaseTask task = new BossChaseTask(target, 10, 5f, 10f);
        task.create(() -> me);

        assertTrue(task.getPriority() < 0, "Target above: should not start chasing");
    }

    @Test
    void shouldStopWhenTargetBecomesAboveWhileActive() {
        Entity me = makePhysicsEntity();
        me.create();
        me.setPosition(0f, 0f);

        Entity target = new Entity();
        target.setPosition(0f, 0.2f);

        BossChaseTask task = new BossChaseTask(target, 10, 5f, 10f);
        task.create(() -> me);

        task.start();
        assertEquals(10, task.getPriority(), "Active & not above: should keep priority");

        target.setPosition(0f, 2.5f);
        assertTrue(task.getPriority() < 0, "Becomes above: should drop priority (stop)");
    }

    @Test
    void shouldMoveTowardsTargetWhenBelowAndInView() {
        Entity target = new Entity();
        target.setPosition(0f, 0f);

        AITaskComponent ai = new AITaskComponent()
                .addTask(new BossChaseTask(target, 10, 5f, 10f));

        Entity me = makePhysicsEntity().addComponent(ai);
        me.create();
        me.setPosition(0f, 2f);

        float d0 = me.getPosition().dst(target.getPosition());

        for (int i = 0; i < 3; i++) {
            me.earlyUpdate();
            me.update();
            ServiceLocator.getPhysicsService().getPhysics().update();
        }

        float d1 = me.getPosition().dst(target.getPosition());
        assertTrue(d1 < d0, "Should move closer when target is below and in view");
    }

    private Entity makePhysicsEntity() {
        return new Entity()
                .addComponent(new PhysicsComponent())
                .addComponent(new PhysicsMovementComponent(new Vector2(2.5f, 2.5f)));
    }
}



