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

public class StoryScreen extends ScreenAdapter {
    private final GdxGame game;
    private Stage stage;
    private Skin skin;
    private Label dialogueLabel;
    private Texture bgTexture;

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

    private int currentLine = 0;
    private float typeSpeed = 0.03f;
    private float typeTimer = 0f;
    private int charIndex = 0;
    private boolean typing;

    public StoryScreen(GdxGame game) {
        this.game = game;
    }
    
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
