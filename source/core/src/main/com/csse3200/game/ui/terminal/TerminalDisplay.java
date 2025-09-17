package com.csse3200.game.ui.terminal;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.csse3200.game.components.screens.BaseScreenDisplay;
import com.csse3200.game.GdxGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TerminalDisplay extends BaseScreenDisplay {
    private static final float Z_INDEX = 10f;

    // Layout
    private static final float WIDTH_RATIO = 0.20f; // 20% of screen width
    private static final float H_PADDING   = 16f;
    private static final float V_PADDING   = 10f;
    private static final float MARGIN_BOT  = 8f;
    private static final float SUGGESTION_ROW_PAD_X = 10f;
    private static final float SUGGESTION_ROW_PAD_Y = 6f;
    private static final int   SUGGESTION_MAX = 5;
    private final Vector2 tmp = new Vector2();

    private Table container;
    private Table promptBox;
    private Label label;
    private Label.LabelStyle promptLabelStyle;
    private Label.LabelStyle suggestionLabelStyle;

    // Suggestions popup
    private Table suggestionsBox;
    private Drawable suggestionsBg;
    private Drawable suggestionHoverBg;
    private List<String> lastShown = new ArrayList<>(SUGGESTION_MAX);

    private Terminal terminal;

    public TerminalDisplay(GdxGame game) {
        super(game);
    }

    @Override
    public void create() {
        super.create();
        terminal = entity.getComponent(Terminal.class);
        if (terminal == null) {
            throw new IllegalStateException("Terminal component is required for TerminalDisplay");
        }
    }

    @Override
    protected void buildUI(Table root) {
        // root may be shared; mount our own container
        container = new Table();
        container.setFillParent(true);
        container.align(Align.bottomLeft);
        container.setTouchable(Touchable.disabled);
        stage.addActor(container);

        // prompt background
        promptBox = new Table();
        promptBox.align(Align.left);
        promptBox.setClip(true);

        Drawable bg = null;
        if (skin.has("textfield", Drawable.class)) bg = skin.getDrawable("textfield");
        else if (skin.has("rounded", Drawable.class)) bg = skin.getDrawable("rounded");
        else if (skin.has("button", Drawable.class)) bg = skin.getDrawable("button");
        if (bg == null) bg = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f));
        promptBox.setBackground(bg);

        promptLabelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));

        suggestionLabelStyle = new Label.LabelStyle(promptLabelStyle);
        suggestionLabelStyle.fontColor = Color.WHITE;

        // label: single line + ellipsis
        label = new Label("> ", promptLabelStyle);   // ← use white style
        label.setAlignment(Align.left);
        label.setWrap(false);
        label.setEllipsis(true);
        promptBox.setClip(true);


        promptBox.add(label)
                .minWidth(0f)
                .growX()
                .padLeft(H_PADDING)
                .padRight(H_PADDING)
                .padTop(V_PADDING)
                .padBottom(V_PADDING);

        // width = 20% of container (screen) width; auto-updates on resize
        container.add(promptBox)
                .width(Value.percentWidth(WIDTH_RATIO, container))
                .left()
                .padBottom(MARGIN_BOT)
                .padLeft(H_PADDING * 0.75f);
        container.row();

        // suggestions popup (initially hidden)
        suggestionsBox = new Table();
        suggestionsBox.align(Align.bottomLeft);
        suggestionsBox.setTouchable(Touchable.enabled);
        suggestionsBg = skin.newDrawable("white", new Color(0f, 0f, 0f, 0.75f));
        suggestionHoverBg = skin.newDrawable("white", new Color(1f, 1f, 1f, 0.10f));
        suggestionsBox.setBackground(suggestionsBg);
        suggestionsBox.setVisible(false);

        // Anchor suggestions directly above the promptBox by placing it in the same column
        stage.addActor(suggestionsBox);

        container.setVisible(false);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (terminal == null) return;

        boolean open = terminal.isOpen();
        if (container != null) {
            container.setVisible(open);
            if (open) container.toFront();
        }
        if (!open) return;

        // update prompt text
        String message = terminal.getEnteredMessage();
        label.setText("> " + (message == null ? "" : message));

        // update suggestions popup
        List<String> suggestions = terminal.getAutocompleteSuggestions();
        // show only when we have a non-empty first token and suggestions exist
        String prefix = extractFirstToken(message);
        boolean show = prefix != null && !prefix.isEmpty() && suggestions != null && !suggestions.isEmpty();

        if (show) {
            if (!sameList(suggestions, lastShown)) {
                rebuildSuggestionRows(suggestions);
                lastShown = new ArrayList<>(suggestions);
                suggestionsBox.pack(); // compute pref size for current rows
            }

            // Position just above the promptBox, same width
            tmp.set(0f, promptBox.getHeight());
            promptBox.localToStageCoordinates(tmp);

            suggestionsBox.setSize(promptBox.getWidth(), suggestionsBox.getPrefHeight());
            suggestionsBox.setPosition(tmp.x, tmp.y + 4f); // small gap above the prompt
            suggestionsBox.setVisible(true);

            suggestionsBox.toFront();
            promptBox.toFront();
        } else {
            suggestionsBox.setVisible(false);
            lastShown.clear();
        }
    }

    @Override
    public float getZIndex() {
        return Z_INDEX;
    }

    // ---------- helpers ----------

    private void rebuildSuggestionRows(List<String> items) {
        suggestionsBox.clearChildren();

        int count = Math.min(SUGGESTION_MAX, items.size());
        for (int i = 0; i < count; i++) {
            final String s = items.get(i);

            // Row container so we can set hover background easily
            final Table row = new Table();
            final Label l = new Label(s, suggestionLabelStyle);
            l.setAlignment(Align.left);

            row.add(l).left().growX()
                    .padLeft(SUGGESTION_ROW_PAD_X)
                    .padRight(SUGGESTION_ROW_PAD_X)
                    .padTop(SUGGESTION_ROW_PAD_Y)
                    .padBottom(SUGGESTION_ROW_PAD_Y);

            // Hover + click behaviour
            row.addListener(new InputListener() {
                @Override
                public boolean mouseMoved (InputEvent event, float x, float y) {
                    row.setBackground(suggestionHoverBg);
                    return false;
                }
                @Override
                public void exit (InputEvent event, float x, float y, int pointer, Actor toActor) {
                    row.setBackground((Drawable) null);
                }
            });
            row.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    acceptSuggestion(s);
                }
            });

            suggestionsBox.add(row).growX();
            suggestionsBox.row();
        }
    }

    private void acceptSuggestion(String suggestion) {
        // Replace only the first token of the entered text with the suggestion
        String current = terminal.getEnteredMessage();
        String rest = stripFirstToken(current);
        terminal.setEnteredMessage(suggestion + rest);
        // Optional: hide popup immediately
        suggestionsBox.setVisible(false);
        lastShown.clear();
    }

    private static boolean sameList(List<String> a, List<String> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            if (!Objects.equals(a.get(i), b.get(i))) return false;
        }
        return true;
    }

    private static String extractFirstToken(String s) {
        if (s == null) return "";
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        int start = i;
        while (i < s.length() && !Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(start, i);
    }

    private static String stripFirstToken(String s) {
        if (s == null) return "";
        int i = 0;
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        while (i < s.length() && !Character.isWhitespace(s.charAt(i))) i++;
        return s.substring(i); // keeps trailing part (incl. whitespace)
    }
}
