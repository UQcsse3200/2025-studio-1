package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.ui.AnimatedClipImage;

import java.util.List;

/**
 * UI component that displays the multi-step Tutorial screen.
 * <p>
 * Each step contains a title, description, and an optional animated clip.
 * Users can navigate with previous/next arrows and return to the main menu.
 * <p>
 * This component extends {@link BaseScreenDisplay} to reuse common UI helpers
 * and lifecycle management (stage root, styles, and auto-disposal).
 */
public class TutorialScreenDisplay extends BaseScreenDisplay {
    /**
     * Ordered list of tutorial steps to display.
     */
    private final List<TutorialStep> steps;
    /**
     * Index of the currently displayed tutorial step.
     */
    protected int currentStep = 0;

    /**
     * Left navigation arrow texture, loaded once per screen instance.
     */
    private Texture arrowLeft;
    /**
     * Right navigation arrow texture, loaded once per screen instance.
     */
    private Texture arrowRight;

    /**
     * Creates a tutorial display bound to a list of steps.
     *
     * @param game  the game instance, used for navigation helpers
     * @param steps ordered list of tutorial steps to render (must not be {@code null})
     */
    public TutorialScreenDisplay(GdxGame game, List<TutorialStep> steps) {
        super(game);
        this.steps = steps;
    }

    /**
     * Loads per-screen textures, then delegates to {@link BaseScreenDisplay#create()}
     * to initialise the stage root and build the UI.
     */
    @Override
    public void create() {
        // Load per-screen textures once, then let BaseScreenDisplay set up stage/root/skin
        arrowLeft = new Texture("images/arrow-left.png");
        arrowRight = new Texture("images/arrow-right.png");
        super.create();
    }

    /**
     * Builds the initial UI by showing the current step.
     *
     * @param root the fill-parent root table created by {@link BaseScreenDisplay}
     */
    @Override
    protected void buildUI(Table root) {
        showStep(currentStep);
    }

    /**
     * Rebuilds the UI for the specified step index.
     * Clears the root table, adds the step title/description, renders the
     * animated clip if available, and wires prev/next and Main Menu controls.
     *
     * @param stepIndex zero-based index into {@link #steps}
     */
    void showStep(int stepIndex) {
        root.clear();
        TutorialStep step = steps.get(stepIndex);

        // Title
        addTitle(root, step.title(), 2f, null, 20f);

        // Description (clone small style; keep text white)
        Label.LabelStyle small = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        small.fontColor = skin.getColor("white");
        Label desc = new Label(step.description(), small);
        desc.setFontScale(2f);
        root.add(desc).colspan(2).center().padBottom(20f);
        root.row();

        // Animation row: [prev] [clip or fallback] [next]
        Table animRow = new Table();
        ImageButton prevBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(arrowLeft)));
        ImageButton nextBtn = new ImageButton(new TextureRegionDrawable(new TextureRegion(arrowRight)));

        AnimatedClipImage anim = null;
        if (step.clip() != null) {
            try {
                anim = new AnimatedClipImage(step.clip());
                anim.setScaling(Scaling.fit);
                anim.setSize(800f, 450f);
            } catch (Exception ex) {
                logger.error("Failed to load tutorial clip", ex);
            }
        }

        animRow.add(prevBtn).size(108f, 108f).padRight(10f);
        if (anim != null) {
            animRow.add(anim).size(1100f, 600f).padLeft(10f).padRight(10f);
        } else {
            Label fallback = new Label("Demo unavailable", small);
            fallback.setFontScale(2f);
            animRow.add(fallback).size(1100f, 600f).padLeft(10f).padRight(10f).center();
        }
        animRow.add(nextBtn).size(108f, 108f).padLeft(10f);

        prevBtn.setVisible(currentStep > 0);
        nextBtn.setVisible(currentStep < steps.size() - 1);

        root.add(animRow).colspan(2).center().padBottom(20f);
        root.row();

        // Bottom controls
        TextButton mainMenuBtn = button("Main Menu", 1.5f, this::backMainMenu);
        root.add(mainMenuBtn).colspan(2).center().padTop(20f);

        // Listeners
        prevBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep > 0) {
                    currentStep--;
                    showStep(currentStep);
                }
            }
        });

        nextBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (currentStep < steps.size() - 1) {
                    currentStep++;
                    showStep(currentStep);
                }
            }
        });
    }

    /**
     * Disposes arrow textures and then delegates to {@link BaseScreenDisplay#dispose()}.
     * Subclasses should ensure UI actors that use textures are detached before disposal.
     */
    @Override
    public void dispose() {
        if (arrowLeft != null) {
            arrowLeft.dispose();
            arrowLeft = null;
        }
        if (arrowRight != null) {
            arrowRight.dispose();
            arrowRight = null;
        }
        super.dispose();
    }
}
