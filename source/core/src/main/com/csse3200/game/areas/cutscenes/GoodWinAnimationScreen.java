package com.csse3200.game.areas.cutscenes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class GoodWinAnimationScreen extends GameArea {
    private static final GridPoint2 PLAYER_COORDS = new GridPoint2(10,10);
    private static final GridPoint2[] EXPLOSION_COORDS = new GridPoint2[]{
            new GridPoint2(20,10)
    };

    final static float EXPLOSION_FRAME_DURATION = 0.1f;


    public GoodWinAnimationScreen(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public void create() {
        ensureAssets();
        spawnPlayer();
        spawnExplosions();
    }

    @Override
    public String toString() {
        return "WinAnimation";
    }

    /**
     * allows manipulation of player character by loading function
     *
     * @return player entity
     */
    public Entity getPlayer() {
        return null; //Handles cleanly without errors
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
                0.1f,
                Animation.PlayMode.LOOP,
                1f);
    }

    private AnimationRenderComponent explosionAnimation() {
        return createAnimation(
                "images/explosion_2.atlas",
                "explosion_2",
                EXPLOSION_FRAME_DURATION,
                Animation.PlayMode.LOOP,
                1f);
    }

    private void spawnPlayer() {
        Entity player = new Entity()
                .addComponent(playerAnimation());
        player.setPosition(PLAYER_COORDS.x, PLAYER_COORDS.y);
        spawnEntity(player);
    }

    private void spawnExplosions() {
            Entity explosion = new Entity()
                    .addComponent(explosionAnimation());
        for (GridPoint2 explosionCoord : EXPLOSION_COORDS) {
            explosion.setPosition(explosionCoord.x, explosionCoord.y);
            spawnEntity(explosion);
        }
    }

    private AnimationRenderComponent createAnimation(String atlasPath, String animationName, float frameDuration, Animation.PlayMode playMode, float width) {
        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService()
                        .getAsset(atlasPath, TextureAtlas.class));
        animator.addAnimation(animationName, frameDuration, playMode);
        animator.scaleEntity(width);
        animator.startAnimation(animationName);
        return animator;
    }
}
