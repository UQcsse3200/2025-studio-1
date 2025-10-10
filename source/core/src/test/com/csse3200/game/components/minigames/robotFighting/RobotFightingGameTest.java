package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RobotFightingGame without relying on FileLoader.
 */
class RobotFightingGameTest {

    /** Minimal subclass that skips FileLoader usage. */
    static class TestGame extends RobotFightingGame {
        TestDisplay injectedDisplay;

        TestGame(RobotFightingText text) {
            super(text);
        }

        @Override
        protected Entity initGameEntity() {
            Entity fakeEntity = new Entity();
            injectedDisplay = new TestDisplay();
            fakeEntity.addComponent(injectedDisplay);
            return fakeEntity;
        }

        TestDisplay getInjectedDisplay() {
            return injectedDisplay;
        }
    }


    /** Minimal stub display that avoids LibGDX UI dependencies. */
    static class TestDisplay extends RobotFightingDisplay {
        boolean shown = false;
        boolean hidden = false;
        int fightOverCalls = 0;
        String lastResult = null;
        String lastEncouragement = null;

        @Override public void show() { shown = true; }
        @Override public void hide() { hidden = true; }
        @Override public void fightOver(String status) {
            fightOverCalls++;
            lastResult = status;
        }
        @Override public void encourageFighter(String msg) {
            lastEncouragement = msg;
        }

    }

    private TestGame game;
    private Entity entity;
    private TestDisplay testDisplay;

    @BeforeEach
    void setUp() throws Exception {
        ServiceLocator.clear();

        RobotFightingText fakeText = new RobotFightingText();
        Field field = RobotFightingText.class.getDeclaredField("encouragingMessages");
        field.setAccessible(true);
        field.set(fakeText, java.util.List.of("test encouragement"));

        game = new TestGame(fakeText);
        entity = game.getGameEntity();
        testDisplay = game.getInjectedDisplay();

        Field displayField = RobotFightingGame.class.getDeclaredField("gameDisplay");
        displayField.setAccessible(true);
        displayField.set(game, testDisplay);
    }

    @AfterEach
    void tearDown() {
        ServiceLocator.clear();
        Timer.instance().clear();
    }

    // Helper reflection accessors (same as before)
    private double getEncourageMult() throws Exception {
        Field f = RobotFightingGame.class.getDeclaredField("encourageMult");
        f.setAccessible(true);
        return f.getDouble(game);
    }

    private void setEncourageMult(double v) throws Exception {
        Field f = RobotFightingGame.class.getDeclaredField("encourageMult");
        f.setAccessible(true);
        f.setDouble(game, v);
    }

    private void callMethod(String name, Class<?>... params) throws Exception {
        Method m = RobotFightingGame.class.getDeclaredMethod(name, params);
        m.setAccessible(true);
        m.invoke(game);
    }

    @Test
    @DisplayName("Encouragement increases multiplier but caps at 2")
    void testEncourageIncreases() throws Exception {
        setEncourageMult(1.0);
        entity.getEvents().trigger("robotFighting:encourage");

        assertNotNull(testDisplay.lastEncouragement);
        double after = getEncourageMult();
        assertTrue(after >= 1.0 && after <= 2.0);
    }

    @Test
    @DisplayName("Lose courage lowers multiplier but not below 1")
    void testLoseCourage() throws Exception {
        setEncourageMult(1.2);
        callMethod("loseCourage");
        assertEquals(1.15, getEncourageMult(), 0.001);
        setEncourageMult(1.0);
        callMethod("loseCourage");
        assertEquals(1.0, getEncourageMult(), 0.001);
    }

    @Test
    @DisplayName("determineWinner calls fightOver with correct status")
    void testDetermineWinner() throws Exception {
        Field cHp = RobotFightingGame.class.getDeclaredField("chosenFighterHp");
        Field oHp = RobotFightingGame.class.getDeclaredField("otherFighterHp");
        cHp.setAccessible(true);
        oHp.setAccessible(true);

        cHp.setInt(game, 50);
        oHp.setInt(game, 0);
        callMethod("determineWinner");
        assertEquals("won", testDisplay.lastResult);

        cHp.setInt(game, 0);
        callMethod("determineWinner");
        assertEquals("drew", testDisplay.lastResult);
    }

    @Test
    @DisplayName("startFight resets HP and schedules timers")
    void testStartFightSchedules() throws Exception {
        Field cHp = RobotFightingGame.class.getDeclaredField("chosenFighterHp");
        Field oHp = RobotFightingGame.class.getDeclaredField("otherFighterHp");
        cHp.setAccessible(true);
        oHp.setAccessible(true);
        cHp.setInt(game, 10);
        oHp.setInt(game, 5);

        callMethod("startFight");
        assertEquals(100, cHp.getInt(game));
        assertEquals(100, oHp.getInt(game));
    }
}
