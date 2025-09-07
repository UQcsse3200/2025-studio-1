package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TutorialScreenDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(TutorialScreenDisplay.class);
    private final GdxGame game;
    private final List<TutorialStep> steps;
    private int currentStep = 0;

    private Table table;
    private NeonStyles neon;

    public TutorialScreenDisplay(GdxGame game, List<TutorialStep> steps) {
        super();
        this.game = game;
        this.steps = steps;
    }

    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.7f);
        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        showStep(currentStep);
    }

    private void showStep(int stepIndex) {
        table.clear();
        TutorialStep step = steps.get(stepIndex);

        // Title
        Label titleLabel = new Label(step.getTitle(), skin, "title");
        titleLabel.setFontScale(3f);
        table.add(titleLabel).colspan(2).center().padBottom(20f);
        table.row();

        Label.LabelStyle smallStyle = skin.get("small", Label.LabelStyle.class);
        smallStyle.fontColor = skin.getColor("white");

        // Description
        Label descLabel = new Label(step.getDescription(), skin, "small");
        descLabel.setFontScale(2f);
        table.add(descLabel).colspan(2).center().padBottom(20f);
        table.row();

        TextButton.TextButtonStyle style = neon.buttonRounded();
        TextButton mainMenuBtn = new TextButton("Main Menu", style);
        mainMenuBtn.getLabel().setFontScale(2f);

        mainMenuBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Main Menu button clicked");
                        backMainMenu();
                    }
                });
        table.add(mainMenuBtn).left();

    }

    private void backMainMenu() {
        logger.debug("Switching to Main Menu screen");
        game.setScreen(ScreenType.MAIN_MENU);
    }

    @Override
    public void draw(SpriteBatch batch) {
        // stage handles rendering
    }

    @Override
    public float getZIndex() {
        return 2f;
    }

    @Override
    public void dispose() {
        table.clear();
        super.dispose();
    }
}