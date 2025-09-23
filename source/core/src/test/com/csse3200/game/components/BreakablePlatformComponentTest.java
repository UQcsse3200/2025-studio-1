package com.csse3200.game.components;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BreakablePlatformComponent, with Timer statics mocked so no real scheduling occurs.
 */
@ExtendWith(GameExtension.class)
class BreakablePlatformComponentTest {

    private Entity platform;
    private TextureRenderComponent render;
    private Body platformBody;

    private Entity player;
    private Body playerBody;

    private Fixture platformFixture;
    private Fixture playerFixture;

    MockedStatic<Timer> timerMock;

    private static Application fakeApp;

    @BeforeAll
    static void stubGdxApp() {
        // Gdx.app is a public static; safe to set in tests
        fakeApp = org.mockito.Mockito.mock(Application.class);

        // If your code ever calls postRunnable, make it run inline:
        org.mockito.Mockito.doAnswer(inv -> {
            Runnable r = inv.getArgument(0);
            r.run();
            return null;
        }).when(fakeApp).postRunnable(org.mockito.ArgumentMatchers.any());

        Gdx.app = fakeApp;
    }

    @AfterAll
    static void clearGdxApp() {
        Gdx.app = null;
        fakeApp = null;
    }

    @BeforeEach
    void setUp() {
        // ---- Platform entity setup
        platform = spy(new Entity());
        render = mock(TextureRenderComponent.class);
        PhysicsComponent physics = mock(PhysicsComponent.class);
        platformBody = mock(Body.class);

        when(physics.getBody()).thenReturn(platformBody);
        when(platformBody.getPosition()).thenReturn(new Vector2(0f, 0f));

        // Stub priorities so Entity.create() sort won't NPE
        when(render.getPrio()).thenReturn(ComponentPriority.LOW);
        when(physics.getPrio()).thenReturn(ComponentPriority.LOW);

        BreakablePlatformComponent breakable = spy(new BreakablePlatformComponent());
        doReturn(ComponentPriority.LOW).when(breakable).getPrio();

        platform.addComponent(render);
        platform.addComponent(physics);
        platform.addComponent(breakable);
        platform.create();

        // ---- Player entity setup
        player = spy(new Entity());
        InventoryComponent inv = spy(new InventoryComponent(10));
        doReturn(ComponentPriority.LOW).when(inv).getPrio();

        player.addComponent(inv);
        player.create();
        player.setPosition(new Vector2(0f, 1.0f)); // definitely above platform

        playerBody = mock(Body.class);

        // BodyUserData -> points to player entity (what the component reads)
        BodyUserData bud = new BodyUserData();
        bud.entity = player;
        when(playerBody.getUserData()).thenReturn(bud);

        // ---- Fixtures for the collision callback
        platformFixture = mock(Fixture.class);
        when(platformFixture.getBody()).thenReturn(platformBody);

        playerFixture = mock(Fixture.class);
        when(playerFixture.getBody()).thenReturn(playerBody);

        AtomicBoolean removed = new AtomicBoolean(false);
        doAnswer(invocation -> {
            removed.set(true);
            // call real method if needed; since platform is a spy, forward to real:
            return invocation.callRealMethod();
        }).when(platform).setToRemove();

        // ---- Mock static Timer.schedule(...) calls
        timerMock = mockStatic(Timer.class);

        // 1) Timer.schedule(Task t, float delay)
        timerMock.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                .thenAnswer(invocation -> {
                    Timer.Task task = invocation.getArgument(0);
                    // For single-shot delayed tasks (e.g., shakeDelay), just run immediately
                    task.run();
                    return null;
                });

        // 2) Timer.schedule(Task t, float delay, float interval)
        AtomicInteger phase = new AtomicInteger(0); // 0 = shake, 1 = fade
        timerMock.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(invocation -> {
                    Timer.Task t = invocation.getArgument(0);
                    float interval = invocation.getArgument(2); // ~0.05

                    int which = phase.getAndIncrement();
                    int ticks;
                    if (which == 0) {
                        // SHAKE: ~0.5s -> 10 ticks + small buffer
                        ticks = (int) Math.ceil(0.5f / Math.max(1e-6f, interval)) + 2; // ~12
                    } else {
                        // FADE: ~1.5s -> 30 ticks + small buffer
                        ticks = (int) Math.ceil(1.5f / Math.max(1e-6f, interval)) + 2; // ~32
                    }

                    for (int i = 0; i < ticks; i++) {
                        t.run();
                        // If fade() finished and removal fired, stop simulating further ticks
                        if (removed.get()) break;
                    }
                    return null;
                });
    }

    @AfterEach
    void tearDown() {
        if (timerMock != null) timerMock.close();
    }

    @Test
    void triggersShakeThenFadeAndRemovesEntity_whenPlayerCollidesFromAbove() {
        // Given platform at y=0 and player at y=1.0 with InventoryComponent (i.e., "is player")

        // When: fire the collision event (what Physics system would dispatch)
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // Then: physics is disabled during fade
        verify(platformBody, atLeastOnce()).setActive(false);

        // Then: alpha fades (we only assert that it's *attempted* to change)
        verify(render, atLeastOnce()).setAlpha(anyFloat());

        // Then: entity removal is requested at the end
        verify(platform, times(1)).setToRemove();
    }

    @Test
    void doesNotRetriggerOnSecondCollision() {
        // First collision triggers the sequence
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // Second collision should be ignored because 'triggered' becomes true
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // We can assert by checking how many times the first delayed schedule (shakeDelay) was invoked.
        // With our static mock, each call to Timer.schedule(Task, delay) immediately runs the task,
        // so it’s enough to verify it was only called once.
        timerMock.verify(() -> Timer.schedule(any(Timer.Task.class), anyFloat()), times(1));

        // Still ends with single removal
        verify(platform, times(1)).setToRemove();
    }

    @Test
    void ignoresCollision_ifOtherIsNotPlayerOrNotAbove() {
        // Case A: not above (player below)
        player.setPosition(new Vector2(0f, -1f));
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // Case B: no InventoryComponent (not a player)
        Entity nonPlayer = new Entity();
        nonPlayer.create();
        BodyUserData otherBud = new BodyUserData();
        otherBud.entity = nonPlayer;
        when(playerBody.getUserData()).thenReturn(otherBud);
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // No schedules should have been made for shakeDelay
        timerMock.verifyNoInteractions(); // nothing should have been scheduled
        verify(platform, never()).setToRemove();
        verify(render, never()).setAlpha(anyFloat());
        verify(platformBody, never()).setActive(false);
    }

    @Test
    void recentersBodyAtEndOfShake_beforeFading() {
        // We don’t assert exact trajectories (sin/cos), just that transform is eventually reset.
        platform.getEvents().trigger("collisionStart", platformFixture, playerFixture);

        // At the end of shake(), the code resets to startPos via body.setTransform(startPos, 0)
        verify(platformBody, atLeastOnce()).setTransform(new Vector2(0f, 0f), 0f);
        // And entity position follows
        // (can't easily verify exact vector equality because new Vector2 instances; we check atLeastOnce call path)
        // If your Entity#setPosition is not spy-able, skip this assert.
    }
}
