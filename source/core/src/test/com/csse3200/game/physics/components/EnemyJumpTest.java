package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EnemyJumpTest {
    @Test
    public void testEnemyJumpAppliesImpulse() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2());

        Entity enemy = new Entity();
        enemy.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        enemy.addComponent(movement);
        movement.create();

        float jumpForce = 5.0f;
        movement.jump(jumpForce);

        // Verify that applyLinearImpulse was called with the correct force
        verify(body).applyLinearImpulse(new Vector2(0, jumpForce), body.getWorldCenter(), true);
    }

    @Test
    public void testJumpNoPhysicsComponentDoesNothing() {
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        Entity enemy = new Entity();
        enemy.addComponent(movement);
        movement.create(); // physicsComponent will be null
        movement.jump(10f); // Should not throw
        // Nothing to verify (no body), just ensure no exception
    }

    @Test
    public void testJumpNullBodyDoesNothing() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        when(physicsComponent.getBody()).thenReturn(null);
        Entity enemy = new Entity();
        enemy.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        enemy.addComponent(movement);
        movement.create();
        movement.jump(3f); // Should safely ignore
        verify(physicsComponent, times(1)).getBody();
    }

    @Test
    public void testJumpAfterDisposeIgnored() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2());
        Entity e = new Entity();
        e.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        e.addComponent(movement);
        movement.create();
        movement.dispose();
        movement.jump(7f);
        verify(body, never()).applyLinearImpulse(any(Vector2.class), any(), anyBoolean());
    }

    @Test
    public void testSetMovingFalseAppliesCounterImpulse() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2());
        when(body.getLinearVelocity()).thenReturn(new Vector2(3f, 4f));
        when(body.getMass()).thenReturn(2f);

        Entity e = new Entity();
        e.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        e.addComponent(movement);
        movement.create();

        movement.setMoving(false); // Should apply impulse to counter velocity
        // Impulse = (0 - (3,4)) * 2 = (-6,-8)
        verify(body).applyLinearImpulse(new Vector2(-6f, -8f), body.getWorldCenter(), true);
    }

    @Test
    public void testUpdateMovesTowardTargetApplyingImpulse() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2());
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        when(body.getMass()).thenReturn(1f);

        Entity e = new Entity();
        e.setPosition(0f, 0f);
        e.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        e.addComponent(movement);
        movement.create();

        movement.setTarget(new Vector2(10f, 0f)); // Direction (1,0)
        movement.update();
        verify(body).applyLinearImpulse(new Vector2(1f, 0f), body.getWorldCenter(), true);
    }

    @Test
    public void testUpdateSkipsWhenBodyNull() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        when(physicsComponent.getBody()).thenReturn(null);
        Entity e = new Entity();
        e.setPosition(0f, 0f);
        e.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        e.addComponent(movement);
        movement.create();
        movement.setTarget(new Vector2(5f, 0f));
        assertDoesNotThrow(movement::update);
        verify(physicsComponent, atLeastOnce()).getBody();
    }

    @Test
    public void testDisposePreventsFurtherMovement() {
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));
        when(body.getWorldCenter()).thenReturn(new Vector2());
        when(body.getMass()).thenReturn(1f);

        Entity e = new Entity();
        e.setPosition(0f,0f);
        e.addComponent(physicsComponent);
        PhysicsMovementComponent movement = new PhysicsMovementComponent();
        e.addComponent(movement);
        movement.create();

        movement.setTarget(new Vector2(2f,0f));
        movement.dispose();
        movement.update(); // Should not apply anything
        verify(body, never()).applyLinearImpulse(any(Vector2.class), any(), anyBoolean());
    }
}
