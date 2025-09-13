package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;
import com.badlogic.gdx.graphics.*;

import static com.badlogic.gdx.Gdx.input;

/**
 * An extension of the TextureRenderComponent, which allows rotation of your textures.
 * Just need to specify the angle of rotation in terms of degrees.
 */
public class TextureRenderWithRotationComponent extends TextureRenderComponent {
    private final TextureRegion region;
    private float rotation = 0;
    private boolean hasSetRotation = false;
    private Camera camera;

    public TextureRenderWithRotationComponent(String texturePath) {
        super(texturePath); // still loads the Texture
        region = new TextureRegion(super.getTexture());

        // Find camera from any entity with CameraComponent
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
        for (Entity entity: entities) {
            if (entity.getComponent(CameraComponent.class) != null) {
                this.camera = entity.getComponent(CameraComponent.class).getCamera();

            }
        }
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

        // is a ranged weapon - follow mouse movement
        if (entity.hasComponent(WeaponsStatsComponent.class)) {
            Vector3 mouseScreenPos = new Vector3(input.getX(), input.getY(), 0);
            if (camera != null) {
                camera.unproject(mouseScreenPos); //convert mouse pos to world coordinates
            }
            Vector2 mouseWorldPos = new Vector2(0, 0);
            mouseWorldPos.set(mouseScreenPos.x, mouseScreenPos.y);
            rotation = (float) Vector2Utils.angleFromTo(entity.getPosition(), mouseWorldPos);
        }

        batch.draw(region, position.x, position.y,
                scale.x / 2f, scale.y / 2f,
                scale.x, scale.y,
                1f, 1f,
                rotation);
    }
}
