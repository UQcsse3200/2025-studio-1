package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Headless logic test for RobotFightingGame, covering combat flow, encouragement,
 * and win/draw/lose determination.
 */
class RobotFightingGameTest {
    private static DummyDisplay display;
    private HeadlessGame game;

    // ------------------------------------------------------------------------
    // Dummy stubs (no LibGDX deps)
    // ------------------------------------------------------------------------

    static class DummyText extends RobotFightingText {
        DummyText() { this.encouragingMessages = new ArrayList<>(); }
        void addMessage() { encouragingMessages.add("Go Clanker!"); }
    }

    static class DummyDisplay extends RobotFightingDisplay {
        boolean hideCalled, showCalled, encouraged, explosionCalled;
        @Override public void hide() { hideCalled = true; }
        @Override public void show() { showCalled = true; }
        @Override public void encourageFighter(String msg) { encouraged = true; }
        @Override public void playExplosionEffect(Actor actor) { explosionCalled = true; }
        @Override public Actor getChosenActor() { return new Actor(); }
        @Override public Actor getOtherActor(Actor actor) { return new Actor(); }
        @Override public void playAttackAnimation(Actor actor) {}
    }

    static class HeadlessGame extends RobotFightingGame {
        HeadlessGame(RobotFightingText text, DummyDisplay display) {
            super(text);
            Entity entity = getGameEntity();
            // Wire dummy events
            entity.getEvents().addListener("interact", display::hide);
            entity.getEvents().addListener("betPlaced", display::show);
        }
    }

    // ------------------------------------------------------------------------
    // Gdx setup/teardown
    // ------------------------------------------------------------------------

    @BeforeAll
    static void initGdxEnvironment() {
        if (Gdx.app == null) {
            HeadlessApplicationConfiguration cfg = new HeadlessApplicationConfiguration();
            new HeadlessApplication(new ApplicationListener() {
                public void create() {}
                public void resize(int w, int h) {}
                public void render() {}
                public void pause() {}
                public void resume() {}
                public void dispose() {}
            }, cfg);
            Gdx.files = new HeadlessFiles();
            Gdx.gl = mock(GL20.class);        // fixes Texture/Skin initialization
            Gdx.gl20 = Gdx.gl;
        }
    }

    @AfterAll
    static void tearDownGdx() {
        if (Gdx.app != null) {
            Gdx.app.exit();
            Gdx.app = null;
        }
    }

    @BeforeEach
    void setUp() {
        DummyText text = new DummyText();
        text.addMessage();
        display = new DummyDisplay();
        game = new HeadlessGame(text, display);
        setPrivate(game, "gameDisplay", display);
        Timer.instance().clear();
    }

    // ------------------------------------------------------------------------
    // Reflection helpers
    // ------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    private static <T> T getPrivate(Object target, String field, Class<T> type) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(field);
                f.setAccessible(true);
                return (T) f.get(target);
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass(); // climb up to RobotFightingGame
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Field not found: " + field);
    }

    private static void setPrivate(Object target, String field, Object val) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Field f = c.getDeclaredField(field);
                f.setAccessible(true);
                f.set(target, val);
                return;
            } catch (NoSuchFieldException e) {
                c = c.getSuperclass();
            } catch (IllegalAccessException e) {
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Field not found: " + field);
    }

    private static Object invokePrivate(Object target, String methodName, Class<?>... params) {
        Class<?> c = target.getClass();
        while (c != null) {
            try {
                Method m = c.getDeclaredMethod(methodName, params);
                m.setAccessible(true);
                return m.invoke(target);
            } catch (NoSuchMethodException e) {
                c = c.getSuperclass(); // climb up until RobotFightingGame
            } catch (ReflectiveOperationException e) {
                throw new AssertionError(e);
            }
        }
        throw new AssertionError("Method not found: " + methodName);
    }


    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    void testEncourageTriggersDisplay() {
        game.encourageFighter();
        assertTrue(display.encouraged, "Display should react to encourage");
    }

    @Test
    void testEncouragementIncreasesAndClamps() {
        for (int i = 0; i < 200; i++) game.encourageFighter();
        double mult = getPrivate(game, "encourageMult", Double.class);
        assertTrue(mult >= 1.0 && mult <= 1.5, "encourageMult should stay within [1,1.5]");
    }

    @Test
    void testLoseCourageReduces() {
        setPrivate(game, "encourageMult", 1.3d);
        invokePrivate(game, "loseCourage");
        double after = getPrivate(game, "encourageMult", Double.class);
        assertTrue(after < 1.3 && after >= 1.0);
    }

    @Test
    void testLoseCourageClampsToOne() {
        setPrivate(game, "encourageMult", 1.02d);
        invokePrivate(game, "loseCourage");
        double after = getPrivate(game, "encourageMult", Double.class);
        assertEquals(1.0, after, 1e-9);
    }

    @Test
    void testDetermineWinnerImmediateWin() {
        setPrivate(game, "chosenFighterHp", 10);
        setPrivate(game, "otherFighterHp", 0);
        final boolean[] win = {false};
        game.getGameEntity().getEvents().addListener("win", () -> win[0] = true);
        invokePrivate(game, "determineWinnerImmediate");
        assertTrue(win[0]);
    }

    @Test
    void testDetermineWinnerImmediateLose() {
        setPrivate(game, "chosenFighterHp", 0);
        setPrivate(game, "otherFighterHp", 10);
        final boolean[] lose = {false};
        game.getGameEntity().getEvents().addListener("lose", () -> lose[0] = true);
        invokePrivate(game, "determineWinnerImmediate");
        assertTrue(lose[0]);
    }

    @Test
    void testDetermineWinnerImmediateDraw() {
        setPrivate(game, "chosenFighterHp", 0);
        setPrivate(game, "otherFighterHp", 0);
        final boolean[] draw = {false};
        game.getGameEntity().getEvents().addListener("draw", () -> draw[0] = true);
        invokePrivate(game, "determineWinnerImmediate");
        assertTrue(draw[0]);
    }

    @Test
    void testDetermineWinnerAlwaysTriggersInteract() {
        final boolean[] interacted = {false};
        game.getGameEntity().getEvents().addListener("interact", () -> interacted[0] = true);
        invokePrivate(game, "determineWinnerImmediate");
        assertTrue(interacted[0]);
    }

    @Test
    void testHandleChosenFighterDecrementsHpAndReturnsTrue() {
        setPrivate(game, "chosenFighterHp", 100);
        setPrivate(game, "otherFighterHp", 100);
        setPrivate(game, "encourageMult", 1.0d);
        // inject dummy display
        setPrivate(game, "gameDisplay", display);
        boolean result = (boolean) invokePrivate(game, "handleChosenFighter");
        assertTrue(result, "handleChosenFighter should continue when HP>0");
        int hp = getPrivate(game, "otherFighterHp", Integer.class);
        assertTrue(hp < 100, "Opponent HP should drop");
    }

    @Test
    void testHandleOtherFighterDecrementsHpAndReturnsTrue() {
        setPrivate(game, "chosenFighterHp", 100);
        setPrivate(game, "otherFighterHp", 100);
        setPrivate(game, "gameDisplay", display);
        boolean result = (boolean) invokePrivate(game, "handleOtherFighter");
        assertTrue(result, "handleOtherFighter should continue when HP>0");
        int hp = getPrivate(game, "chosenFighterHp", Integer.class);
        assertTrue(hp < 100, "Player HP should drop");
    }

    @Test
    void testHandleChosenFighterEndsWhenZeroHp() {
        setPrivate(game, "chosenFighterHp", 0);
        setPrivate(game, "otherFighterHp", 10);
        setPrivate(game, "gameDisplay", display);
        boolean result = (boolean) invokePrivate(game, "handleChosenFighter");
        assertFalse(result, "Should stop when HP <= 0");
    }

    @Test
    void testHandleOtherFighterEndsWhenZeroHp() {
        setPrivate(game, "chosenFighterHp", 10);
        setPrivate(game, "otherFighterHp", 0);
        setPrivate(game, "gameDisplay", display);
        boolean result = (boolean) invokePrivate(game, "handleOtherFighter");
        assertFalse(result, "Should stop when HP <= 0");
    }

    @Test
    void testStartGameSetsDisplayedTrue() {
        setPrivate(game, "gameDisplayed", false);
        invokePrivate(game, "startGame");
        boolean shown = getPrivate(game, "gameDisplayed", Boolean.class);
        assertTrue(shown);
    }

    @Test
    void testHandleInteractSetsDisplayedFalse() {
        setPrivate(game, "gameDisplayed", true);
        setPrivate(game, "gameDisplay", display);
        invokePrivate(game, "handleInteract");
        boolean shown = getPrivate(game, "gameDisplayed", Boolean.class);
        assertFalse(shown);
    }
}
