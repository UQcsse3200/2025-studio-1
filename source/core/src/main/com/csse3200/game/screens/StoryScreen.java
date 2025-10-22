package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;

/**
 * A story screen shown before the gam starts
 * <p>
 *     Displays multiple lines of story text with a typing effect
 *     and allows the player to progress through the dialogue or skip it entirely.
 * </p>
 * <h3>Controls:</h3>
 *  <ul>
 *  <li><b>SPACE</b> — Advance dialogue or reveal the full line</li>
 *  <li><b>ESC</b> — Skip story and start the game immediately</li>
 *  </ul>
 */
public class StoryScreen extends ScreenAdapter {

    /** Reference to the main game instance*/
    private final GdxGame game;

    /** Narrative text lines displayed sequentially*/
    private final String[] storyLines = {
            "In the not so far future, robotic companions have become a fully realised reality.",
            "Ranging from simple janitors to full blown military application - their use is seen on a global scale.",
            "A manufacturing plant heavily funded by the government has been conducting research to further the " +
                    "endless possibilities of the artificial intelligence within these machines.",
            "However, this suddenly halts due to rogue behaviours within their newest model.",
            "With the facility now in lockdown and eager beneficiaries breathing down their neck,",
            "a lone operative has infiltrated the plant and must now navigate its chaotic twists and turns to shut" +
                    " down the artificial menace."
    };

    /** Time delay between each character for the typing effect. */
    private final float typeSpeed = 0.03f;

    /** LibGDX Stage for managing actors and UI layout. */
    private Stage stage;

    /** Skin used for UI styling. */
    private Skin skin;

    /** Label used to display story text. */
    private Label dialogueLabel;

    /** Background texture image. */
    private Texture bgTexture;

    /** Index of the current story line being displayed. */
    private int currentLine = 0;

    /** Timer used to control character typing speed. */
    private float typeTimer = 0f;

    /** Index of the next character to display in the current line. */
    private int charIndex = 0;

    /** Flag indicating if the typing animation is in progress. */
    private boolean typing;

    /**
     * Constructs a new Story Screen
     * @param game the {@link GdxGame} instance used for screen transitions
     */
    public StoryScreen(GdxGame game) {
        this.game = game;
    }

    /**
     * Initialises the {@link Stage}, UI skin, background, and dialogue labels.
     * Begins the story by typing out the first line.
     */
    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        // Background
        bgTexture = new Texture(Gdx.files.internal("images/menu_background.png"));
        Image background = new Image(bgTexture);
        background.setFillParent(true);
        stage.addActor(background);
        // Dialogue
        dialogueLabel = new Label("", skin);
        dialogueLabel.setWrap(true);
        dialogueLabel.setAlignment(Align.center);
        dialogueLabel.setFontScale(1.5f);
        // Input indicators
        Label spaceIndicator = new Label("SPACE → Next", skin);
        Label escIndicator = new Label("ESC → Skip", skin);
        Table indicatorsTable = new Table();
        indicatorsTable.setFillParent(true);
        indicatorsTable.top().left().pad(10);
        indicatorsTable.add(spaceIndicator).left().row();
        indicatorsTable.add(escIndicator).left();
        stage.addActor(indicatorsTable);
        // Center dialogue
        Table dialogueTable = new Table();
        dialogueTable.setFillParent(true);
        dialogueTable.center();
        dialogueTable.add(dialogueLabel).width(800).center();
        stage.addActor(dialogueTable);

        startTypingLine();
    }

    /**
     * Prepares to type out the current line of dialogue
     */
    private void startTypingLine() {
        charIndex = 0;
        typing = true;
        dialogueLabel.setText("");
    }

    /**
     * Handles progressing the story dialogue
     */
    private void advanceDialogue() {
        if (typing) {
            dialogueLabel.setText(storyLines[currentLine]);
            typing = false;
        } else {
            currentLine++;
            if (currentLine < storyLines.length) {
                startTypingLine();
            } else {
                endStory();
            }
        }
    }

    /**
     * Ends the story and switches to the main game
     */
    private void endStory() {
        game.setScreen(GdxGame.ScreenType.MAIN_GAME);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Typing effect
        if (typing) {
            typeTimer += delta;
            if (typeTimer >= typeSpeed) {
                typeTimer = 0f;
                charIndex++;
                if (charIndex > storyLines[currentLine].length()) {
                    charIndex = storyLines[currentLine].length();
                    typing = false;
                }
                dialogueLabel.setText(storyLines[currentLine].substring(0, charIndex));
            }
        }
        // Input handling
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            advanceDialogue();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            endStory();
        }
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
        bgTexture.dispose();
    }
}
