package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.GdxGame;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.effects.TextEffects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * High-coverage tests for BaseEndScreenDisplays.
 * - Registers a no-op RenderService so dispose() doesn't NPE.
 * - Mocks Timer.schedule/post statically so delayed tasks run immediately (no Timer.update()).
 * - Mocks TextEffects.readRandomLine to avoid LibGDX file IO in factories.
 * - Exercises both Victory/DEFEATED and "other title" branches, ctor branches, setters (pre/post build),
 * factories (lambdas), neutralize/formatting paths, and dispose idempotency.
 */
class BaseEndScreenDisplaysTest {

    private RenderService renderServiceMock;

    // ---------- Test wiring ----------

    /**
     * Make all Timer tasks run immediately (no ticking needed).
     */
    private static MockedStatic<Timer> mockTimersImmediate() {
        MockedStatic<Timer> mocked = mockStatic(Timer.class, CALLS_REAL_METHODS);

        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return t;
                });

        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return t;
                });

        mocked.when(() -> Timer.schedule(any(Timer.Task.class), anyFloat(), anyFloat(), anyInt()))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return t;
                });

        mocked.when(() -> Timer.post(any(Timer.Task.class)))
                .thenAnswer(inv -> {
                    Timer.Task t = inv.getArgument(0);
                    t.run();
                    return null;
                });

        return mocked;
    }

    /**
     * Mock TextEffects.readRandomLine(...) to avoid LibGDX files.internal during factory subtitle suppliers.
     */
    private static MockedStatic<TextEffects> mockTextEffectsRead() {
        MockedStatic<TextEffects> mocked = mockStatic(TextEffects.class, CALLS_REAL_METHODS);
        mocked.when(() -> TextEffects.readRandomLine(anyString(), anyString()))
                .thenAnswer(inv -> inv.getArgument(1)); // return provided default
        mocked.when(() -> TextEffects.enableMarkup(any(Label.class))).thenCallRealMethod();
        return mocked;
    }

    private static Stream<Arguments> ctorCases() {
        Runnable customSecondary = () -> { /* no-op */ };
        return Stream.of(
                // titleText,   supplier,                         secondaryAction, expectSubtitle
                Arguments.of("Victory", null, null, false), // supplier null
                Arguments.of("DEFEATED", (java.util.function.Supplier<String>) () -> "", null, false), // empty
                Arguments.of("DEFEATED", (java.util.function.Supplier<String>) () -> "   ", null, false), // blank
                Arguments.of("DEFEATED", (java.util.function.Supplier<String>) () -> "RIP", null, true),  // text
                Arguments.of("Something", (java.util.function.Supplier<String>) () -> "hey", null, true),  // "other title" branch
                Arguments.of("Something", (java.util.function.Supplier<String>) () -> "yo", customSecondary, true) // custom secondary
        );
    }

    @BeforeEach
    void setUp() {
        renderServiceMock = mock(RenderService.class);
        doNothing().when(renderServiceMock).register(any());
        doNothing().when(renderServiceMock).unregister(any());
        ServiceLocator.registerRenderService(renderServiceMock);
    }

    // ---------- Fakes / Probes ----------

    @AfterEach
    void tearDown() {
        try {
            Timer.instance().clear();
        } catch (Throwable ignored) {
            throw new AssertionError("Timer cleanup failed", ignored);
        }
        try {
            ServiceLocator.clear();
        } catch (Throwable ignored) {
            // Fallback to avoid cross-test bleed if clear() isn't available
            ServiceLocator.registerRenderService(mock(RenderService.class));
        }
    }

    @Test
    void victory_and_defeated_paths_using_fake_cover_build_setters_and_dispose() {
        try (MockedStatic<Timer> t = mockTimersImmediate();
             MockedStatic<TextEffects> te = mockTextEffectsRead()) {

            GdxGame game = mock(GdxGame.class);

            // Victory (7-arg with supplier)
            FakeEndScreen v = new FakeEndScreen(
                    game, "Victory", Color.GREEN, "Continue",
                    () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                    null, () -> "You did it!"
            );
            v.buildUI(v.root);
            assertNotNull(v.createdTitle);
            assertNotNull(v.createdSubtitle);
            assertNotNull(v.createdSubtitle.getText());
            assertEquals("Round: 1", v.roundLabel.getText().toString());
            assertEquals("Time: 00:00", v.timeLabel.getText().toString());
            v.setRound(7);
            assertEquals("Round: 7", v.roundLabel.getText().toString());
            v.setRound(-3);
            assertEquals("Round: 1", v.roundLabel.getText().toString());
            v.setElapsedSeconds(125);
            assertEquals("Time: 02:05", v.timeLabel.getText().toString());
            v.setElapsedSeconds(-1);
            assertEquals("Time: 00:00", v.timeLabel.getText().toString());
            v.setElapsedText("09:59");
            assertEquals("Time: 09:59", v.timeLabel.getText().toString());
            assertNotNull(v.lastPrimaryAction);
            v.lastPrimaryAction.run();
            verify(game, times(1)).setScreen(GdxGame.ScreenType.MAIN_GAME);
            assertNotNull(v.lastSecondaryAction);
            v.lastSecondaryAction.run();
            assertTrue(v.backMainMenuCalled);
            v.dispose();
            v.dispose();

            // DEFEATED (7-arg with supplier)
            FakeEndScreen d = new FakeEndScreen(
                    game, "DEFEATED", Color.RED, "Try Again",
                    () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME),
                    null, () -> "Death is only the beginning."
            );
            d.buildUI(d.root);
            assertNotNull(d.createdTitle);
            assertNotNull(d.createdSubtitle);
            assertNotNull(d.createdSubtitle.getText());
            assertEquals("Round: 1", d.roundLabel.getText().toString());
            assertEquals("Time: 00:00", d.timeLabel.getText().toString());
            assertNotNull(d.lastPrimaryAction);
            d.lastPrimaryAction.run();
            verify(game, times(2)).setScreen(GdxGame.ScreenType.MAIN_GAME);
            assertNotNull(d.lastSecondaryAction);
            d.lastSecondaryAction.run();
            assertTrue(d.backMainMenuCalled);
            d.dispose();
            d.dispose();
        }
    }

    @Test
    void sixArgConstructor_isCovered_builds_and_disposes() {
        try (MockedStatic<Timer> t = mockTimersImmediate()) {
            GdxGame game = mock(GdxGame.class);
            FakeEndScreenNoSupplier s = new FakeEndScreenNoSupplier(
                    game, "Victory", Color.WHITE, "Continue",
                    () -> {
                    }, null // secondary becomes backMainMenu() inside
            );
            s.buildUI(s.root);
            s.dispose();
        }
    }

    // ---------- Tests ----------

    @Test
    void factories_returnConfiguredInstances_spy_captures_lambdas_and_buttons() {
        try (MockedStatic<Timer> t = mockTimersImmediate();
             MockedStatic<TextEffects> te = mockTextEffectsRead()) {

            GdxGame game = mock(GdxGame.class);
            Table root = new Table();

            // Victory factory instance
            BaseEndScreenDisplays v = BaseEndScreenDisplays.victory(game);
            BaseEndScreenDisplays spyV = spy(v);

            final Label[] subtitleV = new Label[1];
            final Runnable[] primaryV = new Runnable[1];
            final Runnable[] secondaryV = new Runnable[1];

            doAnswer(inv -> new Label(inv.getArgument(1), new Label.LabelStyle(new BitmapFont(), inv.getArgument(3))))
                    .when(spyV).addTitle(any(), anyString(), anyFloat(), any(Color.class), anyFloat());

            doAnswer(inv -> {
                String text = inv.getArgument(1);
                Label lbl = new Label(text, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
                if ("".equals(text) && subtitleV[0] == null) subtitleV[0] = lbl;
                return lbl;
            }).when(spyV).addBody(any(), anyString(), anyFloat(), anyFloat());

            doAnswer(inv -> {
                String txt = inv.getArgument(0);
                Runnable r = inv.getArgument(2);
                if (primaryV[0] == null) primaryV[0] = r;
                else secondaryV[0] = r;
                TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
                st.font = new BitmapFont();
                return new TextButton(txt, st);
            }).when(spyV).button(anyString(), anyFloat(), any(Runnable.class));

            spyV.buildUI(root);
            assertNotNull(subtitleV[0]);
            assertNotNull(primaryV[0]);
            assertNotNull(secondaryV[0]);
            primaryV[0].run();
            verify(game, times(1)).setScreen(GdxGame.ScreenType.MAIN_GAME);
            secondaryV[0].run();
            spyV.dispose();
            spyV.dispose();

            // DEFEATED factory instance
            BaseEndScreenDisplays d = BaseEndScreenDisplays.defeated(game);
            BaseEndScreenDisplays spyD = spy(d);

            final Label[] subtitleD = new Label[1];
            final Runnable[] primaryD = new Runnable[1];
            final Runnable[] secondaryD = new Runnable[1];

            doAnswer(inv -> new Label(inv.getArgument(1), new Label.LabelStyle(new BitmapFont(), inv.getArgument(3))))
                    .when(spyD).addTitle(any(), anyString(), anyFloat(), any(Color.class), anyFloat());

            doAnswer(inv -> {
                String text = inv.getArgument(1);
                Label lbl = new Label(text, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
                if ("".equals(text) && subtitleD[0] == null) subtitleD[0] = lbl;
                return lbl;
            }).when(spyD).addBody(any(), anyString(), anyFloat(), anyFloat());

            doAnswer(inv -> {
                Runnable r = inv.getArgument(2);
                if (primaryD[0] == null) primaryD[0] = r;
                else secondaryD[0] = r;
                TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
                st.font = new BitmapFont();
                return new TextButton(inv.getArgument(0), st);
            }).when(spyD).button(anyString(), anyFloat(), any(Runnable.class));

            spyD.buildUI(root);
            assertNotNull(subtitleD[0]);
            assertNotNull(primaryD[0]);
            assertNotNull(secondaryD[0]);
            primaryD[0].run();
            verify(game, times(2)).setScreen(GdxGame.ScreenType.MAIN_GAME);
            secondaryD[0].run();
            spyD.dispose();
            spyD.dispose();
        }
    }

    @Test
    void baseScaleAndSpacingMethods_areCovered_viaProbe() {
        ScalesProbe probe = new ScalesProbe(mock(GdxGame.class));
        assertEquals(3f, probe.baseTitle(), 1e-6);
        assertEquals(3f, probe.baseInfo(), 1e-6);
        assertEquals(2f, probe.baseBtn(), 1e-6);
        assertEquals(30f, probe.baseGap(), 1e-6);
        assertEquals(50f, probe.baseBlock(), 1e-6);
    }

    @Test
    void safeTrim_null_and_whitespace_branches() throws Exception {
        var m = BaseEndScreenDisplays.class.getDeclaredMethod("safeTrim", String.class);
        m.setAccessible(true);
        assertEquals("", m.invoke(null, (Object) null));
        assertEquals("", m.invoke(null, "   "));
        assertEquals("x", m.invoke(null, " x "));
    }

    @ParameterizedTest
    @MethodSource("ctorCases")
    void parameterised_constructor_and_build_paths(
            String titleText,
            java.util.function.Supplier<String> subtitleSupplier,
            Runnable secondaryAction,
            boolean expectSubtitle
    ) {
        try (MockedStatic<Timer> t = mockTimersImmediate()) {
            GdxGame game = mock(GdxGame.class);

            // Use the 7-arg constructor directly to hit its branches
            FakeEndScreen screen = new FakeEndScreen(
                    game,
                    titleText,
                    Color.WHITE,
                    "Primary",
                    () -> {
                    },             // primary action
                    secondaryAction,      // null -> defaults to backMainMenu(), non-null -> uses custom
                    subtitleSupplier      // null/blank/text
            );

            // Exercise setter branches when labels are NULL (before buildUI)
            screen.setRound(5);
            screen.setElapsedSeconds(42);
            screen.setElapsedText("01:23");

            // Build UI
            screen.buildUI(screen.root);

            // After build, exercise non-null label branches
            screen.setRound(-7);               // clamps to 1
            screen.setElapsedSeconds(-9);      // clamps to 0 -> 00:00
            screen.setElapsedText("09:59");

            // Subtitle presence/absence
            if (expectSubtitle) {
                assertNotNull(screen.createdSubtitle, "Subtitle should be created when supplier is non-empty");
            } else {
                assertNull(screen.createdSubtitle, "Subtitle should NOT be created when supplier is null/blank");
            }

            // Secondary behaviour
            assertNotNull(screen.lastSecondaryAction);
            if (secondaryAction == null) {
                // default backMainMenu()
                assertFalse(screen.backMainMenuCalled);
                screen.lastSecondaryAction.run();
                assertTrue(screen.backMainMenuCalled, "Default secondary should call backMainMenu()");
            } else {
                // custom secondary runnable
                screen.lastSecondaryAction.run();
                assertFalse(screen.backMainMenuCalled, "Custom secondary should NOT call backMainMenu()");
            }

            // Dispose twice to hit idempotent branch
            screen.dispose();
            screen.dispose();
        }
    }

    @Test
    void neutralizeTint_allNonNull_setsAllToWhite() throws Exception {
        // Arrange
        Table root = new Table();
        Actor wrapper = new Actor();
        Label label = new Label("x", new Label.LabelStyle(new BitmapFont(), Color.RED));

        root.setColor(Color.CYAN);
        wrapper.setColor(Color.MAGENTA);
        label.setColor(Color.YELLOW);

        // Act
        var m = BaseEndScreenDisplays.class.getDeclaredMethod(
                "neutralizeTint", com.badlogic.gdx.scenes.scene2d.Actor.class,
                com.badlogic.gdx.scenes.scene2d.Actor.class,
                com.badlogic.gdx.scenes.scene2d.ui.Table.class
        );
        m.setAccessible(true);
        m.invoke(null, label, wrapper, root);

        // Assert
        assertEquals(Color.WHITE, root.getColor());
        assertEquals(Color.WHITE, wrapper.getColor());
        assertEquals(Color.WHITE, label.getColor());
    }

    // ---------- Parameterised constructor/build tests to cover remaining branches ----------

    @Test
    void neutralizeTint_onlyRoot_setsRootToWhite() throws Exception {
        Table root = new Table();
        root.setColor(Color.GRAY);

        var m = BaseEndScreenDisplays.class.getDeclaredMethod(
                "neutralizeTint", Actor.class, Actor.class, Table.class);
        m.setAccessible(true);

        // label = null, wrapper = null
        m.invoke(null, null, null, root);

        assertEquals(Color.WHITE, root.getColor());
    }

    @Test
    void neutralizeTint_onlyWrapper_setsWrapperToWhite() throws Exception {
        Actor wrapper = new Actor();
        wrapper.setColor(Color.GRAY);

        var m = BaseEndScreenDisplays.class.getDeclaredMethod(
                "neutralizeTint", Actor.class, Actor.class, Table.class);
        m.setAccessible(true);

        // label = null, root = null
        m.invoke(null, null, wrapper, null);

        assertEquals(Color.WHITE, wrapper.getColor());
    }

    @Test
    void neutralizeTint_onlyLabel_setsLabelToWhite() throws Exception {
        Label label = new Label("x", new Label.LabelStyle(new BitmapFont(), Color.BLUE));
        label.setColor(Color.BLUE);

        var m = BaseEndScreenDisplays.class.getDeclaredMethod(
                "neutralizeTint", Actor.class, Actor.class, Table.class);
        m.setAccessible(true);

        // wrapper = null, root = null
        m.invoke(null, label, null, null);

        assertEquals(Color.WHITE, label.getColor());
    }

    @Test
    void neutralizeTint_allNull_noThrow() throws Exception {
        var m = BaseEndScreenDisplays.class.getDeclaredMethod(
                "neutralizeTint", Actor.class, Actor.class, Table.class);
        m.setAccessible(true);

        // Should be a no-op without exceptions
        assertDoesNotThrow(() -> {
            try {
                m.invoke(null, null, null, null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Minimal fake subclass using 7-arg ctor (with supplier) that captures widgets/actions.
     */
    static class FakeEndScreen extends BaseEndScreenDisplays {
        final Table root = new Table();

        Label createdTitle;
        Label createdSubtitle;
        Label roundLabel;
        Label timeLabel;

        TextButton lastPrimaryButton;
        TextButton lastSecondaryButton;

        Runnable lastPrimaryAction;
        Runnable lastSecondaryAction;

        boolean backMainMenuCalled;

        FakeEndScreen(GdxGame game,
                      String titleText,
                      Color titleColor,
                      String primaryText,
                      Runnable primaryAction,
                      Runnable secondaryAction,
                      java.util.function.Supplier<String> subtitleSupplier) {
            super(game, titleText, titleColor, primaryText, primaryAction, secondaryAction, subtitleSupplier);
        }

        @Override
        protected Label addTitle(Table root, String text, float scale, Color color, float padBottom) {
            Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), color);
            Label lbl = new Label(text, style);
            createdTitle = lbl;
            return lbl;
        }

        @Override
        protected Label addBody(Table root, String text, float scale, float padBottom) {
            Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
            Label lbl = new Label(text, style);
            if (text != null) {
                if (text.startsWith("Round:")) roundLabel = lbl;
                if (text.startsWith("Time:")) timeLabel = lbl;
            }
            if (createdSubtitle == null && "".equals(text)) createdSubtitle = lbl;
            return lbl;
        }

        @Override
        protected TextButton button(String text, float labelScale, Runnable onClick) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = new BitmapFont();
            TextButton btn = new TextButton(text, style);
            if (lastPrimaryButton == null) {
                lastPrimaryButton = btn;
                lastPrimaryAction = onClick;
            } else {
                lastSecondaryButton = btn;
                lastSecondaryAction = onClick;
            }
            return btn;
        }

        @Override
        protected void backMainMenu() {
            backMainMenuCalled = true;
        }

        // Deterministic overrides (your class returns larger values by default; these are fine for tests)
        @Override
        protected float titleFontScale() {
            return 3f;
        }

        @Override
        protected float infoFontScale() {
            return 2f;
        }

        @Override
        protected float buttonLabelScale() {
            return 1.5f;
        }

        @Override
        protected float buttonsGap() {
            return 10f;
        }

        @Override
        protected float blockPad() {
            return 8f;
        }
    }

    /**
     * Fake using the 6-arg ctor (no supplier) to cover that constructor branch.
     */
    static class FakeEndScreenNoSupplier extends BaseEndScreenDisplays {
        final Table root = new Table();

        FakeEndScreenNoSupplier(GdxGame game,
                                String titleText,
                                Color titleColor,
                                String primaryText,
                                Runnable primaryAction,
                                Runnable secondaryAction) {
            super(game, titleText, titleColor, primaryText, primaryAction, secondaryAction);
        }

        @Override
        protected Label addTitle(Table r, String t, float s, Color c, float p) {
            return new Label(t, new Label.LabelStyle(new BitmapFont(), c));
        }

        @Override
        protected Label addBody(Table r, String t, float s, float p) {
            return new Label(t, new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        @Override
        protected TextButton button(String t, float s, Runnable r) {
            TextButton.TextButtonStyle st = new TextButton.TextButtonStyle();
            st.font = new BitmapFont();
            return new TextButton(t, st);
        }
    }

    /**
     * Probe to call base scale/spacing methods so they’re covered when needed.
     */
    static class ScalesProbe extends BaseEndScreenDisplays {
        ScalesProbe(GdxGame g) {
            super(g, "X", Color.WHITE, "P", () -> {
            }, null);
        }

        float baseTitle() {
            return super.titleFontScale();
        }

        float baseInfo() {
            return super.infoFontScale();
        }

        float baseBtn() {
            return super.buttonLabelScale();
        }

        float baseGap() {
            return super.buttonsGap();
        }

        float baseBlock() {
            return super.blockPad();
        }
    }
}

