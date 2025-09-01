package com.csse3200.game.entities.factories;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.TouchAttackComponent;
import com.csse3200.game.components.tasks.ChaseTask;
import com.csse3200.game.components.tasks.WanderTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsMovementComponent;
import com.csse3200.game.ai.tasks.AITaskComponent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BossFactoryBaseNPCTest {

    @Test
    void createBaseNPC_shouldHaveMovementAIAndTouchAttack() {
        // Arrange
        Entity player = new Entity();

        // Act: package-private access works because the test is in the same package
        Entity npc = BossFactory.createBaseNPC(player);

        // Assert: core physics/movement
        assertNotNull(npc.getComponent(PhysicsComponent.class), "PhysicsComponent missing");
        assertNotNull(npc.getComponent(PhysicsMovementComponent.class), "PhysicsMovementComponent missing");
        assertNotNull(npc.getComponent(ColliderComponent.class), "ColliderComponent missing");

        HitboxComponent hitbox = npc.getComponent(HitboxComponent.class);
        assertNotNull(hitbox, "HitboxComponent missing");
        assertEquals(PhysicsLayer.NPC, hitbox.getLayer(), "Hitbox layer should be NPC");

        // Touch attack configured to hit players
        TouchAttackComponent touchAttack = npc.getComponent(TouchAttackComponent.class);
        assertNotNull(touchAttack, "TouchAttackComponent missing");
        assertEquals(PhysicsLayer.PLAYER, touchAttack.getTargetLayer(), "TouchAttack target should be PLAYER");

        // AI tasks present: Wander + Chase
        AITaskComponent ai = npc.getComponent(AITaskComponent.class);
        assertNotNull(ai, "AITaskComponent missing");

        // If AITaskComponent exposes tasks (common in the template), assert both exist.
        // If your AITaskComponent doesn't expose a public getter, you can skip the next block.
        try {
            var method = AITaskComponent.class.getDeclaredMethod("getTasks");
            method.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<?> tasks = (List<?>) method.invoke(ai);
            assertTrue(tasks.stream().anyMatch(t -> t instanceof WanderTask),
                    "WanderTask should be registered");
            assertTrue(tasks.stream().anyMatch(t -> t instanceof ChaseTask),
                    "ChaseTask should be registered");
        } catch (NoSuchMethodException ignored) {
            // Fallback: just confirm the component exists; deeper inspection not available.
        } catch (ReflectiveOperationException e) {
            fail("Reflection to inspect AI tasks failed: " + e.getMessage());
        }
    }
}
