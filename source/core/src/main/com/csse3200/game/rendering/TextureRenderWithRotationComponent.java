package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.utils.math.Vector2Utils;

import static com.badlogic.gdx.Gdx.input;

/**
 * An extension of the TextureRenderComponent, which allows rotation of your textures.
 * Just need to specify the angle of rotation in terms of degrees.
 */
public class TextureRenderWithRotationComponent extends TextureRenderComponent {
    private final TextureRegion region;
    private final String texturePath; //For testing
    private float rotation = 0;
    private Camera camera;
    private boolean rotated = false;

    public TextureRenderWithRotationComponent(String texturePath) {
        super(texturePath); // still loads the Texture
        this.texturePath = texturePath;
        region = new TextureRegion(super.getTexture());
    }

    /**
     * @return The rotation value
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set the rotation value this rendering component will use to rotate the texture.
     *
     * @param value The rotation value, in degrees.
     */
    public void setRotation(float value) {
        rotation = value;
    }

    /**
     * Set the boolean "rotated"
     *
     * @param value The boolean value to set to "rotated"
     */
    public void setHasRotated(boolean value) {
        rotated = value;
    }

    /**
     * @return the texture path
     */
    public String getTexturePath() {
        return this.texturePath;
    }

    @Override
    protected void draw(SpriteBatch batch) {
        //System.out.println("Rendering" + ServiceLocator.getTimeSource().getTime());
        Vector2 position = entity.getPosition();
        Vector2 scale = entity.getScale();

        // Find camera from any entity with CameraComponent
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();

        if (camera == null) {
            for (Entity entity : entities) {
                if (entity.getComponent(CameraComponent.class) != null) {
                    this.camera = entity.getComponent(CameraComponent.class).getCamera();

                }
            }
        }

        // is a ranged weapon - follow mouse movement
        if (entity.hasComponent(WeaponsStatsComponent.class) && entity.hasComponent(MagazineComponent.class)) {
            Vector3 mouseScreenPos = new Vector3(input.getX(), input.getY(), 0);
            if (camera != null) {
                camera.unproject(mouseScreenPos); //convert mouse pos to world coordinates
            }
            Vector2 mouseWorldPos = new Vector2(0, 0);
            mouseWorldPos.set(mouseScreenPos.x, mouseScreenPos.y);
            rotation = (float) Vector2Utils.angleFromTo(entity.getPosition(), mouseWorldPos);
        } else if (entity.hasComponent(WeaponsStatsComponent.class)) { //Bullet
            if (!rotated) {
                //Only rotate the bullet once to the mouse direction and then dont rotate anymore
                Vector3 mouseScreenPos = new Vector3(input.getX(), input.getY(), 0);
                if (camera != null) {
                    camera.unproject(mouseScreenPos); //convert mouse pos to world coordinates
                }
                Vector2 mouseWorldPos = new Vector2(0, 0);
                mouseWorldPos.set(mouseScreenPos.x, mouseScreenPos.y);
                rotation = (float) Vector2Utils.angleFromTo(entity.getPosition(), mouseWorldPos);
                rotated = true;
            }

            if (entity.getComponent(WeaponsStatsComponent.class).getRocket()) {
                //Rotate the rocket towards the nearest enemy
                Entity nearestEnemy = null;
                float minDistance = Float.MAX_VALUE;
                for (Entity entity : ServiceLocator.getEntityService().getEntities()) {
                    //Find npcs with hitboxes
                    if (entity.hasComponent(HitboxComponent.class) &&
                            entity.getComponent(HitboxComponent.class).getLayer() == PhysicsLayer.NPC) {
                        float currDistance = Math.abs(ServiceLocator.getPlayer().getCenterPosition().len()
                                - entity.getCenterPosition().len());

                        if (currDistance < minDistance) {
                            //Update nearest enemy
                            nearestEnemy = entity;
                            minDistance = currDistance;
                        }
                    }

                }
                if (nearestEnemy != null) {
                    //Rotate
                    rotation = (float) Vector2Utils.angleFromTo(entity.getPosition(), nearestEnemy.getPosition());
                }
            }
        }

        batch.draw(region, position.x, position.y,
                scale.x / 2f, scale.y / 2f,
                scale.x, scale.y,
                1f, 1f,
                rotation);
    }
}
