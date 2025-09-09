package com.csse3200.game.components.screens;

import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.AnimatedClipImage;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TutorialScreenDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(TutorialScreenDisplay.class);
    private final GdxGame game;
    private final List<TutorialStep> steps;
    private int currentStep = 0;

    private Table table;
    private NeonStyles neon;

    private final Set<String> loadedFramePaths = new HashSet<>();


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

        // Animation
        if (step.getClip() != null) {
            try {
                AnimatedClipImage anim = new AnimatedClipImage(step.getClip());
                anim.setScaling(Scaling.fit);
                anim.setSize(800f, 450f);
                table.add(anim).colspan(2).center().padBottom(20f);
                table.row();
            } catch (Exception ex) {
                logger.error("Failed to load tutorial clip", ex);
                table.add(new Label("Demo unavailable", skin)).colspan(2).center().padBottom(20f);
                table.row();
            }
        }

        // Buttons
        TextButton.TextButtonStyle style = neon.buttonRounded();
        TextButton nextBtn = new TextButton("Next", style);
        TextButton prevBtn = new TextButton("Previous", style);
        TextButton mainMenuBtn = new TextButton("Main Menu", style);

        nextBtn.getLabel().setFontScale(2f);
        prevBtn.getLabel().setFontScale(2f);
        mainMenuBtn.getLabel().setFontScale(2.0f);

        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep < steps.size() - 1) {
                    currentStep++;
                    showStep(currentStep);
                }
            }
        });

        prevBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep > 0) {
                    currentStep--;
                    showStep(currentStep);
                }
            }
        });

        mainMenuBtn.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent changeEvent, Actor actor) {
                        logger.debug("Main Menu button clicked");
                        backMainMenu();
                    }
                });

        if (currentStep > 0) {
            table.add(prevBtn).left();
        }
        if (currentStep < steps.size() - 1) {
            table.add(nextBtn).left();
        }
        table.row();
        table.add(mainMenuBtn).colspan(2).center().padTop(20f);

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
        if (!loadedFramePaths.isEmpty()) {
            ServiceLocator.getResourceService()
                    .unloadAssets(loadedFramePaths.toArray(new String[0]));
            loadedFramePaths.clear();
        }
        table.clear();
        super.dispose();
    }
}