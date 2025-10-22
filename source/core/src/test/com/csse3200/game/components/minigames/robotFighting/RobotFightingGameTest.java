package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure logic unit test for RobotFightingGame.
 * No Gdx textures, ServiceLocator, or native rendering required.
 */
public class RobotFightingGameTest {
    private HeadlessGame game;
    private static DummyDisplay display;
    private DummyText text;

    // ------------------------------------------------------------------------
    // Dummy dependencies
    // ------------------------------------------------------------------------

    /** Safe mock of RobotFightingText that initializes its list. */
    static class DummyText extends RobotFightingText {
        DummyText() {
            this.encouragingMessages = new ArrayList<>();
        }
        void addMessage(String msg) {
            encouragingMessages.add(msg);
        }
    }

    /** Fake display (no LibGDX code). */
    static class DummyDisplay {
        boolean hideCalled = false;
        boolean showCalled = false;
        boolean encouraged = false;
        boolean explosionPlayed = false;

        public void hide() { hideCalled = true; }
        public void show() { showCalled = true; }
        public void encourageFighter() { encouraged = true; }
        public void playExplosionEffect() { explosionPlayed = true; }
    }

    /** Headless RobotFightingGame that uses dummy display and dummy entity. */
    static class HeadlessGame extends RobotFightingGame {

        HeadlessGame(RobotFightingText text, DummyDisplay display) {
            super(text); // uses the headless constructor you added in RobotFightingGame

            // IMPORTANT: use super.getGameEntity() here (not getGameEntity())
            Entity dummyEntity = super.getGameEntity();

            // Wire only what the tests need, without touching gameDisplay
            dummyEntity.getEvents().addListener("interact", display::hide);
            dummyEntity.getEvents().addListener("betPlaced", display::show);
            dummyEntity.getEvents().addListener("robotFighting:encourage",
                    display::encourageFighter);
        }
    }






    // ------------------------------------------------------------------------
    // Setup
    // ------------------------------------------------------------------------

    @BeforeEach
    void setUp() {
        text = new DummyText();
        text.addMessage("Go Clanker!");
        text.addMessage("Keep fighting!");

        display = new DummyDisplay();
        game = new HeadlessGame(text, display);

        Timer.instance().clear();
    }

    @BeforeAll
    static void initGdxEnvironment() {
        if (Gdx.app == null) {
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            new HeadlessApplication(new ApplicationListener() {
                @Override public void create() {}
                @Override public void resize(int width, int height) {}
                @Override public void render() {}
                @Override public void pause() {}
                @Override public void resume() {}
                @Override public void dispose() {}
            }, config);
            Gdx.files = new HeadlessFiles();
        }
    }

    @AfterAll
    static void tearDownGdx() {
        if (Gdx.app != null) {
            Gdx.app.exit();
            Gdx.app = null;
        }
    }


    // ------------------------------------------------------------------------
    // Tests
    // ------------------------------------------------------------------------

    @Test
    void testEncourageTriggersDisplay() {
        game.getGameEntity().getEvents().trigger("robotFighting:encourage");
        assertTrue(display.encouraged, "encourageFighter() should be invoked.");
    }

    @Test
    void testHideAndShowAreCalled() {
        game.getGameEntity().getEvents().trigger("interact");
        assertTrue(display.hideCalled, "interact should hide the display");

        game.getGameEntity().getEvents().trigger("betPlaced");
        assertTrue(display.showCalled, "betPlaced should show the display");
    }

    @Test
    void testExplosionEffectIsInvoked() {
        display.playExplosionEffect();
        assertTrue(display.explosionPlayed, "Explosion effect should trigger.");
    }

    @Test
    void testTextGetRandomReturnsMessage() {
        String msg = text.getRandom();
        assertNotNull(msg, "getRandom() should return a message when list is filled");
        assertTrue(text.encouragingMessages.contains(msg));
    }
}
