package com.csse3200.game.areas.cutscenes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;


public class GoodWinAnimationScreen extends BadWinAnimationScreen {
    private static final GridPoint2 PLAYER_COORDS = new GridPoint2(-10,15);
    private static final float PLAYER_FRAME_DURATION = 0.15f;


    public GoodWinAnimationScreen(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public void create() {
        super.create();
        spawnPlayer();
    }

    @Override
    public String toString() {
        return "GoodWinAnimation";
    }

    private void ensureAssets() {
        String[] textures = {
                "images/WinscreenAnimationBackground.png"
        };
        String[] atlases = {
                "images/player.atlas",
                "images/explosion_2.atlas"
        };
        ensureAtlases(atlases);
        ensureTextures(textures);
    }

    private AnimationRenderComponent playerAnimation() {
        return createAnimation(
                "images/player.atlas",
                "left_run",
                PLAYER_FRAME_DURATION,
                Animation.PlayMode.LOOP,
                1f);
    }

    private void spawnPlayer() {
        Entity player = new Entity()
                .addComponent(playerAnimation());
        player.setPosition(x(PLAYER_COORDS.x), y(PLAYER_COORDS.y));
        player.getComponent(AnimationRenderComponent.class).scaleEntity(10f);
        spawnEntity(player);
    }

    public static GoodWinAnimationScreen load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new GoodWinAnimationScreen(terrainFactory, camera));
    }
}
