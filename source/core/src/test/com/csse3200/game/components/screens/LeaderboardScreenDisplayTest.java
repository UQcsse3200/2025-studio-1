package com.csse3200.game.components.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.csse3200.game.GdxGame;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;
import com.csse3200.game.utils.TimefromSeconds;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LeaderboardScreenDisplayTest {

    private HeadlessApplication app;
    private GdxGame mockGame;
    private LeaderBoardManager mockLbm;
    private ButtonSoundService mockButtonSound;
    private MockedStatic<ServiceLocator> serviceLocatorStatic;
    private LeaderboardScreenDisplay display;

    @BeforeAll
    void bootHeadless() {
        HeadlessApplicationConfiguration cfg = new HeadlessApplicationConfiguration();
        app = new HeadlessApplication(new ApplicationAdapter() {}, cfg);

        Gdx.gl20 = org.mockito.Mockito.mock(GL20.class);
        Gdx.gl = Gdx.gl20;
    }

    @AfterAll
    void shutdownHeadless() {
        if (app != null) {
            app.exit();
            app = null;
        }
    }

    @BeforeEach
    void setUp() {
        mockGame = mock(GdxGame.class);
        mockLbm = mock(LeaderBoardManager.class);
        mockButtonSound = mock(ButtonSoundService.class);

        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        serviceLocatorStatic.when(ServiceLocator::getLeaderBoardManager).thenReturn(mockLbm);
        serviceLocatorStatic.when(ServiceLocator::getButtonSoundService).thenReturn(mockButtonSound);

        display = spy(new LeaderboardScreenDisplay(mockGame));

        if (UIComponent.getSkin().getDrawable("white") == null) {
            UIComponent.getSkin().add("white", new BaseDrawable());
        }

        // provided by ChatGPT
        doAnswer(inv -> {
            String label = inv.getArgument(0, String.class);
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = new BitmapFont();
            return new TextButton(label, style);
        }).when(display).button(anyString(), anyFloat(), any());

    }

    @AfterEach
    void tearDown() {
        if (serviceLocatorStatic != null) {
            serviceLocatorStatic.close();
        }
    }

    // ---------- getHighestScore() ----------
    @Test
    void getHighestScore_returnsZero_whenLeaderBoardManagerNotRegistered() throws Exception {
        serviceLocatorStatic.close();
        serviceLocatorStatic = mockStatic(ServiceLocator.class);
        serviceLocatorStatic.when(ServiceLocator::getLeaderBoardManager).thenReturn(null);

        int highest = invokeGetHighestScore(display);
        assertEquals(0, highest);
    }

    @Test
    void getHighestScore_returnsZero_whenLeaderBoardEmpty() throws Exception {
        when(mockLbm.getLeaderBoard()).thenReturn(List.of());

        int highest = invokeGetHighestScore(display);
        assertEquals(0, highest);
    }

    @Test
    void getHighestScore_returnsMax_whenLeaderboardHasScores() throws Exception {
        RoundData r1 = mock(RoundData.class);
        RoundData r2 = mock(RoundData.class);
        RoundData r3 = mock(RoundData.class);
        when(r1.getScore()).thenReturn(150);
        when(r2.getScore()).thenReturn(900);
        when(r3.getScore()).thenReturn(450);
        when(mockLbm.getLeaderBoard()).thenReturn(List.of(r1, r2, r3));

        int highest = invokeGetHighestScore(display);
        assertEquals(900, highest);
    }

    @Test
    void buildUI_showsNoRounds_whenEmpty() {
        when(mockLbm.getLeaderBoard()).thenReturn(List.of());

        Table root = new Table();
        // buildUI is protected; test is in same package so we can call it directly
        display.buildUI(root);

        assertTrue(findLabelText(root, "No rounds recorded yet."));
        assertTrue(findLabelText(root, "Current Highest Score: 0"));
        // headers present too
        assertTrue(findLabelText(root, "Round"));
        assertTrue(findLabelText(root, "Currency"));
        assertTrue(findLabelText(root, "Time Bonus"));
        assertTrue(findLabelText(root, "Score"));
    }

    @Test
    void buildUI_rendersRows_headers_and_values() {
        var rounds = List.of(
                new RoundData(10, 7f),   // score 17, time 00:07
                new RoundData(3, 65f)    // score 68, time 01:05 (highest)
        );
        when(mockLbm.getLeaderBoard()).thenReturn(rounds);

        Table root = new Table();
        display.buildUI(root);

        // headers
        assertTrue(findLabelText(root, "Round"));
        assertTrue(findLabelText(root, "Currency"));
        assertTrue(findLabelText(root, "Time Bonus"));
        assertTrue(findLabelText(root, "Score"));

        // highest score label
        assertTrue(findLabelText(root, "Current Highest Score: 68"));

        // row 1
        assertTrue(findLabelText(root, "1"));
        assertTrue(findLabelText(root, "10"));
        assertTrue(findLabelText(root, TimefromSeconds.toMMSS(7)));   // 00:07
        assertTrue(findLabelText(root, "17"));

        // row 2
        assertTrue(findLabelText(root, "2"));
        assertTrue(findLabelText(root, "3"));
        assertTrue(findLabelText(root, TimefromSeconds.toMMSS(65)));  // 01:05
        assertTrue(findLabelText(root, "68"));
    }

    // ---------- onStart() / onExit() ----------
    @Test
    void onStart_playsClick_andNavigatesToMainGame() throws Exception {
        invokePrivate(display, "onStart");

        verify(mockButtonSound, times(1)).playClick();
        verify(mockGame, times(1)).setScreen(GdxGame.ScreenType.MAIN_GAME);
        verifyNoMoreInteractions(mockGame);
    }

    @Test
    void onExit_playsClick_andExitsGame() throws Exception {
        invokePrivate(display, "onExit");

        verify(mockButtonSound, times(1)).playClick();
        verify(mockGame, times(1)).exit();
        verifyNoMoreInteractions(mockGame);
    }

    // ---------- Reflection helpers ----------
    private static int invokeGetHighestScore(LeaderboardScreenDisplay display) throws Exception {
        Method m = LeaderboardScreenDisplay.class.getDeclaredMethod("getHighestScore");
        m.setAccessible(true);
        return (int) m.invoke(display);
    }

    private static void invokePrivate(Object target, String methodName, Class<?>... paramTypes) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, paramTypes);
        m.setAccessible(true);
        m.invoke(target);
    }

    // label finder
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
