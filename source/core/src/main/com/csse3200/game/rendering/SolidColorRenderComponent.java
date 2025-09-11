package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/** Render a solid color rectangle using a 1x1 texture scaled to the entity size. */
public class SolidColorRenderComponent extends RenderComponent {
  private final Color color;
  private Texture texture;

  public SolidColorRenderComponent(Color color) {
    this.color = new Color(color);
  }

  @Override
  public void create() {
    super.create();
    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(color);
    pixmap.fill();
    texture = new Texture(pixmap);
    pixmap.dispose();
  }

  @Override
  protected void draw(SpriteBatch batch) {
    Vector2 position = entity.getPosition();
    Vector2 scale = entity.getScale();
    batch.draw(texture, position.x, position.y, scale.x, scale.y);
  }

  @Override
  public void dispose() {
    if (texture != null) {
      texture.dispose();
    }
    super.dispose();
  }
}


