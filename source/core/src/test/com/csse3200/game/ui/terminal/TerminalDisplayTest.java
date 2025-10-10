package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Single-class, headless-safe tests covering TerminalDisplay helpers, draw(), buildUI(), and create().
 */
@ExtendWith(GameExtension.class)
class TerminalDisplayTest {

    // ----------------------------- buildUI & create -----------------------------

    /**
     * Minimal wiring for draw(): inject only fields used at draw-time (no real Stage/Skin).
     */
    private static TerminalDisplay newDisplayWithMinimalUI(Terminal terminal) throws Exception {
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        set(d, "terminal", terminal);

        Label.LabelStyle style = new Label.LabelStyle(new BitmapFont(), null);
        set(d, "suggestionLabelStyle", style);
        set(d, "label", new Label("> ", style));
        set(d, "container", new Table());
        set(d, "promptBox", new Table());
        set(d, "suggestionsBox", new Table());
        set(d, "lastShown", new ArrayList<String>());

        return d;
    }

    /**
     * Read (not set) the final static UIComponent.skin. Skip tests if your runtime didn't initialise it.
     */
    private static Skin getUiSkinOrSkip() throws Exception {
        Field f = UIComponent.class.getDeclaredField("skin");
        f.setAccessible(true);
        Skin skin = (Skin) f.get(null);
        assumeTrue(skin != null, "UIComponent.skin not initialised in test runtime");
        return skin;
    }

    // reflection utils
    private static void set(Object target, String field, Object value) throws Exception {
        Field f = find(target.getClass(), field);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ----------------------------- draw() & helpers -----------------------------

    private static Object get(Object target, String field) throws Exception {
        Field f = find(target.getClass(), field);
        f.setAccessible(true);
        return f.get(target);
    }

    private static Field find(Class<?> cls, String name) throws NoSuchFieldException {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    // convenience accessors
    private static Table getContainer(TerminalDisplay d) throws Exception {
        return (Table) get(d, "container");
    }

    private static Table getSuggestionsBox(TerminalDisplay d) throws Exception {
        return (Table) get(d, "suggestionsBox");
    }

    @SuppressWarnings("unchecked")
    private static List<String> getLastShown(TerminalDisplay d) throws Exception {
        return (List<String>) get(d, "lastShown");
    }

    @Test
    void buildUI_addsActors_and_initialisesWidgets() throws Exception {
        // Ensure UIComponent.skin exists (we won't set it; just mutate the Skin object).
        Skin skin = getUiSkinOrSkip();
        // Add minimal assets the code expects:
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable());
        if (!skin.has("default", Label.LabelStyle.class))
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));

        // SUT
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage); // inject stage instance (not static final)

        Table fakeRoot = new Table();
        d.buildUI(fakeRoot);

        // Stage should get our container and suggestionsBox
        verify(stage, atLeast(2)).addActor(any());

        // Core widgets exist
        assertNotNull(get(d, "container"));
        assertNotNull(get(d, "promptBox"));
        assertNotNull(get(d, "label"));
        assertNotNull(get(d, "suggestionsBox"));

        // Container starts hidden
        assertFalse(((Table) get(d, "container")).isVisible());
    }

    @Test
    void create_throws_if_no_Terminal_component_attached() throws Exception {
        Entity e = new Entity();
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        e.addComponent(d); // attaches display -> sets its internal entity reference

        set(d, "stage", mock(Stage.class)); // if super.create touches stage

        assertThrows(NullPointerException.class, d::create);
    }

    @Test
    void buildUI_installs_F1_capture_listener_that_hides_suggestions() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable());
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        // Capture the listener
        ArgumentCaptor<InputListener> cap = ArgumentCaptor.forClass(InputListener.class);
        verify(stage).addCaptureListener(cap.capture());
        InputListener listener = cap.getValue();
        assertNotNull(listener);

        // Make popup visible and lastShown non-empty, then press F1
        Table suggestionsBox = (Table) get(d, "suggestionsBox");
        suggestionsBox.setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("help")));

        InputEvent ev = new InputEvent();
        boolean consumed = listener.keyDown(ev, com.badlogic.gdx.Input.Keys.F1);

        assertFalse(consumed, "F1 must not be consumed");
        assertFalse(suggestionsBox.isVisible(), "F1 should hide suggestions");
        assertTrue(((List<?>) get(d, "lastShown")).isEmpty(), "F1 should clear lastShown");
    }

    @Test
    void draw_closed_hidesPrompt_andSuggestions() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(false);

        // prime popup visible
        getSuggestionsBox(d).setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("help")));

        d.draw(mock(SpriteBatch.class));

        assertFalse(getContainer(d).isVisible());
        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_open_noPrefixOrNoSuggestions_hidesSuggestions() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("   "); // empty first token
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of("help", "heal"));

        d.draw(mock(SpriteBatch.class));

        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_open_withPrefix_showsPopup_andBuildsRows_cappedTo5() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");
        when(terminal.getAutocompleteSuggestions()).thenReturn(
                List.of("help", "heal", "hello", "heap", "health", "helm", "hear")
        );

        d.draw(mock(SpriteBatch.class));

        assertTrue(getSuggestionsBox(d).isVisible());
        assertEquals(5, getSuggestionsBox(d).getChildren().size);
    }

    @Test
    void draw_updatesPromptLabelText() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("status --all");

        d.draw(mock(SpriteBatch.class));

        Label label = (Label) get(d, "label");
        assertEquals("> status --all", label.getText().toString());
    }

    @Test
    void acceptSuggestion_replacesOnlyFirstToken_andHidesPopup() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.getEnteredMessage()).thenReturn("he world");

        getSuggestionsBox(d).setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("help")));

        Method m = TerminalDisplay.class.getDeclaredMethod("acceptSuggestion", String.class);
        m.setAccessible(true);
        m.invoke(d, "help");

        verify(terminal).setEnteredMessage("help world");
        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void sameList_variousCases() throws Exception {
        Method m = TerminalDisplay.class.getDeclaredMethod("sameList", List.class, List.class);
        m.setAccessible(true);

        List<String> a = List.of("x", "y");
        assertTrue((boolean) m.invoke(null, a, a)); // same ref
        assertTrue((boolean) m.invoke(null, null, null)); // both null
        assertFalse((boolean) m.invoke(null, null, a));
        assertFalse((boolean) m.invoke(null, a, null));
        assertFalse((boolean) m.invoke(null, List.of("a"), List.of("a", "b")));
        assertFalse((boolean) m.invoke(null, List.of("a", "c"), List.of("a", "b")));
        assertTrue(
                (boolean) m.invoke(
                        null,
                        Arrays.asList("a", null, "c"),
                        Arrays.asList("a", null, "c")
                )
        );
    }

    @Test
    void skipFirstToken_covers_null_and_nonNull_branches() throws Exception {
        var m = TerminalDisplay.class.getDeclaredMethod("skipFirstToken", String.class, boolean.class);
        m.setAccessible(true);

        // null input -> becomes "" (covers s == null branch)
        int startNull = (int) m.invoke(null, (Object) null, true);
        int endNull = (int) m.invoke(null, (Object) null, false);
        assertEquals(0, startNull);
        assertEquals(0, endNull);

        // non-null with leading spaces
        int start1 = (int) m.invoke(null, "   abc def", true);   // index of 'a'
        int end1 = (int) m.invoke(null, "   abc def", false);  // after 'abc'
        assertEquals(3, start1);
        assertEquals(6, end1);

        // non-null without leading spaces
        int start2 = (int) m.invoke(null, "abc def", true);      // index 0
        int end2 = (int) m.invoke(null, "abc def", false);     // after 'abc'
        assertEquals(0, start2);
        assertEquals(3, end2);

        // non-null consisting only of spaces
        int start3 = (int) m.invoke(null, "   ", true);          // past whitespace
        int end3 = (int) m.invoke(null, "   ", false);         // same position
        assertEquals(3, start3);
        assertEquals(3, end3);
    }

    @Test
    void tokenHelpers_coverNullLeadingSpacesSingleAndMulti() throws Exception {
        Method extract = TerminalDisplay.class.getDeclaredMethod("extractFirstToken", String.class);
        Method strip = TerminalDisplay.class.getDeclaredMethod("stripFirstToken", String.class);
        extract.setAccessible(true);
        strip.setAccessible(true);

        assertEquals("", extract.invoke(null, (String) null));
        assertEquals("", extract.invoke(null, " \t  "));
        assertEquals("hello", extract.invoke(null, "hello"));
        assertEquals("hello", extract.invoke(null, "  hello world"));

        assertEquals("", strip.invoke(null, (String) null));
        assertEquals("", strip.invoke(null, "   "));
        assertEquals("", strip.invoke(null, "hello"));
        assertEquals(" world", strip.invoke(null, "hello world"));
        assertEquals("   world", strip.invoke(null, "hello   world"));
    }

    @Test
    void draw_reusesRowsWhenSuggestionsUnchanged_andRebuildsWhenChanged() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        SpriteBatch batch = mock(SpriteBatch.class);

        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");

        // First draw with s1 -> builds rows
        List<String> s1 = new ArrayList<>(List.of("help", "heal", "hello", "heap", "health"));
        when(terminal.getAutocompleteSuggestions()).thenReturn(s1);
        d.draw(batch);
        int rows1 = getSuggestionsBox(d).getChildren().size;
        List<String> lastRef = getLastShown(d);
        assertEquals(5, rows1);

        // Same reference list -> should NOT rebuild lastShown (same object), row count unchanged
        when(terminal.getAutocompleteSuggestions()).thenReturn(s1);
        d.draw(batch);
        assertSame(lastRef, getLastShown(d));
        assertEquals(rows1, getSuggestionsBox(d).getChildren().size);

        // Different list (new object) -> SHOULD rebuild lastShown and still cap rows to 5
        List<String> s2 = new ArrayList<>(List.of("help", "heal", "hello", "heap", "health", "helm"));
        when(terminal.getAutocompleteSuggestions()).thenReturn(s2);
        d.draw(batch);
        assertNotSame(lastRef, getLastShown(d));
        assertEquals(5, getSuggestionsBox(d).getChildren().size);
    }

    @Test
    void draw_open_withPrefixButNoSuggestions_hidesPopup_andClearsLastShown() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);

        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");  // non-empty first token
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of()); // empty suggestions

        // prime popup visible + lastShown non-empty to ensure it gets cleared
        getSuggestionsBox(d).setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("help")));

        d.draw(mock(SpriteBatch.class));

        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_returnsEarly_whenTerminalNull_leavesStateUntouched() throws Exception {
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Table container = spy(new Table());
        Table suggestions = spy(new Table());
        suggestions.setVisible(true);

        set(d, "container", container);
        set(d, "promptBox", new Table());
        set(d, "label", new Label("> ", new Label.LabelStyle(new BitmapFont(), null)));
        set(d, "suggestionsBox", suggestions);
        set(d, "lastShown", new ArrayList<>(List.of("foo")));
        set(d, "terminal", null); // ← triggers early return

        d.draw(mock(SpriteBatch.class));

        verify(container, never()).setVisible(anyBoolean());
        verify(container, never()).toFront();
        verify(suggestions, never()).setVisible(false);
        assertEquals(List.of("foo"), getLastShown(d));
    }

    @Test
    void draw_open_setsContainerVisible_and_toFront() throws Exception {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn(null);
        when(terminal.getAutocompleteSuggestions()).thenReturn(null); // show=false

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Table container = spy(new Table());
        set(d, "container", container);
        set(d, "promptBox", new Table());
        set(d, "label", new Label("> ", new Label.LabelStyle(new BitmapFont(), null)));
        set(d, "suggestionsBox", new Table());
        set(d, "lastShown", new ArrayList<>());
        set(d, "terminal", terminal);

        d.draw(mock(SpriteBatch.class));

        verify(container).setVisible(true);
        verify(container).toFront();
        assertFalse(getSuggestionsBox(d).isVisible());
    }

    @Test
    void draw_closed_setsContainerInvisible_hidesSuggestions_and_clears() throws Exception {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);

        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        Table containerSpy = spy(getContainer(d));
        set(d, "container", containerSpy);

        getSuggestionsBox(d).setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("a")));

        d.draw(mock(SpriteBatch.class));

        verify(containerSpy).setVisible(false);
        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_open_showTrue_rebuilds_packs_and_bringsBothToFront() throws Exception {
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("co");
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of("config", "commit"));

        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        Table suggestionsSpy = spy(getSuggestionsBox(d));
        Table promptSpy = spy((Table) get(d, "promptBox"));
        set(d, "suggestionsBox", suggestionsSpy);
        set(d, "promptBox", promptSpy);

        d.draw(mock(SpriteBatch.class));

        verify(suggestionsSpy).setVisible(true);
        verify(suggestionsSpy, atLeastOnce()).pack(); // rebuild path
        verify(suggestionsSpy).toFront();
        verify(promptSpy).toFront();
        assertEquals(2, suggestionsSpy.getChildren().size);
    }

    @Test
    void draw_open_prefixNonEmpty_butSuggestionsNull_hidesPopup_andClears() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("abc");   // prefix non-empty
        when(terminal.getAutocompleteSuggestions()).thenReturn(null); // show=false

        getSuggestionsBox(d).setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("x")));

        d.draw(mock(SpriteBatch.class));

        assertFalse(getSuggestionsBox(d).isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_open_nullMessage_setsPromptChevronOnly() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn(null);
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of()); // show=false

        d.draw(mock(SpriteBatch.class));

        Label lbl = (Label) get(d, "label");
        assertEquals("> ", lbl.getText().toString());
    }

    @Test
    void draw_withSameSuggestions_skipsRebuildAndPack() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);

        Table suggestionsSpy = spy(getSuggestionsBox(d));
        set(d, "suggestionsBox", suggestionsSpy);

        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");

        List<String> s1 = new ArrayList<>(List.of("help", "heal"));
        when(terminal.getAutocompleteSuggestions()).thenReturn(s1);

        // First draw → rebuild + pack
        d.draw(mock(SpriteBatch.class));
        verify(suggestionsSpy, times(1)).pack();
        List<String> lastRef = getLastShown(d);

        // Second draw with same suggestions list → no rebuild, no extra pack
        when(terminal.getAutocompleteSuggestions()).thenReturn(s1);
        d.draw(mock(SpriteBatch.class));
        verify(suggestionsSpy, times(1)).pack(); // still 1
        assertSame(lastRef, getLastShown(d));
    }

    @Test
    void draw_open_withNullContainer_doesNotThrow() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        set(d, "container", null); // hit container==null branch
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("x");
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of());

        assertDoesNotThrow(() -> d.draw(mock(SpriteBatch.class)));
    }

    @Test
    void draw_closed_withNullSuggestionsBox_doesNotThrow() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);
        set(d, "suggestionsBox", null); // hit suggestionsBox==null path inside !open
        when(terminal.isOpen()).thenReturn(false);

        assertDoesNotThrow(() -> d.draw(mock(SpriteBatch.class)));
    }

    @Test
    void draw_closed_whenSuggestionsVisible_hidesAndClears() throws Exception {
        // Arrange: terminal closed, suggestionsBox visible, lastShown non-empty
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);

        TerminalDisplay d = newDisplayWithMinimalUI(terminal);

        // spy container to verify visibility change
        Table containerSpy = spy(getContainer(d));
        set(d, "container", containerSpy);

        Table suggestions = getSuggestionsBox(d);
        suggestions.setVisible(true); // triggers the inner if
        set(d, "lastShown", new ArrayList<>(List.of("a", "b")));

        // Act
        d.draw(mock(SpriteBatch.class));

        // Assert: container hidden, popup hidden, lastShown cleared
        verify(containerSpy).setVisible(false);
        assertFalse(suggestions.isVisible());
        assertTrue(getLastShown(d).isEmpty());
    }

    @Test
    void draw_closed_whenSuggestionsNotVisible_doesNotClearLastShown() throws Exception {
        // Arrange: terminal closed, suggestionsBox present but NOT visible, lastShown non-empty
        Terminal terminal = mock(Terminal.class);
        when(terminal.isOpen()).thenReturn(false);

        TerminalDisplay d = newDisplayWithMinimalUI(terminal);

        // spy container to verify visibility change
        Table containerSpy = spy(getContainer(d));
        set(d, "container", containerSpy);

        Table suggestions = getSuggestionsBox(d);
        suggestions.setVisible(false); // inner if should NOT run
        List<String> before = new ArrayList<>(List.of("keep"));
        set(d, "lastShown", before);

        // Act
        d.draw(mock(SpriteBatch.class));

        // Assert: container hidden; popup stays not visible; lastShown NOT cleared
        verify(containerSpy).setVisible(false);
        assertFalse(suggestions.isVisible());
        assertEquals(before, getLastShown(d));
    }

    @Test
    void clickingSuggestionRow_invokesAcceptSuggestion_andHidesPopup() throws Exception {
        Terminal terminal = mock(Terminal.class);
        TerminalDisplay d = newDisplayWithMinimalUI(terminal);

        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he world"); // replace first token only
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of("help", "heal"));

        // Build rows
        d.draw(mock(SpriteBatch.class));

        // Grab first row and fire its ClickListener
        Table suggestions = getSuggestionsBox(d);
        assertTrue(suggestions.getChildren().size > 0, "Expected at least one suggestion row");
        var firstRow = suggestions.getChildren().first();

        // Find ClickListener and invoke
        var listeners = firstRow.getListeners();
        boolean clicked = false;
        for (int i = 0; i < listeners.size; i++) {
            var l = listeners.get(i);
            if (l instanceof com.badlogic.gdx.scenes.scene2d.utils.ClickListener cl) {
                cl.clicked(new InputEvent(), 0f, 0f);
                clicked = true;
                break;
            }
        }
        assertTrue(clicked, "Row should have a ClickListener");

        // acceptSuggestion(...) path verification
        verify(terminal).setEnteredMessage("help world");
        assertFalse(getSuggestionsBox(d).isVisible());
    }

    @Test
    void create_setsTerminal_whenComponentPresent() throws Exception {
        // Stub ServiceLocator → RenderService → Stage so super.create() is happy
        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            RenderService renderService = mock(RenderService.class);
            when(renderService.getStage()).thenReturn(mock(Stage.class));
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);

            // Entity with Terminal + our display (buildUI is no-op to avoid skin dependencies)
            Entity entity = new Entity();
            Terminal terminal = mock(Terminal.class);
            TerminalDisplay display = new TerminalDisplay(mock(GdxGame.class)) {
                @Override
                protected void buildUI(Table root) { /* no-op */ }
            };
            entity.addComponent(terminal);
            entity.addComponent(display);

            // Act
            assertDoesNotThrow(display::create);

            // Assert: terminal field set from entity component
            Field f = TerminalDisplay.class.getDeclaredField("terminal");
            f.setAccessible(true);
            assertSame(terminal, f.get(display), "create() must set the terminal field");
        }
    }

    @Test
    void create_throwsIllegalState_whenTerminalMissing() {
        try (MockedStatic<ServiceLocator> svc = Mockito.mockStatic(ServiceLocator.class)) {
            RenderService renderService = mock(RenderService.class);
            when(renderService.getStage()).thenReturn(mock(Stage.class));
            svc.when(ServiceLocator::getRenderService).thenReturn(renderService);

            // Entity with display only (no Terminal)
            Entity entity = new Entity();
            TerminalDisplay display = new TerminalDisplay(mock(GdxGame.class)) {
                @Override
                protected void buildUI(Table root) { /* no-op */ }
            };
            entity.addComponent(display);

            // Act + Assert
            IllegalStateException ex = assertThrows(IllegalStateException.class, display::create);
            assertTrue(ex.getMessage().contains("Terminal component is required"),
                    "Exception message should mention missing Terminal component");
        }
    }

    @Test
    void buildUI_uses_textfield_drawable_when_available() throws Exception {
        Skin skin = getUiSkinOrSkip();
        // Clean slate for keys we care about
        if (skin.has("textfield", Drawable.class)) skin.remove("textfield", Drawable.class);
        if (skin.has("rounded", Drawable.class)) skin.remove("rounded", Drawable.class);
        if (skin.has("button", Drawable.class)) skin.remove("button", Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        // Add only textfield
        BaseDrawable tf = new BaseDrawable();
        skin.add("textfield", tf, Drawable.class);

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        Table prompt = (Table) get(d, "promptBox");
        assertSame(tf, prompt.getBackground(), "Should pick textfield when present");

        // cleanup
        skin.remove("textfield", Drawable.class);
    }

    @Test
    void buildUI_uses_rounded_when_no_textfield_but_rounded_available() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (skin.has("textfield", Drawable.class)) skin.remove("textfield", Drawable.class);
        if (skin.has("rounded", Drawable.class)) skin.remove("rounded", Drawable.class);
        if (skin.has("button", Drawable.class)) skin.remove("button", Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        BaseDrawable rd = new BaseDrawable();
        skin.add("rounded", rd, Drawable.class);

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        Table prompt = (Table) get(d, "promptBox");
        assertSame(rd, prompt.getBackground(), "Should fall back to rounded when textfield is absent");

        // cleanup
        skin.remove("rounded", Drawable.class);
    }

    @Test
    void buildUI_uses_button_when_no_textfield_no_rounded_but_button_available() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (skin.has("textfield", Drawable.class)) skin.remove("textfield", Drawable.class);
        if (skin.has("rounded", Drawable.class)) skin.remove("rounded", Drawable.class);
        if (skin.has("button", Drawable.class)) skin.remove("button", Drawable.class);

        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        BaseDrawable btn = new BaseDrawable();
        skin.add("button", btn, Drawable.class);

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        Table prompt = (Table) get(d, "promptBox");
        assertSame(btn, prompt.getBackground(), "Should fall back to button when textfield/rounded are absent");

        // cleanup
        skin.remove("button", Drawable.class);
    }

    @Test
    void buildUI_uses_fallback_white_newDrawable_when_no_known_drawables() throws Exception {
        Skin skin = getUiSkinOrSkip();
        // Ensure none of the preferred keys exist
        if (skin.has("textfield", Drawable.class)) skin.remove("textfield", Drawable.class);
        if (skin.has("rounded", Drawable.class)) skin.remove("rounded", Drawable.class);
        if (skin.has("button", Drawable.class)) skin.remove("button", Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        // Provide a base "white" so Skin.newDrawable("white", color) works
        // (Skin will create a new tinted drawable, distinct from this base instance)
        BaseDrawable whiteBase = new BaseDrawable();
        if (!skin.has("white", Drawable.class)) {
            skin.add("white", whiteBase);
        }

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        Table prompt = (Table) get(d, "promptBox");
        assertNotNull(prompt.getBackground(), "Fallback should produce a background via newDrawable");
        assertNotSame(whiteBase, prompt.getBackground(), "newDrawable should return a new tinted drawable");
    }


    @Test
    void f1_hidesSuggestions_and_clears_lastShown_returnsFalse() throws Exception {
        // Arrange skin so buildUI won't fallback
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable(), Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        // Capture the capture-listener
        ArgumentCaptor<InputListener> cap = ArgumentCaptor.forClass(InputListener.class);
        verify(stage).addCaptureListener(cap.capture());
        InputListener listener = cap.getValue();

        // Prime: popup visible + lastShown non-empty
        Table suggestions = (Table) get(d, "suggestionsBox");
        suggestions.setVisible(true);
        set(d, "lastShown", new ArrayList<>(List.of("help")));

        // Act
        boolean consumed = listener.keyDown(new InputEvent(), com.badlogic.gdx.Input.Keys.F1);

        // Assert
        assertFalse(consumed);
        assertFalse(suggestions.isVisible());
        assertTrue(((List<?>) get(d, "lastShown")).isEmpty());
    }

    @Test
    void f1_whenPopupNotVisible_keeps_lastShown_returnsFalse() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable(), Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        ArgumentCaptor<InputListener> cap = ArgumentCaptor.forClass(InputListener.class);
        verify(stage).addCaptureListener(cap.capture());
        InputListener listener = cap.getValue();

        // popup NOT visible; lastShown has content
        Table suggestions = (Table) get(d, "suggestionsBox");
        suggestions.setVisible(false);
        List<String> before = new ArrayList<>(List.of("keep"));
        set(d, "lastShown", before);

        boolean consumed = listener.keyDown(new InputEvent(), com.badlogic.gdx.Input.Keys.F1);

        assertFalse(consumed);
        assertFalse(suggestions.isVisible());
        assertEquals(before, get(d, "lastShown")); // unchanged
    }

    @Test
    void nonF1_key_doesNothing_and_returnsFalse() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable(), Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        ArgumentCaptor<InputListener> cap = ArgumentCaptor.forClass(InputListener.class);
        verify(stage).addCaptureListener(cap.capture());
        InputListener listener = cap.getValue();

        // Prime: popup visible + lastShown non-empty
        Table suggestions = (Table) get(d, "suggestionsBox");
        suggestions.setVisible(true);
        List<String> before = new ArrayList<>(List.of("keep"));
        set(d, "lastShown", before);

        boolean consumed = listener.keyDown(new InputEvent(), com.badlogic.gdx.Input.Keys.F2);

        assertFalse(consumed);
        assertTrue(suggestions.isVisible());               // unchanged
        assertEquals(before, get(d, "lastShown"));         // unchanged
    }

    @Test
    void f1_withNullSuggestionsBox_doesNotThrow_and_returnsFalse() throws Exception {
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("textfield", Drawable.class)) skin.add("textfield", new BaseDrawable(), Drawable.class);
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }

        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);

        d.buildUI(new Table());

        ArgumentCaptor<InputListener> cap = ArgumentCaptor.forClass(InputListener.class);
        verify(stage).addCaptureListener(cap.capture());
        InputListener listener = cap.getValue();

        // Force null to hit the (suggestionsBox != null) guard's false side
        set(d, "suggestionsBox", null);

        boolean consumed = listener.keyDown(new InputEvent(), com.badlogic.gdx.Input.Keys.F1);

        assertFalse(consumed);
        // no exception, nothing to assert beyond return value
    }

    /**
     * Hovering a suggestion row sets its background; exiting clears it.
     */
    @Test
    void suggestionRow_hover_setsBackground_and_exit_clears_it() throws Exception {
        // Prepare Skin so buildUI() can create backgrounds/label styles
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        if (!skin.has("white", Drawable.class)) {
            skin.add("white", new BaseDrawable(), Drawable.class);
        }

        // Build real UI (so suggestionHoverBg is initialised)
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);
        d.buildUI(new Table());

        // Drive draw() to create suggestion rows
        Terminal terminal = mock(Terminal.class);
        set(d, "terminal", terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of("help", "heal"));
        d.draw(mock(SpriteBatch.class));

        Table suggestionsBox = (Table) get(d, "suggestionsBox");
        assertTrue(suggestionsBox.isVisible());
        Table row = (Table) suggestionsBox.getChildren().first();

        Drawable hoverBg = (Drawable) get(d, "suggestionHoverBg");
        assertNotNull(hoverBg, "suggestionHoverBg should be initialised by buildUI()");

        // Find the InputListener and trigger mouseMoved / exit
        boolean fired = false;
        for (EventListener l : row.getListeners()) {
            if (l instanceof InputListener il) {
                // hover
                boolean consumed = il.mouseMoved(new InputEvent(), 0f, 0f);
                assertFalse(consumed, "mouseMoved should not consume the event");
                assertSame(hoverBg, row.getBackground(), "hover should set suggestionHoverBg");

                // exit
                il.exit(new InputEvent(), 0f, 0f, 0, null);
                assertNull(row.getBackground(), "exit should clear the background");
                fired = true;
                break;
            }
        }
        assertTrue(fired, "Row should have an InputListener for hover/exit");
    }

    /**
     * Hovering one row must not affect other rows’ backgrounds.
     */
    @Test
    void suggestionRow_hover_affects_only_target_row() throws Exception {
        // Skin prerequisites
        Skin skin = getUiSkinOrSkip();
        if (!skin.has("default", Label.LabelStyle.class)) {
            skin.add("default", new Label.LabelStyle(new BitmapFont(), Color.WHITE));
        }
        if (!skin.has("white", Drawable.class)) {
            skin.add("white", new BaseDrawable(), Drawable.class);
        }

        // Build UI
        TerminalDisplay d = new TerminalDisplay(mock(GdxGame.class));
        Stage stage = mock(Stage.class);
        set(d, "stage", stage);
        d.buildUI(new Table());

        // Create two rows
        Terminal terminal = mock(Terminal.class);
        set(d, "terminal", terminal);
        when(terminal.isOpen()).thenReturn(true);
        when(terminal.getEnteredMessage()).thenReturn("he");
        when(terminal.getAutocompleteSuggestions()).thenReturn(List.of("help", "heal"));
        d.draw(mock(SpriteBatch.class));

        Table suggestionsBox = (Table) get(d, "suggestionsBox");
        assertTrue(suggestionsBox.getChildren().size >= 2, "need at least two rows");
        Table row0 = (Table) suggestionsBox.getChildren().get(0);
        Table row1 = (Table) suggestionsBox.getChildren().get(1);

        Drawable hoverBg = (Drawable) get(d, "suggestionHoverBg");

        // Hover only the first row
        boolean hovered = false;
        for (EventListener l : row0.getListeners()) {
            if (l instanceof InputListener il) {
                il.mouseMoved(new InputEvent(), 0f, 0f);
                hovered = true;
                break;
            }
        }
        assertTrue(hovered, "first row should have hover InputListener");

        // Assert: first row got hover bg, second row unchanged
        assertSame(hoverBg, row0.getBackground(), "hover should set bg on hovered row");
        assertNull(row1.getBackground(), "non-hovered row should remain unchanged");
    }

    @Test
    void zIndex_constant() {
        assertEquals(10f, new TerminalDisplay(mock(GdxGame.class)).getZIndex(), 1e-6);
    }
}
