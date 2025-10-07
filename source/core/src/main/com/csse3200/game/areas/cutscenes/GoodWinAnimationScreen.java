package com.csse3200.game.areas.cutscenes;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.MainHall;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

public class GoodWinAnimationScreen extends GameArea {
    private static final GridPoint2 PLAYER_COORDS = new GridPoint2(-10,15);
    private static final GridPoint2[] EXPLOSION_COORDS = new GridPoint2[]{
            new GridPoint2(77,69),
            new GridPoint2(54,69),
            new GridPoint2(57,49),
            new GridPoint2(52,24),
            new GridPoint2(74,45),
            new GridPoint2(78, 24),
            new GridPoint2(50, 45)
    };

    final static float EXPLOSION_FRAME_DURATION = 0.2f;


    public GoodWinAnimationScreen(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public void create() {
        ensureAssets();
        terrain = terrainFactory.createTerrain(TerrainFactory.TerrainType.WIN_SCREEN);
        spawnEntity(new Entity().addComponent(terrain));

        spawnPlayer();
        spawnExplosions();
    }

    @Override
    public String toString() {
        return "GoodWinAnimation";
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
                0.15f,
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
        player.setPosition(x(PLAYER_COORDS.x), y(PLAYER_COORDS.y));
        player.getComponent(AnimationRenderComponent.class).scaleEntity(10f);
        spawnEntity(player);
    }

    private void spawnExplosions() {
        for (GridPoint2 explosionCoord : EXPLOSION_COORDS) {
            Entity explosion = new Entity()
                        .addComponent(explosionAnimation());
            explosion.setPosition(x(explosionCoord.x), y(explosionCoord.y));
            spawnEntity(explosion);
        }
    }

    private AnimationRenderComponent createAnimation(String atlasPath, String animationName, float frameDuration, Animation.PlayMode playMode, float width) {
        AnimationRenderComponent animator = new AnimationRenderComponent(
                ServiceLocator.getResourceService()
                        .getAsset(atlasPath, TextureAtlas.class));
        animator.addAnimation(animationName, frameDuration, playMode);
        //animator.scaleEntity(width);
        animator.startAnimation(animationName);
        return animator;
    }

    public static GoodWinAnimationScreen load(TerrainFactory terrainFactory, CameraComponent camera) {
        return (new GoodWinAnimationScreen(terrainFactory, camera));
    }

    private float x(int value) {
        return value * 14 / 100f;
    }

    private float y(int value) {
        return value * 8 / 100f + 3;
    }
}
