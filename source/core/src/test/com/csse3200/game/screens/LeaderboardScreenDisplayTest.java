package com.csse3200.game.screens;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.LeaderboardScreenDisplay;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;
import com.csse3200.game.utils.TimefromSeconds;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class LeaderboardScreenDisplayTest {

    /** Simple subclass to expose protected buildUI() for testing */
    static class TestableLeaderboardScreenDisplay extends LeaderboardScreenDisplay {
        TestableLeaderboardScreenDisplay(GdxGame game) { super(game); }
        void build(Table root) { super.buildUI(root); }
    }

    private GdxGame game;
    private LeaderBoardManager lbm;
    private TestableLeaderboardScreenDisplay screen;


    // Helper: invoke a private method (onStart/onExit)
    private static void invokePrivate(Object target, Class<?> owner, String methodName) {
        try {
            var m = owner.getDeclaredMethod(methodName);
            m.setAccessible(true);
            m.invoke(target);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setup() {
        // Mock the game and LBM
        game = mock(GdxGame.class);
        lbm = mock(LeaderBoardManager.class);
        ServiceLocator.registerLeaderBoardManager(lbm);

        // Button sound service
        var buttonSound = mock(com.csse3200.game.services.ButtonSoundService.class);
        ServiceLocator.registerButtonSoundService(buttonSound);

        screen = new TestableLeaderboardScreenDisplay(game);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void highestScoreReturnsZeroWhenLbmNull() throws Exception {
        ServiceLocator.registerLeaderBoardManager(null); // simulate null LBM
        var method = LeaderboardScreenDisplay.class.getDeclaredMethod("getHighestScore");
        method.setAccessible(true);
        int result = (int) method.invoke(screen);
        assertEquals(0, result);
    }

    @Test
    void highestScoreReturnsZeroWhenEmpty() throws Exception {
        when(lbm.getLeaderBoard()).thenReturn(List.of());
        var method = LeaderboardScreenDisplay.class.getDeclaredMethod("getHighestScore");
        method.setAccessible(true);
        int result = (int) method.invoke(screen);
        assertEquals(0, result);
    }

    @Test
    void highestScoreReturnsMax() throws Exception {
        var rounds = new ArrayList<RoundData>();
        rounds.add(new RoundData(5 /*currency*/, 8f /*time*/));
        rounds.add(new RoundData(7, 1f));
        rounds.add(new RoundData(2, 20f));
        when(lbm.getLeaderBoard()).thenReturn(rounds);

        var method = LeaderboardScreenDisplay.class.getDeclaredMethod("getHighestScore");
        method.setAccessible(true);
        int result = (int) method.invoke(screen);
        assertEquals(22, result);
    }


    @Test
    void buildUIShowsNoRoundsMessageWhenEmpty() {
        when(lbm.getLeaderBoard()).thenReturn(List.of());

        Table root = new Table();
        screen.build(root);

        boolean foundMessage = findLabelText(root, "No rounds recorded yet.");
        assertTrue(foundMessage, "Should render the 'No rounds recorded yet.' message");
        // Also check the high score line is present with 0
        assertTrue(findLabelText(root, "Current Highest Score: 0"));
    }


    @Test
    void buildUIRendersRowsAndValues() {
        // Prepare two rounds
        var rounds = List.of(
                new RoundData(10, 7f),
                new RoundData(3, 65f)
        );
        when(lbm.getLeaderBoard()).thenReturn(rounds);

        Table root = new Table();
        screen.build(root);

        // Highest score is 68
        assertTrue(findLabelText(root, "Current Highest Score: 68"));

        // Verify headers
        assertTrue(findLabelText(root, "Round"));
        assertTrue(findLabelText(root, "Currency"));
        assertTrue(findLabelText(root, "Time Bonus"));
        assertTrue(findLabelText(root, "Score"));

        // Verify row 1: index 1, currency 10, time 00:07, score 17
        assertTrue(findLabelText(root, "1"));
        assertTrue(findLabelText(root, "10"));
        assertTrue(findLabelText(root, TimefromSeconds.toMMSS(7)));   // 00:07
        assertTrue(findLabelText(root, "17"));

        // Verify row 2: index 2, currency 3, time 01:05, score 68
        assertTrue(findLabelText(root, "2"));
        assertTrue(findLabelText(root, "3"));
        assertTrue(findLabelText(root, TimefromSeconds.toMMSS(65)));  // 01:05
        assertTrue(findLabelText(root, "68"));
    }


    @Test
    void onStartNavigatesToMainGameAndPlaysSound() {
        // Call private onStart()
        invokePrivate(screen, LeaderboardScreenDisplay.class, "onStart");
        verify(ServiceLocator.getButtonSoundService(), times(1)).playClick();
        verify(game, times(1)).setScreen(GdxGame.ScreenType.MAIN_GAME);
        verifyNoMoreInteractions(game);
    }

    @Test
    void onExitExitsGameAndPlaysSound() {
        invokePrivate(screen, LeaderboardScreenDisplay.class, "onExit");
        verify(ServiceLocator.getButtonSoundService(), times(1)).playClick();
        verify(game, times(1)).exit();
        verifyNoMoreInteractions(game);
    }

    private static boolean findLabelText(Actor actor, String text) {
        if (actor instanceof Label l) {
            if (text.equals(l.getText().toString())) return true;
        }
        if (actor instanceof Table t) {
            for (Actor child : t.getChildren()) {
                if (findLabelText(child, text)) return true;
            }
        } else if (actor instanceof WidgetGroup wg) {
            for (Actor child : wg.getChildren()) {
                if (findLabelText(child, text)) return true;
            }
        }
        return false;
    }
}
