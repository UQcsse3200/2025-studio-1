package com.csse3200.game.components.fireball;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.FireballMovementComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

/**
 * Tests for FireballMovementComponent.
 *
 * Covers:
 *  - Moves by velocity * deltaTime for a single update
 *  - No movement when deltaTime == 0
 *  - Accumulated movement across multiple updates
 *  - Constructor copies input velocity (external mutation does not affect)
 */
public class FireballMovementComponentTest {

    /** Helper: attach the component to a mocked Entity without requiring the full ECS. */
    private static void attachToEntity(Object component, Entity entity) {
        try {
            // Prefer calling setEntity(Entity) if it exists on the superclass
            Method m = component.getClass().getSuperclass().getDeclaredMethod("setEntity", Entity.class);
            m.setAccessible(true);
            m.invoke(component, entity);
        } catch (Exception ignored) {
            try {
                // Fallback: write to the protected 'entity' field directly
                Field f = component.getClass().getSuperclass().getDeclaredField("entity");
                f.setAccessible(true);
                f.set(component, entity);
            } catch (Exception e) {
                throw new RuntimeException("Failed to attach component to entity.", e);
            }
        }
    }

    @Test
    void movesByVelocityTimesDt_once() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            // dt = 0.5
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0.5f);

            // Start (1,2), velocity (4,-6) -> delta (2,-3) -> expected (3,-1)
            Entity host = mock(Entity.class);
            when(host.getPosition()).thenReturn(new Vector2(1f, 2f));

            FireballMovementComponent comp =
                    new FireballMovementComponent(new Vector2(4f, -6f));
            attachToEntity(comp, host);

            assertDoesNotThrow(comp::update);

            verify(host).setPosition(
                    argThat(v -> approx(v.x, 3f) && approx(v.y, -1f))
            );
        }
    }

    @Test
    void noMove_whenDtIsZero() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(0f);

            Entity host = mock(Entity.class);
            when(host.getPosition()).thenReturn(new Vector2(10f, 10f));

            FireballMovementComponent comp =
                    new FireballMovementComponent(new Vector2(100f, 100f));
            attachToEntity(comp, host);

            comp.update();

            // Should remain at (10,10)
            verify(host).setPosition(
                    argThat(v -> approx(v.x, 10f) && approx(v.y, 10f))
            );
        }
    }

    @Test
    void accumulatesOverMultipleUpdates() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            // Three frames: 0.1, 0.2, 0.3  -> total dt = 0.6
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime())
                    .thenReturn(0.1f, 0.2f, 0.3f);

            Entity host = mock(Entity.class);

            // Keep a mutable "real" position state
            final Vector2 state = new Vector2(0f, 0f);

            // Each getPosition() returns a copy of the current state (avoid external mutation)
            when(host.getPosition()).thenAnswer(inv -> new Vector2(state));

            // On setPosition(Vector2), update the state
            doAnswer(inv -> {
                Vector2 v = inv.getArgument(0);
                state.set(v); // update real position
                return null;
            }).when(host).setPosition(any(Vector2.class));

            FireballMovementComponent comp =
                    new FireballMovementComponent(new Vector2(10f, 0f));
            // Attach component to host
            attachToEntity(comp, host);

            // Three consecutive frames:
            comp.update(); // 0.1 * 10 = +1  -> (1,0)
            comp.update(); // 0.2 * 10 = +2  -> (3,0)
            comp.update(); // 0.3 * 10 = +3  -> (6,0)

            // Final position should be (6,0)
            org.junit.jupiter.api.Assertions.assertTrue(approx(state.x, 6f) && approx(state.y, 0f),
                    "Expected final position to be (6,0) but was " + state);

            // If you want to strictly validate the call sequence, uncomment below:
            /*
            ArgumentCaptor<Vector2> cap = ArgumentCaptor.forClass(Vector2.class);
            verify(host, times(3)).setPosition(cap.capture());
            List<Vector2> calls = cap.getAllValues();
            // 1st call -> (1,0)
            assertTrue(approx(calls.get(0).x, 1f) && approx(calls.get(0).y, 0f));
            // 2nd call -> (3,0)
            assertTrue(approx(calls.get(1).x, 3f) && approx(calls.get(1).y, 0f));
            // 3rd call -> (6,0)
            assertTrue(approx(calls.get(2).x, 6f) && approx(calls.get(2).y, 0f));
            */
        }
    }

    @Test
    void constructorCopiesVelocity_inputMutationDoesNotAffect() {
        try (MockedStatic<ServiceLocator> sl =
                     mockStatic(ServiceLocator.class, RETURNS_DEEP_STUBS)) {
            sl.when(() -> ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(1f);

            Vector2 src = new Vector2(5f, 5f);
            FireballMovementComponent comp = new FireballMovementComponent(src);
            // Mutate the original vector; the component should not be affected
            src.set(-100f, -100f);

            Entity host = mock(Entity.class);
            when(host.getPosition()).thenReturn(new Vector2(0f, 0f));
            attachToEntity(comp, host);

            comp.update(); // Expected delta still (5,5)

            verify(host).setPosition(argThat(v -> approx(v.x, 5f) && approx(v.y, 5f)));
        }
    }

    private static boolean approx(float a, float b) {
        return Math.abs(a - b) < 1e-3f;
    }
}
