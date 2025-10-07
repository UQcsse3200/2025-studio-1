package com.csse3200.game.components.screens;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;

import com.badlogic.gdx.graphics.GL20;
import com.csse3200.game.GdxGame;
import com.csse3200.game.records.RoundData;
import com.csse3200.game.services.ButtonSoundService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.session.LeaderBoardManager;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.util.List;

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

        display = new LeaderboardScreenDisplay(mockGame);
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
}
