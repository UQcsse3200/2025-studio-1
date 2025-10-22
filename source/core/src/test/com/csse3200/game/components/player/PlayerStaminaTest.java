package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.events.EventHandler;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests stamina drain/regen logic, double-jump cost, and the infinite stamina cheat.
 * Drive via StaminaComponent.update() using a controllable fake GameTime.
 */
@ExtendWith(GameExtension.class)
class PlayerStaminaTest {

    private TestTime time;

    @BeforeEach
    void setup() {
        ServiceLocator.registerPhysicsService(new PhysicsService());
        time = new TestTime();
        ServiceLocator.registerTimeSource(time);
    }

    @Test
    void sprintingDrainsStamina() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        StaminaComponent stamina = attachStamina(actions);

        injectTime(stamina, time);

        stamina.setSprinting(true);
        stamina.setMoving(true);
        stamina.setDashing(false);
        stamina.setGrounded(true);

        setField(stamina, "stamina", 100f);

        float tick = 0.1f;
        float drainPerSec = 30f;

        time.advance(tick);
        stamina.update();

        float staminaAfter = reflectFloat(stamina, "stamina");
        float expected = Math.max(0f, 100f - drainPerSec * tick);
        assertEquals(expected, staminaAfter, 1e-4);
    }

    @Test
    void notMovingDoesNotDrain() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        StaminaComponent stamina = attachStamina(actions);

        injectTime(stamina, time);

        stamina.setSprinting(true);
        stamina.setMoving(false); // stationary
        stamina.setDashing(false);
        stamina.setGrounded(true);
        setField(stamina, "stamina", 40f);

        // Make regen NOT trigger yet by pretending we just spent (in game time)
        setField(stamina, "lastStaminaSpendMs", time.getTime());

        time.advance(0.1f);
        stamina.update();

        float staminaAfter = reflectFloat(stamina, "stamina");
        assertEquals(40f, staminaAfter, 1e-4);
    }

    @Test
    void regeneratesAfterDelay() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        StaminaComponent stamina = attachStamina(actions);

        injectTime(stamina, time);

        stamina.setSprinting(false);
        stamina.setMoving(false);
        stamina.setDashing(false);
        stamina.setGrounded(true);
        setField(stamina, "stamina", 60f);

        long delayMs = 800L; // REGEN_DELAY_MS default
        // Pretend we last spent long enough ago (in game time)
        setField(stamina, "lastStaminaSpendMs", time.getTime() - delayMs - 1);

        float tick = 0.1f;        // TICK_SEC
        float regenPerSec = 10f;  // default
        int max = 100;            // default

        time.advance(tick);
        stamina.update();

        float staminaAfter = reflectFloat(stamina, "stamina");
        float expected = Math.min(max, 60f + regenPerSec * tick);
        assertEquals(expected, staminaAfter, 1e-4);
    }

    @Test
    void trySpendStaminaFailsWhenInsufficientAndSucceedsWhenEnough() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        StaminaComponent stamina = attachStamina(actions);

        injectTime(stamina, time);

        setField(stamina, "stamina", 5f);
        assertFalse(stamina.trySpend(10)); // not enough -> false
        assertEquals(5f, reflectFloat(stamina, "stamina"), 1e-4);

        setField(stamina, "stamina", 20f);
        assertTrue(stamina.trySpend(10)); // enough -> true and reduced
        assertEquals(10f, reflectFloat(stamina, "stamina"), 1e-4);
    }

    @Test
    void infiniteStaminaCheatLocksStaminaAtMax() {
        PlayerActions actions = new PlayerActions();
        attachEntity(actions);
        StaminaComponent stamina = attachStamina(actions);
        injectTime(stamina, time);

        int max = 100;
        // Drop stamina, then enable cheat
        setField(stamina, "stamina", 1f);
        actions.infStamina();

        // Even if we try to "spend", it should succeed and not reduce
        assertTrue(stamina.trySpend(9999));
        assertEquals(max, (int) reflectFloat(stamina, "stamina"));

        // A time-driven update pegs it at MAX too
        time.advance(0.1f);
        stamina.update();
        assertEquals(max, (int) reflectFloat(stamina, "stamina"));
    }

    // ---------- helpers ----------

    /** Minimal, controllable GameTime for tests. */
    private static class TestTime extends GameTime {
        private long tMillis = 0L;
        private float dt = 0f;
        private boolean paused = false;

        public void advance(float seconds) {
            if (paused) {
                dt = 0f;
            } else {
                dt = seconds;
                tMillis += (long) (seconds * 1000f);
            }
        }

        @Override public void setPaused(boolean paused) { this.paused = paused; }
        @Override public boolean isPaused() { return paused; }
        @Override public float getDeltaTime() { return paused ? 0f : dt; }
        @Override public long getTime() { return tMillis; }
        @Override public float getRawDeltaTime() { return dt; } // not used here
    }

    private static void injectTime(StaminaComponent stamina, GameTime time) {
        setField(stamina, "time", time);
    }

    private static StaminaComponent attachStamina(PlayerActions actions) {
        StaminaComponent stamina = new StaminaComponent();

        // Give the stamina component a mock entity + events so it can emit safely
        Entity staminaEnt = mock(Entity.class);
        EventHandler ev = mock(EventHandler.class);
        when(staminaEnt.getEvents()).thenReturn(ev);
        setField(stamina, "entity", staminaEnt);

        // Inject into PlayerActions so its methods (jump/dash/etc.) can use it
        setField(actions, "stamina", stamina);
        return stamina;
    }

    private static void attachEntity(PlayerActions actions) {
        Entity ent = mock(Entity.class);
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

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}