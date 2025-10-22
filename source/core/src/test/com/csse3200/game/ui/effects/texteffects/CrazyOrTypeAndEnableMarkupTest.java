package com.csse3200.game.ui.effects.texteffects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CrazyOrTypeAndEnableMarkupTest extends TextEffectsTestBase {

    static java.util.stream.Stream<Arguments> enableMarkupCases() {
        // OK font/data
        BitmapFont.BitmapFontData okData = new BitmapFont.BitmapFontData();
        okData.markupEnabled = false;
        BitmapFont okFont = mock(BitmapFont.class);
        when(okFont.getData()).thenReturn(okData);
        Label.LabelStyle allOk = new Label.LabelStyle(okFont, Color.WHITE);
        
        // font null
        Label.LabelStyle fontNull = new Label.LabelStyle();
        fontNull.font = null;
        fontNull.fontColor = Color.WHITE;

        // data null
        BitmapFont fontWithNullData = mock(BitmapFont.class);
        when(fontWithNullData.getData()).thenReturn(null);
        Label.LabelStyle dataNull = new Label.LabelStyle(fontWithNullData, Color.WHITE);

        return java.util.stream.Stream.of(
                Arguments.of("style == null", null, false),
                Arguments.of("style.font == null", fontNull, false),
                Arguments.of("style.font.getData() == null", dataNull, false),
                Arguments.of("all non-null -> true branch", allOk, true)
        );
    }

    @ParameterizedTest
    @CsvSource({"Hello, 60"})
    void crazyOrType_plain_and_crazy_paths(String hello, float cps) {
        try (MockedStatic<Timer> ignored = mockTimersImmediate()) {
            TextEffects fx = new TextEffects();

            var p = newLabel("");
            fx.crazyOrType(p, hello, cps);
            assertEquals(hello, p.getText().toString());

            var c = newLabel("");
            String crazy = "{CRAZY style=blast origin=middle spread=2 fps=60 cycles=1 rainbow=true rshift=20}OK{/CRAZY}";
            fx.crazyOrType(c, crazy, 40f);
            assertFalse(c.getText().toString().isEmpty());
            fx.cancel();
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("enableMarkupCases")
    void covers_if_chain(String name, Label.LabelStyle style, boolean expectToggle) {
        Label lbl = mock(Label.class);
        when(lbl.getStyle()).thenReturn(style);
        doAnswer(inv -> null).when(lbl).setStyle(any());

        BitmapFont.BitmapFontData data =
                (style != null && style.font != null) ? style.font.getData() : null;
        boolean before = data != null && data.markupEnabled;

        TextEffects.enableMarkup(lbl);

        boolean after = data != null && data.markupEnabled;
        if (expectToggle) {
            assertTrue(after);
        } else {
            assertEquals(before, after);
        }
    }
}
