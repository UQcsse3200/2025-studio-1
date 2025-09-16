package com.csse3200.game.physics.components;

import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.badlogic.gdx.physics.box2d.Body;

public class EnemyJumpTest {
    @Test
    public void testEnemyJumpAppliesImpulse() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);

        Entity enemy = new Entity();
        enemy.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        enemy.addComponent(movement);
        movement.create();

        float jumpForce = 5.0f;
        movement.jump(jumpForce);

        // Verify that applyLinearImpulse was called with the correct force
        verify(body).applyLinearImpulse(new com.badlogic.gdx.math.Vector2(0, jumpForce), body.getWorldCenter(), true);
    }
}

