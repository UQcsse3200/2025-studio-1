package com.csse3200.game.services;

import com.csse3200.game.components.CameraComponent;

public class CameraService {
    private CameraComponent mainCamera;

    public void setMainCamera(CameraComponent camera) {
        this.mainCamera = camera;
    }

    public CameraComponent getMainCamera() {
        return mainCamera;
    }

    public boolean hasCamera() {
        return mainCamera != null;
    }
}