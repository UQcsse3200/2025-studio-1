package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

/**
 * A generic component for rendering an entity. Registers itself with the render service in order to
 * be rendered each frame. Child classes can implement different kinds of rendering behaviour.
 */
public abstract class RenderComponent extends Component implements Renderable, Disposable {
    private static final int DEFAULT_LAYER = 1;
    private boolean disabled = false;
    private Float customZIndex = null;

    @Override
    public void create() {
        ServiceLocator.getRenderService().register(this);
    }

    @Override
    public void dispose() {
        ServiceLocator.getRenderService().unregister(this);
    }

    @Override
    public void render(SpriteBatch batch) {
        if (disabled)
            return;
        draw(batch);
    }

    @Override
    public int compareTo(Renderable o) {
        return Float.compare(getZIndex(), o.getZIndex());
    }

    @Override
    public int getLayer() {
        return DEFAULT_LAYER;
    }

    @Override
    public float getZIndex() {
        // The smaller the Y value, the higher the Z index, so that closer entities are drawn in front
        if (customZIndex != null)
            return customZIndex;
        return -entity.getPosition().y;
    }

    /**
     * This function sets the Z index for the component the
     * higher the Z index, so that closer entities are drawn in front
     *
     * @param value
     */
    public void setZIndex(Float value) {
        customZIndex = value;
    }

    /**
     * Draw the renderable. Should be called only by the renderer, not manually.
     *
     * @param batch Batch to render to.
     */
    protected abstract void draw(SpriteBatch batch);

    /**
     * Disables the component
     */
    public void disableComponent() {
        this.disabled = true;
    }

    /**
     * Enables the component
     */
    public void enableComponent() {
        this.disabled = false;
    }

    /**
     * Checks if the component is disabled
     *
     * @return True if the component is disabled, false otherwise
     */
    public boolean isDisabled() {
        return this.disabled;
    }
}
