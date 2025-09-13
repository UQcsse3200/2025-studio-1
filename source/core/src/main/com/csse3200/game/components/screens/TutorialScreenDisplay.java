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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An ui component for displaying the tutorial screen.
 */
public class TutorialScreenDisplay extends UIComponent {
    private static final Logger logger = LoggerFactory.getLogger(TutorialScreenDisplay.class);
    private final GdxGame game;
    private final List<TutorialStep> steps;
    public int currentStep = 0;

    private Table table;
    private NeonStyles neon;

    private final Set<String> loadedFramePaths = new HashSet<>();

    /**
     * Creates a tutorial display bound to a list of steps.
     */
    public TutorialScreenDisplay(GdxGame game, List<TutorialStep> steps) {
        super();
        this.game = game;
        this.steps = steps;
        logger.debug("TutorialScreenDisplay created with {} steps", steps.size());
    }

    /**
     * Builds the root table and shows the first step.
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.7f);
        table = new Table();
        table.setFillParent(true);
        table.center();
        stage.addActor(table);
        logger.debug("UI table created and added to stage");
        showStep(currentStep);
    }

    /**
     * Rebuilds the UI for the given step index.
     * Clears the table, then adds title, text, optional animation, and controls.
     */
    void showStep(int stepIndex) {
        logger.debug("Showing step {} of {}", stepIndex + 1, steps.size());
        table.clear();

        TutorialStep step = steps.get(stepIndex);

        // Title
        Label titleLabel = new Label(step.getTitle(), skin, "title");
        titleLabel.setFontScale(3f);
        table.add(titleLabel).colspan(2).center().padBottom(20f);
        table.row();
        logger.debug("Title added to table: {}", step.getTitle());

        Label.LabelStyle smallStyle = skin.get("small", Label.LabelStyle.class);
        smallStyle.fontColor = skin.getColor("white");

        // Description
        Label descLabel = new Label(step.getDescription(), skin, "small");
        descLabel.setFontScale(2f);
        table.add(descLabel).colspan(2).center().padBottom(20f);
        table.row();
        logger.debug("Description added to table: {}", step.getDescription());

        Table animRow = new Table();
        animRow.center();

        // Buttons texture
        Texture nextBtnTexture = new Texture("images/arrow-right.png");
        ImageButton nextBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(nextBtnTexture)));
        logger.debug("Next button created");

        Texture prevBtnTexture = new Texture("images/arrow-left.png");
        ImageButton prevBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(prevBtnTexture)));
        logger.debug("Previos button created");

        // Animation
        AnimatedClipImage anim = null;
        if (step.getClip() != null) {
            try {
                anim = new AnimatedClipImage(step.getClip());
                anim.setScaling(Scaling.fit);
                anim.setSize(800f, 450f);
                logger.debug("Animation clip loaded for step{}", stepIndex + 1);
            } catch (Exception ex) {
                logger.error("Failed to load tutorial clip", ex);
                table.add(new Label("Demo unavailable", skin)).colspan(2).center().padBottom(20f);
                table.row();
            }
        }

        animRow.add(prevBtn).size(108f, 108f).padRight(10f);
        animRow.add(anim).size(1100f, 600f).padLeft(10f).padRight(10f);
        animRow.add(nextBtn).size(108f, 108f).padLeft(10f);

        prevBtn.setVisible(currentStep > 0);
        nextBtn.setVisible(currentStep < steps.size() - 1);
        logger.debug("Prev button is visible: {}, Next button is visible: {}", prevBtn.isVisible(), nextBtn.isVisible());

        table.add(animRow).colspan(2).center().padBottom(20f);
        table.row();

        TextButton.TextButtonStyle style = neon.buttonRounded();
        TextButton mainMenuBtn = new TextButton("Main Menu", style);
        mainMenuBtn.getLabel().setFontScale(2.0f);
        logger.debug("Main Menu Button created");

        // Button listeners
        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep < steps.size() - 1) {
                    logger.debug("Next button clicked");
                    currentStep++;
                    showStep(currentStep);
                }
            }
        });

        prevBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep > 0) {
                    logger.debug("Preivous button clicked");
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


        table.row();
        table.add(mainMenuBtn).colspan(2).center().padTop(20f);
        logger.debug("Main Menu Button added to table");

    }

    /**
     * Switches back to the main menu screen.
     */
    void backMainMenu() {
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

    /**
     * Unloads any loaded clip textures and clears UI.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing TutorialScreenDisplay");
        if (!loadedFramePaths.isEmpty()) {
            ServiceLocator.getResourceService()
                    .unloadAssets(loadedFramePaths.toArray(new String[0]));
            loadedFramePaths.clear();
        }
        table.clear();
        super.dispose();
        logger.debug("Disposed TutorialScreenDisplay");
    }
}