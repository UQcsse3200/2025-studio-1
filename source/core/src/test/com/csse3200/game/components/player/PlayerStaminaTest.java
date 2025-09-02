package com.csse3200.game.components.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.services.ServiceLocator;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests stamina drain/regen logic, double-jump cost, and the infinite stamina cheat.
 * We avoid the Timer by calling the private staminaTick() via reflection.
 */
@ExtendWith(GameExtension.class)
class PlayerStaminaTest {

    @BeforeEach
    void setup() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
    }

    @Test
    void sprintingDrainsStamina() {
        PlayerActions actions = new PlayerActions();

        attachEntity(actions);
        PhysicsComponent physics = mock(PhysicsComponent.class);

        // If methods like getBody() are called, mock them too
        Body body = mock(Body.class);
        when(physics.getBody()).thenReturn(body);
        when(body.getLinearVelocity()).thenReturn(new Vector2(0f, 0f));

        // Inject the component into your player/actions object
        setField(actions, "physicsComponent", physics);

        // Arrange: pretend we are sprinting and moving right, not dashing
        setField(actions, "sprinting", true);
        setField(actions, "moving", true);
        setField(actions, "dashing", false);
        setField(actions, "walkDirection", new Vector2(1f, 0f));
        setField(actions, "stamina", 50f);

        float tick = reflectFloatConst(PlayerActions.class, "STAMINA_TICK_SEC", 0.1f);
        float drainPerSec = reflectFloatConst(PlayerActions.class, "SPRINT_DRAIN_PER_SEC", 15f);

        // Act
        callPrivate(actions, "staminaTick");

        // Assert: drained by (drain/sec * tick)
        float staminaAfter = reflectFloat(actions, "stamina");
        float expected = Math.max(0f, 50f - drainPerSec * tick);
        assertEquals(expected, staminaAfter, 1e-4);
    }

    @Test
    void notMovingDoesNotDrain() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);

        setField(actions, "sprinting", true);
        setField(actions, "moving", false); // stationary
        setField(actions, "dashing", false);
        setField(actions, "walkDirection", new Vector2(0f, 0f));
        setField(actions, "stamina", 40f);

        // Make regen NOT trigger yet by pretending we just spent
        long now = System.currentTimeMillis();
        setField(actions, "lastStaminaSpendMs", now);

        callPrivate(actions, "staminaTick");

        float staminaAfter = reflectFloat(actions, "stamina");
        assertEquals(40f, staminaAfter, 1e-4);
    }

    @Test
    void regeneratesAfterDelay() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);

        setField(actions, "sprinting", false);
        setField(actions, "moving", false);
        setField(actions, "dashing", false);
        setField(actions, "walkDirection", new Vector2(0f, 0f));
        setField(actions, "stamina", 60f);

        long delayMs = reflectLongConst(PlayerActions.class, "STAMINA_REGEN_DELAY_MS", 800L);
        // Pretend we last spent long enough ago
        setField(actions, "lastStaminaSpendMs", System.currentTimeMillis() - delayMs - 1);

        float tick = reflectFloatConst(PlayerActions.class, "STAMINA_TICK_SEC", 0.1f);
        float regenPerSec = reflectFloatConst(PlayerActions.class, "SPRINT_REGEN_PER_SEC", 10f);

        callPrivate(actions, "staminaTick");

        float staminaAfter = reflectFloat(actions, "stamina");
        float expected = Math.min(
                reflectIntConst(PlayerActions.class, "MAX_STAMINA", 100),
                60f + regenPerSec * tick
        );
        assertEquals(expected, staminaAfter, 1e-4);
    }

    @Test
    void trySpendStaminaFailsWhenInsufficient_andSucceedsWhenEnough() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);

        setField(actions, "stamina", 5f);
        assertFalse(callTrySpend(actions, 10)); // not enough -> false
        assertEquals(5f, reflectFloat(actions, "stamina"), 1e-4);

        setField(actions, "stamina", 20f);
        assertTrue(callTrySpend(actions, 10)); // enough -> true and reduced
        assertEquals(10f, reflectFloat(actions, "stamina"), 1e-4);
    }

    @Test
    void doubleJumpSpendsStamina_andBlocksWithoutIt() throws Exception {
        // Mock physics so jump can apply impulses safely
        PhysicsComponent physicsComponent = mock(PhysicsComponent.class);
        Body body = mock(Body.class);
        when(physicsComponent.getBody()).thenReturn(body);
        when(body.getWorldCenter()).thenReturn(new Vector2(0f, 0f));

        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        Field physField = PlayerActions.class.getDeclaredField("physicsComponent");
        physField.setAccessible(true);
        physField.set(actions, physicsComponent);

        // Configure as double-jump (not ground jump)
        setField(actions, "dashing", false);
        setField(actions, "jumpsLeft", 1); // air jump
        int doubleCost = reflectIntConst(PlayerActions.class, "DOUBLE_JUMP_COST", 10);

        // Case 1: Not enough stamina -> no impulse
        setField(actions, "stamina", (float) (doubleCost - 1));
        actions.jump();
        verify(body, never()).applyLinearImpulse(any(Vector2.class), any(Vector2.class), anyBoolean());

        // Case 2: Enough stamina -> impulse applied and stamina reduced by cost
        reset(body);
        setField(actions, "jumpsLeft", 1); // reset for another air jump
        setField(actions, "stamina", (float) (doubleCost + 5));
        actions.jump();

        verify(body, times(1)).applyLinearImpulse(any(Vector2.class), nullable(Vector2.class), eq(true));
        float staminaAfter = reflectFloat(actions, "stamina");
        assertEquals(doubleCost + 5 - doubleCost, staminaAfter, 1e-4);
    }

    @Test
    void infiniteStaminaCheatLocksStaminaAtMax() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);

        int max = reflectIntConst(PlayerActions.class, "MAX_STAMINA", 100);
        // Drop stamina, then enable cheat
        setField(actions, "stamina", 1f);
        actions.infStamina();

        // Even if we try to "spend", it should succeed and not reduce
        assertTrue(callTrySpend(actions, 9999));
        assertEquals(max, (int) reflectFloat(actions, "stamina"));

        // A tick pegs it at MAX too
        callPrivate(actions, "staminaTick");
        assertEquals(max, (int) reflectFloat(actions, "stamina"));
    }

    // Helpers
    private static void attachEntity(PlayerActions actions) {
        Entity ent = mock(Entity.class);
        //EventHandler events = new EventHandler();
        EventHandler events = mock(EventHandler.class);
        when(ent.getEvents()).thenReturn(events);
        setField(actions, "entity", ent);
    }

    private static Field findField(Class<?> clazz, String name) {
        Class<?> c = clazz;
        while (c != null) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
                c = c.getSuperclass();
            }
        }
        throw new RuntimeException(new NoSuchFieldException(name));
    }

    private static float reflectFloat(Object target, String name) {
        try {
            Field f = findField(target.getClass(), name);
            return f.getFloat(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static int reflectIntConst(Class<?> clazz, String name, int fallback) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.getInt(null);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static float reflectFloatConst(Class<?> clazz, String name, float fallback) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.getFloat(null);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static long reflectLongConst(Class<?> clazz, String name, long fallback) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f.getLong(null);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void callPrivate(Object target, String method) {
        try {
            Method m = target.getClass().getDeclaredMethod(method);
            m.setAccessible(true);
            m.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean callTrySpend(Object target, int amount) {
        try {
            Method m = target.getClass().getDeclaredMethod("trySpendStamina", int.class);
            m.setAccessible(true);
            Object out = m.invoke(target, amount);
            return (Boolean) out;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}