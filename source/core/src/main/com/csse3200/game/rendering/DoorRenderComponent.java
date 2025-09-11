package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Color;

/**
 * A render component specifically for doors that ensures they are rendered on top of floor assets.
 * Uses a higher rendering layer than the default layer.
 */
public class DoorRenderComponent extends SolidColorRenderComponent {
  private static final int DOOR_LAYER = 999; // Very high layer to ensure doors are absolutely on top of everything

  public DoorRenderComponent(Color color) {
    super(color);
  }

  @Override
  public int getLayer() {
    return DOOR_LAYER;
  }

  @Override
  public float getZIndex() {
    // Doors should always be rendered on top within their layer, regardless of Y position
    return Float.MAX_VALUE;
  }
}
