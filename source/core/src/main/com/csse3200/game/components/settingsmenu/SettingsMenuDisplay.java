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
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.files.UserSettings;
import com.csse3200.game.files.UserSettings.DisplaySettings;
import com.csse3200.game.services.ServiceLocator;
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
    private CheckBox musicCheck;

    private static final String STYLE_WHITE = "white";

    public SettingsMenuDisplay(GdxGame game) {
        super();
        this.game = game;
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

    /**
     * Initialises styles and builds the actors.
     */
    @Override
    public void create() {
        super.create();
        addActors();
    }

    /**
     * Builds the title, settings table, and action buttons,
     * and attaches the layout to the stage.
     */
    private void addActors() {
        // Title label
        Label title = new Label("Settings", skin, "title");
        title.setFontScale(1.5f);

        // Build the tables
        Table settingsTable = makeSettingsTable();
        Table menuBtns = makeMenuBtns();

        rootTable = new Table();
        rootTable.setFillParent(true);

        // Title row
        rootTable.add(title).expandX().top().padTop(70f);

        // Settings rows
        rootTable.row();
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

        TextField.TextFieldStyle style = new TextField.TextFieldStyle(skin.get(TextField.TextFieldStyle.class));
        style.font.getData().setScale(1.5f);


        // Create components
        Label fpsLabel = new Label("FPS Cap:", skin, STYLE_WHITE);
        fpsText = new TextField(Integer.toString(settings.fps), style);

        Label fullScreenLabel = new Label("Fullscreen:", skin, STYLE_WHITE);
        fullScreenCheck = new CheckBox("", skin);
        fullScreenCheck.setChecked(settings.fullscreen);
        Label fullScreenState = new Label(settings.fullscreen ? "ON" : "OFF", skin, STYLE_WHITE);
        HorizontalGroup fullScreenCheckGroup = new HorizontalGroup();
        fullScreenCheckGroup.space(15f);
        fullScreenCheckGroup.addActor(fullScreenCheck);
        fullScreenCheckGroup.addActor(fullScreenState);

        Label vsyncLabel = new Label("VSync:", skin, STYLE_WHITE);
        vsyncCheck = new CheckBox("", skin);
        vsyncCheck.setChecked(settings.vsync);
        Label vsyncCheckState = new Label(settings.vsync ? "ON" : "OFF", skin, STYLE_WHITE);
        HorizontalGroup vsyncCheckGroup = new HorizontalGroup();
        vsyncCheckGroup.space(15f);
        vsyncCheckGroup.addActor(vsyncCheck);
        vsyncCheckGroup.addActor(vsyncCheckState);


        Label uiScaleLabel = new Label("ui Scale (Unused):", skin, STYLE_WHITE);
        uiScaleSlider = new Slider(0.2f, 2f, 0.1f, false, skin);
        uiScaleSlider.setValue(settings.uiScale);
        Label uiScaleValue = new Label(String.format("%.2fx", settings.uiScale), skin, STYLE_WHITE);

        Label displayModeLabel = new Label("Resolution:", skin, STYLE_WHITE);
        displayModeSelect = new SelectBox<>(skin);
        Monitor selectedMonitor = Gdx.graphics.getMonitor();
        displayModeSelect.setItems(getDisplayModes(selectedMonitor));
        displayModeSelect.setSelected(getActiveMode(displayModeSelect.getItems()));

        Label musicLabel = new Label("Music:", skin, STYLE_WHITE);
        musicCheck = new CheckBox("", skin);
        musicCheck.setChecked(settings.isMusicEnabled());
        Label musicStateLabel = new Label(musicCheck.isChecked() ? "ON" : "OFF", skin, STYLE_WHITE);
        HorizontalGroup musicCheckGroup = new HorizontalGroup();
        musicCheckGroup.space(15f);
        musicCheckGroup.addActor(musicCheck);
        musicCheckGroup.addActor(musicStateLabel);

        //Enlarge label text
        fpsLabel.setFontScale(2f);
        fullScreenLabel.setFontScale(2f);
        vsyncLabel.setFontScale(2f);
        uiScaleLabel.setFontScale(2f);
        displayModeLabel.setFontScale(2f);
        musicLabel.setFontScale(2f);


        // Layout table
        Table table = new Table();

        table.add(fpsLabel).right().padRight(15f);
        table.add(fpsText).width(100).left();

        table.row().padTop(10f);
        table.add(fullScreenLabel).right().padRight(15f);
        table.add(fullScreenCheckGroup).left();

        table.row().padTop(10f);
        table.add(vsyncLabel).right().padRight(15f);
        table.add(vsyncCheckGroup).left();

        table.row().padTop(10f);
        Table uiScaleTable = new Table();
        uiScaleTable.add(uiScaleSlider).width(150).left();
        uiScaleTable.add(uiScaleValue).left().padLeft(5f).expandX();

        table.add(uiScaleLabel).right().padRight(15f);
        table.add(uiScaleTable).left();

        table.row().padTop(10f);
        table.add(displayModeLabel).right().padRight(15f);
        table.add(displayModeSelect).left();

        table.row().padTop(10f);
        table.add(musicLabel).right().padRight(15f);
        table.add(musicCheckGroup).left();

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
                    musicStateLabel.setText(musicCheck.isChecked() ? "ON" : "OFF");
                    return true;
                });

        fullScreenCheck.addListener(event -> {
            boolean checked = fullScreenCheck.isChecked();
            fullScreenState.setText(checked ? "ON" : "OFF");
            return true;
        });


        vsyncCheck.addListener(event -> {
            boolean checked = vsyncCheck.isChecked();
            fullScreenState.setText(checked ? "ON" : "OFF");
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
        TextButton exitBtn = new TextButton("Exit", skin);
        TextButton applyBtn = new TextButton("Apply", skin);

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
                        ServiceLocator.getButtonSoundService().playClick();
                        logger.debug("Exit button clicked");
                        exitMenu();
                    }
                });

        applyBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        ServiceLocator.getButtonSoundService().playClick();
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
}
