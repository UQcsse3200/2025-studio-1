package com.csse3200.game.components.minigames.whackamole;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WhackAMoleGameTest {

    static class TestDisplay extends WhackAMoleDisplay {
        int score = 0;
        boolean running = false;
        boolean hideCalled = false;

        int endCalls = 0;
        String lastEndTitle = null;
        String lastEndMessage = null;

        @Override public void resetScore() { score = 0; }
        @Override public void setScore(int value) { score = Math.max(0, value); }
        @Override public int getScore() { return score; }
        @Override public void setRunning(boolean r) { running = r; }
        @Override public void hideAllMoles() { /* no-op */ }
        @Override public void showMoleAt(int idx) { /* no-op */ }
        @Override public void hideMoleAt(int idx) { /* no-op */ }
        @Override public void prepareToPlay() { score = 0; running = false; }
        @Override public void show() { /* no-op */ }
        @Override public void hide() { hideCalled = true; }
        @Override public void showEnd(String title, String message) {
            endCalls++;
            lastEndTitle = title;
            lastEndMessage = message;
        }
    }

    private WhackAMoleGame game;
    private Entity gameEntity;
    private TestDisplay testDisplay;

    @BeforeEach
    void setUp() {
        ServiceLocator.clear();

        PhysicsEngine mockEngine = mock(PhysicsEngine.class);
        when(mockEngine.createBody(any())).thenReturn(mock(Body.class));
        ServiceLocator.registerPhysicsService(new PhysicsService(mockEngine));

        ResourceService rs = mock(ResourceService.class);
        when(rs.getAsset(anyString(), eq(Texture.class))).thenReturn(mock(Texture.class));
        when(rs.getAsset(anyString(), eq(Sound.class))).thenReturn(mock(Sound.class));
        ServiceLocator.registerResourceService(rs);

        ServiceLocator.registerTimeSource(new GameTime());

        game = new WhackAMoleGame();
        gameEntity = game.getGameEntity();

        testDisplay = new TestDisplay();
        swapDisplay(game, testDisplay);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
    }

    // Helpers

    private boolean isRunning() {
        try {
            Field f = WhackAMoleGame.class.getDeclaredField("running");
            f.setAccessible(true);
            return f.getBoolean(game);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void callHandleMiss() {
        try {
            Method m = WhackAMoleGame.class.getDeclaredMethod("handleMiss");
            m.setAccessible(true);
            m.invoke(game);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void swapDisplay(WhackAMoleGame g, WhackAMoleDisplay replacement) {
        try {
            Field f = WhackAMoleGame.class.getDeclaredField("display");
            f.setAccessible(true);
            f.set(g, replacement);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject test display", e);
        }
    }

    // Tests

    @Test
    @DisplayName("Interact toggles UI: first opens+prepares, second hides and stops")
    void interactTogglesUI() {
        // First interact -> open & prepare
        gameEntity.getEvents().trigger("interact");
        assertEquals(0, testDisplay.getScore(), "Score should reset on open");

        gameEntity.getEvents().trigger("wm:start");
        assertTrue(isRunning(), "Should be running after start");

        gameEntity.getEvents().trigger("interact");

        assertTrue(testDisplay.hideCalled, "Display.hide() should be called");
        assertFalse(isRunning(), "Game should be stopped after second interact");
    }

    @Test
    @DisplayName("Start then Stop: onStop cleans state; hits while stopped ignored")
    void startThenStop() {
        gameEntity.getEvents().trigger("wm:start");
        assertTrue(isRunning(), "Should be running after start");

        testDisplay.setScore(5);
        gameEntity.getEvents().trigger("wm:stop");
        assertFalse(isRunning(), "Should not be running after stop");

        int before = testDisplay.getScore();
        gameEntity.getEvents().trigger("wm:hit");
        assertEquals(before, testDisplay.getScore(), "Score unchanged when hit while stopped");
    }

    @Test
    @DisplayName("Hit during running but below target does not end the game")
    void hitBelowTargetKeepsRunning() {
        gameEntity.getEvents().trigger("wm:start");
        assertTrue(isRunning(), "Should be running");

        testDisplay.setScore(5);
        gameEntity.getEvents().trigger("wm:hit"); // below 20
        assertTrue(isRunning(), "Still running after below-target hit");
        assertEquals(5, testDisplay.getScore(), "Score remains as set; win is checked only at hit time");
    }

    @Test
    @DisplayName("Win: score >= 20 then hit -> stops and resets with 'You Win!'")
    void winAtTwenty() {
        gameEntity.getEvents().trigger("wm:start");
        assertTrue(isRunning(), "Should be running");

        testDisplay.setScore(20);
        gameEntity.getEvents().trigger("wm:hit");

        assertFalse(isRunning(), "Stopped after win");
        assertEquals(0, testDisplay.getScore(), "Score reset after win");
        assertEquals("You Win!", testDisplay.lastEndTitle, "Win dialog title");
        assertTrue(testDisplay.endCalls > 0, "showEnd should be called");
    }

    @Test
    @DisplayName("Lose: two misses -> stops and resets with 'You Lose'")
    void loseAfterTwoMisses() {
        gameEntity.getEvents().trigger("wm:start");
        assertTrue(isRunning(), "Should be running");

        callHandleMiss();
        assertTrue(isRunning(), "Still running after first miss");
        callHandleMiss();

        assertFalse(isRunning(), "Stopped after second miss");
        assertEquals(0, testDisplay.getScore(), "Score reset after lose");
        assertEquals("You Lose", testDisplay.lastEndTitle, "Lose dialog title");
        assertTrue(testDisplay.endCalls > 0, "showEnd should be called");
    }
}