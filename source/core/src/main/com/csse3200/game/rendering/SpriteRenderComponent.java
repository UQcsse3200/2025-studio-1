package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.services.ServiceLocator;

public class SpriteRenderComponent extends TextureRenderComponent {
    private final Sprite sprite;
    private final TextureRegion region;
    private float rotation = 0;
    private boolean hasSetRotation = false;

    private static int id;
    private int ownID;

    public SpriteRenderComponent(String texturePath) {
        super(texturePath); // still loads the Texture
        region = new TextureRegion(super.getTexture());
        this.sprite = new Sprite(super.getTexture()); // wrap it in a Sprite
        sprite.setOriginCenter(); // rotate around center by default
    }

    public void setRotation(float value) {
        if (!hasSetRotation)
        {
            rotation = value;
            hasSetRotation = true;
            ownID = id++;
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
