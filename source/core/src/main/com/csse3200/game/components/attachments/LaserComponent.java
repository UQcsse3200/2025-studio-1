package com.csse3200.game.components.attachments;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.RenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class LaserComponent extends RenderComponent {
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final float length;
    private Vector2 direction = new Vector2(1, 0); // default facing right
    private Color color = Color.RED;
    private Camera camera;

    public LaserComponent(float length) {
        this.length = length;
        Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
        for (Entity entity: entities) {
            if (entity.getComponent(CameraComponent.class) != null) {
                camera = entity.getComponent(CameraComponent.class).getCamera();
            }
        }
    }

    @Override
    public void draw(SpriteBatch batch) {

        Vector2 gunPos = entity.getPosition();
        gunPos.y += 0.18f;
        gunPos.x += 1f;
        Vector3 end = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        batch.end();

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.line(gunPos, new Vector2(end.x, end.y));
        shapeRenderer.end();

        batch.begin();
    }
}