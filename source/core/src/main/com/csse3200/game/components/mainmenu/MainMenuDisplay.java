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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ui component for displaying the Main menu.
 */
public class MainMenuDisplay extends UIComponent {
  private static final Logger logger = LoggerFactory.getLogger(MainMenuDisplay.class);
  private static final float Z_INDEX = 2f;
  private Table table;
  private NeonStyles neon;

  @Override
  public void create() {
    super.create();
    neon = new NeonStyles(0.70f);
    addActors();
  }

  private void addActors() {
    table = new Table();
    table.setFillParent(true);

    // Position
    float leftPad = stage.getWidth() * 0.12f;
    table.center().left().padLeft(leftPad);

    Image title =
        new Image(
            ServiceLocator.getResourceService()
                .getAsset("images/logo.png", Texture.class));

    float btnW = stage.getWidth() * 0.34f;
    float btnH = Math.max(64f, stage.getHeight() * 0.08f);
    table.defaults().width(btnW).height(btnH);

    TextButton.TextButtonStyle style = neon.buttonRounded();

    TextButton startBtn = new TextButton("Start", style);
    TextButton loadBtn = new TextButton("Load", style);
    TextButton settingsBtn = new TextButton("Settings", style);
    TextButton exitBtn = new TextButton("Exit", style);

    startBtn.getLabel().setFontScale(2.0f);
    loadBtn.getLabel().setFontScale(2.0f);
    settingsBtn.getLabel().setFontScale(2.0f);
    exitBtn.getLabel().setFontScale(2.0f);

    // Triggers an event when the button is pressed
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

    table.add(title).left().padBottom(40f).padLeft(-10f);
    table.row();
    table.add(startBtn).padTop(15f).left();
    table.row();
    table.add(loadBtn).padTop(15f).left();
    table.row();
    table.add(settingsBtn).padTop(15f).left();
    table.row();
    table.add(exitBtn).padTop(15f).left();

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

  @Override
  public void dispose() {
    table.clear();
    super.dispose();
  }
}
