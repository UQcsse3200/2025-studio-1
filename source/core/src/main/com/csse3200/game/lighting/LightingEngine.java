package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.CameraComponent;

/**
 * Processes lighting components using the Box2DLight library. Sets up the ray handler that is responsible
 * for rendering all lights to the screen.
 */
public class LightingEngine implements Disposable {
    private static final float AMBIENT_LIGHT = 0.75f;

    private final RayHandler rayHandler;
    private CameraComponent camera;

    /**
     * Constructor method for the lighting engine. This is where some of the rayHandler's
     * global variables are set and can be changed.
     *
     * @param camera The camera associated with the current renderer for the screen.
     * @param world The same world registered with the physics engine.
     */
    public LightingEngine(CameraComponent camera, World world) {
        this.camera = camera;
        this.rayHandler = new RayHandler(world);

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(false);
        rayHandler.setAmbientLight(AMBIENT_LIGHT);

        rayHandler.setBlur(true);
        rayHandler.setBlurNum(1);
    }

    /**
     * Injectable constructor method.
     *
     * @param rayHandler mock rayHandler
     * @param camera camera component
     */
    LightingEngine(RayHandler handler, CameraComponent camera) {
        this.rayHandler = handler;
        this.camera = camera;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void setAmbientLight(float value) {
        rayHandler.setAmbientLight(value);
    }

    /** If your camera changes (e.g., room switch) */
    public void setCamera(CameraComponent camera) {
        this.camera = camera;
    }

    public void render() {
        rayHandler.setCombinedMatrix(
                (com.badlogic.gdx.graphics.OrthographicCamera) camera.getCamera()
        );
        rayHandler.updateAndRender();
    }

    @Override
    public void dispose() {
        if (rayHandler != null) rayHandler.dispose();
    }
}