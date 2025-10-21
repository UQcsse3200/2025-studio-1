package com.csse3200.game.lighting;

import box2dLight.RayHandler;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.components.CameraComponent;

public class LightingEngine implements Disposable {
    private final RayHandler rayHandler;
    private CameraComponent camera;

    public LightingEngine(CameraComponent camera, World world) {
        this.camera = camera;
        this.rayHandler = new RayHandler(world);

        RayHandler.setGammaCorrection(true);
        RayHandler.useDiffuseLight(false);
        rayHandler.setAmbientLight(0.75f);

        rayHandler.setBlur(true);
        rayHandler.setBlurNum(1);
    }

    // Injectable/testing ctor
    LightingEngine(RayHandler handler, CameraComponent camera) {
        this.rayHandler = handler;
        this.camera = camera;
    }

    public RayHandler getRayHandler() {
        return rayHandler;
    }

    public void setAmbientLight(float value) {
        rayHandler.setAmbientLight(value); // push immediately
    }

    /** If your camera changes (e.g., room switch) */
    public void setCamera(CameraComponent camera) {
        this.camera = camera;
    }

    public void render() {
        rayHandler.setCombinedMatrix(camera.getProjectionMatrix());
        rayHandler.updateAndRender();
    }

    @Override
    public void dispose() {
        if (rayHandler != null) rayHandler.dispose();
    }
}