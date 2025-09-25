package com.csse3200.game.components.settingsmenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.files.UserSettings.DisplaySettings;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.utils.StringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Settings menu display and logic. If you bork the settings, they can be changed manually in
 * CSSE3200Game/settings.json under your home directory (This is C:/users/[username] on Windows).
 */
public class SettingsMenuDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(SettingsMenuDisplay.class);
    private final GdxGame game;

    private Table rootTable;
    private TextField fpsText;
    private CheckBox fullScreenCheck;
    private CheckBox vsyncCheck;
    private Slider uiScaleSlider;
    private SelectBox<StringDecorator<DisplayMode>> displayModeSelect;
    private NeonStyles neon;
    private CheckBox musicCheck;

    public SettingsMenuDisplay(GdxGame game) {
        super();
        this.game = game;
    }

    /**
     * Initialises styles and builds the actors.
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        addActors();
    }

    /**
     * Builds the title, settings table, and action buttons,
     * and attaches the layout to the stage.
     */
    private void addActors() {
        // Title label
        Label title = new Label("Settings", skin, "title");
        Label.LabelStyle titleStyle = new Label.LabelStyle(title.getStyle());
        titleStyle.fontColor = new com.badlogic.gdx.graphics.Color(0f, 0.95f, 1f, 1f);
        title.setStyle(titleStyle);

        title.setFontScale(1.20f);

        // Build the tables
        Table settingsTable = makeSettingsTable();
        Table menuBtns = makeMenuBtns();

        rootTable = new Table();
        rootTable.setFillParent(true);

        // Title row
        rootTable.add(title).expandX().top().padTop(30f);

        // Settings rows
        rootTable.row().padTop(30f);
        rootTable.add(settingsTable).expandX().expandY();

        // Buttons
        rootTable.row();
        rootTable.add(menuBtns).center().padBottom(80f);

        stage.addActor(rootTable);
    }

    /**
     * Creates the settings controls, applies styling, binds live-updating labels,
     * and returns a table ready to be placed in the layout.
     */
    private Table makeSettingsTable() {
        // Get current values
        UserSettings.Settings settings = UserSettings.get();

        // Create components
        Label fpsLabel = new Label("FPS Cap:", skin);
        fpsText = new TextField(Integer.toString(settings.fps), skin);

        Label fullScreenLabel = new Label("Fullscreen:", skin);
        fullScreenCheck = new CheckBox("", skin);
        fullScreenCheck.setChecked(settings.fullscreen);

        Label vsyncLabel = new Label("VSync:", skin);
        vsyncCheck = new CheckBox("", skin);
        vsyncCheck.setChecked(settings.vsync);

        Label uiScaleLabel = new Label("ui Scale (Unused):", skin);
        uiScaleSlider = new Slider(0.2f, 2f, 0.1f, false, skin);
        uiScaleSlider.setValue(settings.uiScale);
        Label uiScaleValue = new Label(String.format("%.2fx", settings.uiScale), skin);

        Label displayModeLabel = new Label("Resolution:", skin);
        displayModeSelect = new SelectBox<>(skin);
        Monitor selectedMonitor = Gdx.graphics.getMonitor();
        displayModeSelect.setItems(getDisplayModes(selectedMonitor));
        displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));

        Label musicLabel = new Label("Music:", skin);
        musicCheck = new CheckBox("", skin);
        musicCheck.setChecked(settings.isMusicEnabled());

        // White labels
        makeWhite(
                fpsLabel,
                fullScreenLabel,
                vsyncLabel,
                uiScaleLabel,
                uiScaleValue,
                displayModeLabel,
                musicLabel
        );

        // TextField style
        {
            TextField.TextFieldStyle tf = new TextField.TextFieldStyle(fpsText.getStyle());
            tf.fontColor = Color.WHITE;
            tf.focusedFontColor = Color.WHITE;
            tf.messageFontColor = new Color(1f, 1f, 1f, 0.6f);
            if (tf.cursor != null) tf.cursor = skin.newDrawable(tf.cursor, Color.WHITE);
            if (tf.selection != null) tf.selection = skin.newDrawable(tf.selection, new Color(1f, 1f, 1f, 0.25f));
            if (tf.background != null) tf.background = skin.newDrawable(tf.background, new Color(1f, 1f, 1f, 0.15f));
            fpsText.setStyle(tf);
        }
        logger.debug("TextField styled");

        // CheckBox style
        {
            CheckBox.CheckBoxStyle cb = new CheckBox.CheckBoxStyle(fullScreenCheck.getStyle());
            cb.fontColor = Color.WHITE;
            if (cb.checkboxOn != null) cb.checkboxOn = skin.newDrawable(cb.checkboxOn, Color.WHITE);
            if (cb.checkboxOff != null) cb.checkboxOff = skin.newDrawable(cb.checkboxOff, new Color(1f, 1f, 1f, 0.35f));
            if (cb.checkboxOver != null) cb.checkboxOver = skin.newDrawable(cb.checkboxOver, Color.WHITE);
            fullScreenCheck.setStyle(cb);
            vsyncCheck.setStyle(cb);
            musicCheck.setStyle(cb);
        }
        logger.debug("CheckBox styled");

        // Slider style
        {
            Slider.SliderStyle ss = new Slider.SliderStyle(uiScaleSlider.getStyle());

            if (ss.background != null) ss.background = skin.newDrawable(ss.background, new Color(1f, 1f, 1f, 0.25f));
            if (ss.knobBefore != null) ss.knobBefore = skin.newDrawable(ss.knobBefore, new Color(1f, 1f, 1f, 0.35f));
            if (ss.knobAfter != null) ss.knobAfter = skin.newDrawable(ss.knobAfter, new Color(1f, 1f, 1f, 0.15f));

            final Drawable plainKnob =
                    ss.knob != null ? skin.newDrawable(ss.knob, Color.WHITE) : null;

            ss.knob = plainKnob;
            ss.knobOver = plainKnob;
            ss.knobDown = plainKnob;

            uiScaleSlider.setStyle(ss);
            uiScaleSlider.invalidateHierarchy();
        }
        logger.debug("Slider styled");

        // SelectBox style
        {
            Drawable tfBg = fpsText.getStyle().background;

            SelectBox.SelectBoxStyle sb = new SelectBox.SelectBoxStyle(displayModeSelect.getStyle());
            sb.fontColor = Color.WHITE;

            // Closed state backgrounds
            if (tfBg != null) {
                sb.background = skin.newDrawable(tfBg, new Color(1f, 1f, 1f, 0.15f));
                sb.backgroundOver = skin.newDrawable(tfBg, new Color(1f, 1f, 1f, 0.25f));
                sb.backgroundOpen = skin.newDrawable(tfBg, new Color(1f, 1f, 1f, 0.25f));
                sb.backgroundDisabled = skin.newDrawable(tfBg, new Color(1f, 1f, 1f, 0.10f));
            }
            logger.debug("SelectBox styled");

            // Dropdown list
            List.ListStyle ls = new List.ListStyle(sb.listStyle);
            ls.fontColorSelected = Color.WHITE;
            ls.fontColorUnselected = Color.WHITE;
            if (ls.selection != null) ls.selection = skin.newDrawable(ls.selection, new Color(1f, 1f, 1f, 0.15f));
            if (ls.background != null) ls.background = skin.newDrawable(ls.background, new Color(1f, 1f, 1f, 0.08f));
            else if (tfBg != null) ls.background = skin.newDrawable(tfBg, new Color(1f, 1f, 1f, 0.08f));
            sb.listStyle = ls;
            logger.debug("Dropdown list styled");

            // ScrollPane inside the dropdown
            if (sb.scrollStyle != null) {
                ScrollPane.ScrollPaneStyle sp = new ScrollPane.ScrollPaneStyle(sb.scrollStyle);
                logger.debug("Dropdown scroll styled");
                if (sp.background != null)
                    sp.background = skin.newDrawable(sp.background, new Color(1f, 1f, 1f, 0.05f));
                if (sp.vScrollKnob != null) sp.vScrollKnob = skin.newDrawable(sp.vScrollKnob, Color.WHITE);
                if (sp.vScroll != null) sp.vScroll = skin.newDrawable(sp.vScroll, new Color(1f, 1f, 1f, 0.15f));
                if (sp.hScrollKnob != null) sp.hScrollKnob = skin.newDrawable(sp.hScrollKnob, Color.WHITE);
                if (sp.hScroll != null) sp.hScroll = skin.newDrawable(sp.hScroll, new Color(1f, 1f, 1f, 0.15f));
                sb.scrollStyle = sp;
            }
            displayModeSelect.setStyle(sb);
            displayModeSelect.invalidateHierarchy();
        }


        // Layout table
        Table table = new Table();

        table.add(fpsLabel).right().padRight(15f);
        table.add(fpsText).width(100).left();

        table.row().padTop(10f);
        table.add(fullScreenLabel).right().padRight(15f);
        table.add(fullScreenCheck).left();

        table.row().padTop(10f);
        table.add(vsyncLabel).right().padRight(15f);
        table.add(vsyncCheck).left();

        table.row().padTop(10f);
        Table uiScaleTable = new Table();
        uiScaleTable.add(uiScaleSlider).width(100).left();
        uiScaleTable.add(uiScaleValue).left().padLeft(5f).expandX();

        table.add(uiScaleLabel).right().padRight(15f);
        table.add(uiScaleTable).left();

        table.row().padTop(10f);
        table.add(displayModeLabel).right().padRight(15f);
        table.add(displayModeSelect).left();

        table.row().padTop(10f);
        table.add(musicLabel).right().padRight(15f);
        table.add(musicCheck).left();

        // Events on inputs
        uiScaleSlider.addListener(
                (Event event) -> {
                    float value = uiScaleSlider.getValue();
                    uiScaleValue.setText(String.format("%.2fx", value));
                    return true;
                });

        musicCheck.addListener(
                (Event event) -> {
                    settings.setMusicEnabled(musicCheck.isChecked());
                    UserSettings.set(settings, true);
                    return true;
                });

        return table;
    }

    /**
     * Returns the display mode from the provided list that matches the current system mode,
     * or null if no match is found.
     */
    private StringDecorator<DisplayMode> getActiveMode(Array<StringDecorator<DisplayMode>> modes) {
        DisplayMode active = Gdx.graphics.getDisplayMode();

        for (StringDecorator<DisplayMode> stringMode : modes) {
            DisplayMode mode = stringMode.object;
            if (active.width == mode.width
                    && active.height == mode.height
                    && active.refreshRate == mode.refreshRate) {
                return stringMode;
            }
        }
        return null;
    }

    /**
     * Returns all display modes for the selected monitor, wrapped for pretty printing.
     */
    private Array<StringDecorator<DisplayMode>> getDisplayModes(Monitor monitor) {
        DisplayMode[] displayModes = Gdx.graphics.getDisplayModes(monitor);
        Array<StringDecorator<DisplayMode>> arr = new Array<>();

        for (DisplayMode displayMode : displayModes) {
            arr.add(new StringDecorator<>(displayMode, this::prettyPrint));
        }

        return arr;
    }

    /**
     * Formats a display mode as a concise resolution and refresh-rate string.
     */
    private String prettyPrint(DisplayMode displayMode) {
        return displayMode.width + "x" + displayMode.height + ", " + displayMode.refreshRate + "hz";
    }

    /**
     * Creates the buttons (Exit, Apply) with the neon style and wires
     * their change listeners.
     */
    private Table makeMenuBtns() {
        TextButton.TextButtonStyle style = neon.buttonRounded();

        TextButton exitBtn = new TextButton("Exit", style);
        TextButton applyBtn = new TextButton("Apply", style);

        // Label text size
        exitBtn.getLabel().setFontScale(1.5f);
        applyBtn.getLabel().setFontScale(1.5f);

        // Button sizing relative to screen
        float btnW = stage.getWidth() * 0.10f;
        float btnH = Math.max(50f, stage.getHeight() * 0.06f);
        float gap = 30f;

        Table table = new Table();
        table.center();
        table.add(exitBtn).width(btnW).height(btnH).padRight(gap);
        table.add(applyBtn).width(btnW).height(btnH).padLeft(gap);

        // Button actions
        exitBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Exit button clicked");
                        exitMenu();
                    }
                });

        applyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Apply button clicked");
                        applyChanges();
                    }
                });

        return table;
    }

    /**
     * Reads values from the UI controls and writes them to {@code UserSettings},
     * persisting the changes.
     */
    private void applyChanges() {
        UserSettings.Settings settings = UserSettings.get();

        Integer fpsVal = parseOrNull(fpsText.getText());
        if (fpsVal != null) {
            settings.fps = fpsVal;
        }
        settings.fullscreen = fullScreenCheck.isChecked();
        settings.uiScale = uiScaleSlider.getValue();
        settings.displayMode = new DisplaySettings(displayModeSelect.getSelected().object);
        settings.vsync = vsyncCheck.isChecked();
        settings.setMusicEnabled(musicCheck.isChecked());

        UserSettings.set(settings, true);
        ServiceLocator.getMusicService().setMenuMusicPlaying(musicCheck.isChecked());
    }

    /**
     * Leaves the Settings screen and returns to the main menu.
     */
    private void exitMenu() {
        logger.debug("Switching to Main Menu screen");
        game.setScreen(ScreenType.MAIN_MENU);
    }

    /**
     * Parses an integer from a string or returns null if parsing fails.
     */
    private Integer parseOrNull(String num) {
        try {
            return Integer.parseInt(num, 10);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        // draw is handled by the stage
    }

    /**
     * Ticks the stage so UI animations and input events are processed.
     */
    @Override
    public void update() {
        stage.act(ServiceLocator.getTimeSource().getDeltaTime());
    }

    /**
     * Removes and clears the root table.
     */
    @Override
    public void dispose() {
        rootTable.clear();
        super.dispose();
    }

    /**
     * Sets the provided labels' font colour to white by cloning their styles.
     */
    private static void makeWhite(Label... labels) {
        for (Label l : labels) {
            Label.LabelStyle st = new Label.LabelStyle(l.getStyle());
            st.fontColor = Color.WHITE;
            l.setStyle(st);
            logger.debug("Label styled to white");
        }
    }
}
