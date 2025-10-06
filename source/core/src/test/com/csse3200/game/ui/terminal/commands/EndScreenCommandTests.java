package com.csse3200.game.ui.terminal.commands;

import com.badlogic.gdx.Screen;
import com.csse3200.game.GdxGame;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.screens.DeathScreen;
import com.csse3200.game.screens.WinScreen;
import com.csse3200.game.services.CountdownTimerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
class EndScreenCommandTests {

    private static boolean run(Command cmd, String... args) {
        return cmd.action(new ArrayList<>(List.of(args)));
    }

    @Test
    void switchesScreenAndReturnsTrue() {
        GdxGame game = mock(GdxGame.class);
        CountdownTimerService timer = mock(CountdownTimerService.class);
        when(timer.getDuration()).thenReturn(60_000L);
        when(timer.getRemainingMs()).thenReturn(15_000L);

        WinScreen win = mock(WinScreen.class);
        when(game.getScreen()).thenReturn(win);

        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN, timer);

        boolean ok = run(cmd);
        assertTrue(ok);

        verify(game).setScreen(GdxGame.ScreenType.WIN_SCREEN);
        verify(game).getScreen();
        verify(win).updateTime(45L);

        verify(timer, atLeastOnce()).getDuration();
        verify(timer, atLeastOnce()).getRemainingMs();
        verifyNoMoreInteractions(game);
    }

    @Test
    void switchesToDeathAndUpdatesTime() {
        GdxGame game = mock(GdxGame.class);
        CountdownTimerService timer = mock(CountdownTimerService.class);
        // 90s total, 10s remaining => 80s elapsed
        when(timer.getDuration()).thenReturn(90_000L);
        when(timer.getRemainingMs()).thenReturn(10_000L);

        DeathScreen death = mock(DeathScreen.class);
        when(game.getScreen()).thenReturn(death);

        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.DEATH_SCREEN, timer);

        boolean ok = run(cmd);
        assertTrue(ok);

        verify(game).setScreen(GdxGame.ScreenType.DEATH_SCREEN);
        verify(game).getScreen();
        verify(death).updateTime(80L);

        verify(timer, atLeastOnce()).getDuration();
        verify(timer, atLeastOnce()).getRemainingMs();
        verifyNoMoreInteractions(game);
    }

    @Test
    void winScreenHandlesNullCurrentScreenGracefully() {
        GdxGame game = mock(GdxGame.class);
        CountdownTimerService timer = mock(CountdownTimerService.class);
        when(timer.getDuration()).thenReturn(30_000L);
        when(timer.getRemainingMs()).thenReturn(5_000L);

        // Router hasn’t populated current screen yet
        when(game.getScreen()).thenReturn(null);

        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN, timer);

        boolean ok = run(cmd);
        assertTrue(ok);

        verify(game).setScreen(GdxGame.ScreenType.WIN_SCREEN);
        verify(game).getScreen(); // called but null → no updateTime call
        verify(timer, atLeastOnce()).getDuration();
        verify(timer, atLeastOnce()).getRemainingMs();
        verifyNoMoreInteractions(game);
    }

    @Test
    void winScreenOtherScreenType_NoUpdateCalled() {
        GdxGame game = mock(GdxGame.class);
        CountdownTimerService timer = mock(CountdownTimerService.class);
        when(timer.getDuration()).thenReturn(10_000L);
        when(timer.getRemainingMs()).thenReturn(2_000L);

        // Some other Screen implementation (not WinScreen/DeathScreen)
        Screen other = mock(Screen.class);
        when(game.getScreen()).thenReturn(other);

        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN, timer);

        boolean ok = run(cmd);
        assertTrue(ok);

        verify(game).setScreen(GdxGame.ScreenType.WIN_SCREEN);
        verify(game).getScreen();
        // No methods on this generic Screen should be called by EndScreenCommand
        verifyNoInteractions(other);

        verify(timer, atLeastOnce()).getDuration();
        verify(timer, atLeastOnce()).getRemainingMs();
        verifyNoMoreInteractions(game);
    }

    @Test
    void elapsedSecondsCanBeNegative_PassedThroughAsIs() {
        GdxGame game = mock(GdxGame.class);
        CountdownTimerService timer = mock(CountdownTimerService.class);
        // Remaining > duration → negative elapsed
        when(timer.getDuration()).thenReturn(5_000L);
        when(timer.getRemainingMs()).thenReturn(8_000L); // elapsed = -3s

        WinScreen win = mock(WinScreen.class);
        when(game.getScreen()).thenReturn(win);

        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN, timer);

        boolean ok = run(cmd);
        assertTrue(ok);

        verify(game).setScreen(GdxGame.ScreenType.WIN_SCREEN);
        verify(game).getScreen();
        verify(win).updateTime(-3L); // current implementation passes negative through

        verify(timer, atLeastOnce()).getDuration();
        verify(timer, atLeastOnce()).getRemainingMs();
        verifyNoMoreInteractions(game);
    }
}
