package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;

import com.csse3200.game.GdxGame;

/**
 * Configurable, shared end-of-run screen for both Victory and Defeated outcomes.
 * <p>
 * Centralises common layout and behaviour:
 * <ul>
 *   <li>Title (text + colour)</li>
 *   <li>Round and elapsed-time labels</li>
 *   <li>Primary/secondary action buttons</li>
 * </ul>
 * Consumers provide configuration via the constructor (texts, colours, actions),
 * or use the convenience factory methods {@link #victory(GdxGame)} and
 * {@link #defeated(GdxGame)}.
 * <p>
 * This class extends {@link BaseScreenDisplay} for shared UI helpers and lifecycle.
 */
public class BaseEndScreenDisplays extends BaseScreenDisplay {
    /** Title text displayed at the top (e.g., "Victory", "DEFEATED"). */
    private final String titleText;
    /** Colour applied to the title text. */
    private final Color  titleColor;
    /** Label for the primary action button (e.g., "Continue", "Try Again"). */
    private final String primaryText;
    /** Behaviour executed when the primary action is invoked. */
    private final Runnable primaryAction;
    /** Label for the secondary action button (defaults to "Main Menu"). */
    private final String secondaryText;
    /** Behaviour executed when the secondary action is invoked. */
    private final Runnable secondaryAction;

    /** Reference to the round label for runtime updates. */
    private Label roundLabelRef;
    /** Reference to the time label for runtime updates. */
    private Label timeLabelRef;

    /**
     * Constructs a new end-of-run display with the given configuration.
     *
     * @param game            game instance used for screen navigation
     * @param titleText       title text (e.g., "Victory", "DEFEATED")
     * @param titleColor      colour to apply to the title
     * @param primaryText     label for the primary button
     * @param primaryAction   action executed on primary button press
     * @param secondaryAction action executed on secondary button press; if {@code null},
     *                        defaults to {@link #backMainMenu()}
     */
    protected BaseEndScreenDisplays(
            GdxGame game,
            String titleText,
            Color titleColor,
            String primaryText,
            Runnable primaryAction,
            Runnable secondaryAction
    ) {
        super(game);
        this.titleText = titleText;
        this.titleColor = titleColor;
        this.primaryText = primaryText;
        this.primaryAction = primaryAction;
        this.secondaryText = "Main Menu";
        this.secondaryAction = (secondaryAction != null) ? secondaryAction : this::backMainMenu;
    }

    /**
     * Builds the end-screen UI: title, round/time labels, and the action buttons.
     * <p>
     * Appearance can be tuned by overriding the styling hooks:
     * {@link #titleFontScale()}, {@link #infoFontScale()},
     * {@link #buttonLabelScale()}, {@link #buttonsGap()}, {@link #blockPad()}.
     *
     * @param root the root table (already added to the Stage by {@link BaseScreenDisplay})
     */
    @Override
    protected void buildUI(Table root) {
        // Title
        addTitle(root, titleText, titleFontScale(), titleColor, blockPad());

        // Round
        roundLabelRef = addBody(root, "Round: 1", infoFontScale(), blockPad());

        // Time
        timeLabelRef = addBody(root, "Time: 00:00", infoFontScale(), blockPad());

        // Buttons row
        Table row = new Table();
        TextButton primary = button(primaryText, buttonLabelScale(), primaryAction);
        TextButton secondary = button(secondaryText, buttonLabelScale(), secondaryAction);
        row.add(primary).left().padRight(buttonsGap());
        row.add(secondary).left();
        root.add(row).colspan(2).center();
    }

    // --- Runtime updates ---

    /**
     * Updates the displayed round number.
     *
     * @param round the current round (values &lt; 1 are clamped to 1)
     */
    public void setRound(int round) {
        if (roundLabelRef != null) roundLabelRef.setText("Round: " + Math.max(1, round));
    }

    /**
     * Updates the elapsed time label using total seconds, formatted as {@code mm:ss}.
     *
     * @param seconds total elapsed seconds (negative values are clamped to 0)
     */
    public void setElapsedSeconds(long seconds) {
        if (timeLabelRef != null) timeLabelRef.setText("Time: " + toMMSS(Math.max(0, seconds)));
    }

    /**
     * Updates the elapsed time label with a preformatted string (e.g., {@code "12:34"}).
     *
     * @param mmss elapsed time string in {@code mm:ss} format
     */
    public void setElapsedText(String mmss) {
        if (timeLabelRef != null) timeLabelRef.setText("Time: " + mmss);
    }

    // --- Styling hooks (override as needed) ---

    /**
     * Font scale for the title label.
     *
     * @return title font scale (default {@code 3.0f})
     */
    protected float titleFontScale() { return 3.0f; }

    /**
     * Font scale for informational labels (round/time).
     *
     * @return info label font scale (default {@code 3.0f})
     */
    protected float infoFontScale() { return 3.0f; }

    /**
     * Font scale for button labels.
     *
     * @return button label font scale (default {@code 2.0f})
     */
    protected float buttonLabelScale() { return 2.0f; }

    /**
     * Horizontal gap between the primary and secondary buttons.
     *
     * @return gap in pixels (default {@code 30f})
     */
    protected float buttonsGap() { return 30f; }

    /**
     * Vertical padding applied beneath title and info blocks.
     *
     * @return padding in pixels (default {@code 50f})
     */
    protected float blockPad() { return 50f; }

    /**
     * Formats seconds into {@code mm:ss}.
     *
     * @param totalSeconds total seconds to format
     * @return a string formatted as {@code mm:ss}
     */
    private static String toMMSS(long totalSeconds) {
        long m = totalSeconds / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d", m, s);
    }

    // --- Factories ---

    /**
     * Convenience factory for a Victory end screen.
     *
     * @param game game instance for navigation
     * @return a configured {@code BaseEndScreenDisplays} showing "Victory" with a "Continue" action
     */
    public static BaseEndScreenDisplays victory(GdxGame game) {
        return new BaseEndScreenDisplays(
                game, "Victory", new Color(0f, 1f, 0f, 1f), "Continue",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME), null);
    }

    /**
     * Convenience factory for a Defeated end screen.
     *
     * @param game game instance for navigation
     * @return a configured {@code BaseEndScreenDisplays} showing "DEFEATED" with a "Try Again" action
     */
    public static BaseEndScreenDisplays defeated(GdxGame game) {
        return new BaseEndScreenDisplays(
                game, "DEFEATED", new Color(1f, 0f, 0f, 1f), "Try Again",
                () -> game.setScreen(GdxGame.ScreenType.MAIN_GAME), null);
    }
}
