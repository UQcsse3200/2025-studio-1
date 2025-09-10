package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * An extension of the TextureRenderComponent, which allows rotation of your textures.
 * Just need to specify the angle of rotation in terms of degrees.
 */
public class TextureRenderWithRotationComponent extends TextureRenderComponent {
    private final TextureRegion region;
    private float rotation = 0;
    private boolean hasSetRotation = false;

    public TextureRenderWithRotationComponent(String texturePath) {
        super(texturePath); // still loads the Texture
        region = new TextureRegion(super.getTexture());
    }

    /**
     * Set the rotation value this rendering component will use to rotate the texture.
     * @param value The rotation value, in degrees.
     */
    public void setRotation(float value) {
        if (!hasSetRotation)
        {
            rotation = value;
            hasSetRotation = true;
        }
    }

    @Override
    protected void draw(SpriteBatch batch) {
        //System.out.println("Rendering" + ServiceLocator.getTimeSource().getTime());
        Vector2 position = entity.getPosition();
        Vector2 scale = entity.getScale();

        batch.draw(region, position.x, position.y,
                scale.x / 2f, scale.y / 2f,
                scale.x, scale.y,
                1f, 1f,
                rotation);
    }
}
