package com.csse3200.game.ui.terminal.commands;

import com.csse3200.game.GdxGame;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class EndScreenCommandTests {

    private static boolean run(Command cmd, String... args) {
        return cmd.action(new ArrayList<>(List.of(args)));
    }

    @Test
    void switchesScreenAndReturnsTrue() {
        GdxGame game = mock(GdxGame.class);
        EndScreenCommand cmd = new EndScreenCommand(game, GdxGame.ScreenType.WIN_SCREEN);

        boolean ok = run(cmd);
        assertTrue(ok, "action should return true");

        ArgumentCaptor<GdxGame.ScreenType> cap = ArgumentCaptor.forClass(GdxGame.ScreenType.class);
        verify(game, times(1)).setScreen(cap.capture());
        assertEquals(GdxGame.ScreenType.WIN_SCREEN, cap.getValue());
        verifyNoMoreInteractions(game);
    }
}
