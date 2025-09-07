package com.csse3200.game.components.mainmenu;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.components.screens.TutorialScreenDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.csse3200.game.GdxGame;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;
  private NeonStyles neon;
  private GdxGame game;

  public MainMenuDisplay(GdxGame game) {
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
   * Creates the logo and buttons, sizes them relative to the stage, and wires
   * button events.
   */
  private void addActors() {
    table = new Table();
    table.setFillParent(true);

    // Column position
    float leftPad = stage.getWidth() * 0.12f;
    table.center().left().padLeft(leftPad);

    // Logo image
    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/logo.png", Texture.class));
    logger.debug("Logo image added");

    // Button sizing relative to screen
    float btnW = stage.getWidth() * 0.34f;
    float btnH = Math.max(64f, stage.getHeight() * 0.08f);
    table.defaults().width(btnW).height(btnH);

    TextButton.TextButtonStyle style = neon.buttonRounded();

    // Create buttons
    TextButton startBtn = new TextButton("Start", style);
    TextButton loadBtn = new TextButton("Load", style);
    TextButton settingsBtn = new TextButton("Settings", style);
    TextButton exitBtn = new TextButton("Exit", style);
    TextButton tutorialBtn = new TextButton("Tutorial", style);

    // Label text size
    startBtn.getLabel().setFontScale(2.0f);
    loadBtn.getLabel().setFontScale(2.0f);
    settingsBtn.getLabel().setFontScale(2.0f);
    exitBtn.getLabel().setFontScale(2.0f);
    tutorialBtn.getLabel().setFontScale(2.0f);

    // Button actions
    startBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Start button clicked");
            entity.getEvents().trigger("start");
          }
        });

    loadBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Load button clicked");
            entity.getEvents().trigger("load");
          }
        });

    settingsBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Settings button clicked");
            entity.getEvents().trigger("settings");
          }
        });

    exitBtn.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent changeEvent, Actor actor) {
            logger.debug("Exit button clicked");
            entity.getEvents().trigger("exit");
          }
        });

    tutorialBtn.addListener(new ChangeListener() {
      @Override
      public void changed(ChangeEvent event, Actor actor) {
        logger.debug("Tutorial button clicked");
        entity.getEvents().trigger("tutorial");
      }
    });

    // Column layout
    table.add(title).left().padBottom(40f).padLeft(-10f);
    table.row();
    table.add(startBtn).padTop(15f).left();
    table.row();
    table.add(loadBtn).padTop(15f).left();
    table.row();
    table.add(settingsBtn).padTop(15f).left();
    table.row();
    table.add(exitBtn).padTop(15f).left();
    table.row();
    table.add(tutorialBtn).padTop(15f).left();
    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  @Override
  public float getZIndex() {
    return Z_INDEX;
  }

  /**
   * Removes and clears the root table.
   */
  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
