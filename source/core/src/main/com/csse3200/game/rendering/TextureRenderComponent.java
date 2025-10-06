package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

/**
 * Render a static texture, with optional fade (alpha).
 */
public class TextureRenderComponent extends RenderComponent {
    private final Texture texture;
    private float alpha = 1f; // Added for fade/opacity
    private float zIndex;

    /**
     * @param texturePath Internal path of static texture to render.
     *                    Will be scaled to the entity's scale.
     */
    public TextureRenderComponent(String texturePath) {
        this(ServiceLocator.getResourceService().getAsset(texturePath, Texture.class));
        this.zIndex = Float.MIN_VALUE;
    }

    /**
     * @param texture Static texture to render. Will be scaled to the entity's scale.
     */
    public TextureRenderComponent(Texture texture) {
        this.texture = texture;
    }

    /**
     * Scale the entity to a width of 1 and a height matching the texture's ratio
     */
    public void scaleEntity() {
        entity.setScale(1f, (float) texture.getHeight() / texture.getWidth());
    }

    protected Texture getTexture() {
        return texture;
    }

    /**
     * Sets the opacity (alpha multiplier 0-1) for fade effects.
     * @param alpha value from 0.0 (fully transparent) to 1.0 (fully opaque)
     */
    public void setAlpha(float alpha) {
        this.alpha = Math.clamp(alpha, 0f, 1f);
    }

    /**
     * Gets the current opacity (alpha multiplier).
     */
    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void draw(SpriteBatch batch) {
        if (!isDisabled()) {
            Vector2 position = entity.getPosition();
            Vector2 scale = entity.getScale();
            batch.setColor(1f, 1f, 1f, alpha); // Set fade/opacity
            batch.draw(texture, position.x, position.y, scale.x, scale.y);
            batch.setColor(1f, 1f, 1f, 1f); // Reset color for other renders
        }
    }

    /**
     * Gets the zIndex of the entity to determine rendering order.
     * @return If zIndex hasn't been set, return negative entity's y position, else return the set value.
     */
    @Override
    public float getZIndex() {
        if (this.zIndex != Float.MIN_VALUE) {
            return this.zIndex;
        } else {
            return -entity.getPosition().y;
        }
    }

    /**
     * Set the zIndex.
     * @param zIndex zIndex to be set.
     */
    public void setZIndex(float zIndex) {
        this.zIndex = zIndex;
    }
}
