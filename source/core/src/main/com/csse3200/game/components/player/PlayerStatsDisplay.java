package com.csse3200.game.components.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.components.player.InventoryComponent;

/**
 * A ui component for displaying player stats, e.g. health.
 */
public class PlayerStatsDisplay extends UIComponent {
  private Table table;
  private ProgressBar healthBar;
  private Label currencyLabel;

  /**
   * Creates reusable ui styles and adds actors to the stage.
   */
  @Override
  public void create() {
    super.create();
    addActors();

    entity.getEvents().addListener("updateHealth", this::updatePlayerHealthUI);
    entity.getEvents().addListener("updateCurrency", this::updatePlayerCurrencyUI);
  }

  /**
   * Helper to create a colored drawable from a 1x1 pixel texture
   */
  private Drawable makeColorDrawable(Color color) {
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    Texture texture = new Texture(pixmap);
    pixmap.dispose();
    return new TextureRegionDrawable(new TextureRegion(texture));
  }

  /**
   * Creates actors and positions them on the stage using a table.
   * @see Table for positioning options
   */
  private void addActors() {
    table = new Table();
    table.top().left();
    table.setFillParent(true);
    table.padTop(45f).padLeft(5f);

    // Health bar size
    float barWidth = 200f;
    float barHeight = 30f;

    // Setting health bar attributes
    ProgressBar.ProgressBarStyle healthBarStyle = new ProgressBar.ProgressBarStyle();
    healthBarStyle.background = makeColorDrawable(Color.DARK_GRAY);
    healthBarStyle.background.setMinHeight(barHeight);
    healthBarStyle.knobBefore = makeColorDrawable(Color.RED);
    healthBarStyle.knobBefore.setMinHeight(barHeight);
    healthBarStyle.knob = null;

    int health = entity.getComponent(CombatStatsComponent.class).getHealth();
    // Health bar creation, currently hardcoded to be max of 100
    healthBar = new ProgressBar(0, 100, 1, false, healthBarStyle);
    healthBar.setValue(health);
    healthBar.setAnimateDuration(0.0f);

    // Currency text
    int currency = entity.getComponent(InventoryComponent.class).getGold();
    CharSequence currencyText = String.format("Currency: %d", currency);
    currencyLabel = new Label(currencyText, skin, "large");

    table.add(healthBar).width(barWidth).height(barHeight).pad(5);
    table.row();
    table.add(currencyLabel);
    stage.addActor(table);
  }

  @Override
  public void draw(SpriteBatch batch) {
    // draw is handled by the stage
  }

  /**
   * Updates the player's health on the UI.
   * @param health player health
   */
  public void updatePlayerHealthUI(int health) {
    healthBar.setValue(health);
  }

  /**
   * Updates the player's currency on the UI.
   * @param currency player currency
   */
  public void updatePlayerCurrencyUI(int currency) {
    CharSequence text = String.format("Currency: %d", currency);
    currencyLabel.setText(text);
  }

  @Override
  public void dispose() {
    super.dispose();
    healthBar.remove();
    currencyLabel.remove();
  }
}