package com.csse3200.game.components.minigames.robotFighting;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * A Scene2D Image that plays an Animation<TextureRegion>.
 * Used to display animated atlas sprites in UI stages.
 */
public class AnimatedImage extends Image {
    private final Animation<TextureRegion> animation;
    private float stateTime = 0f;

    /**
     * Creates an AnimatedImage using the given animation.
     *
     * @param animation the Animation<TextureRegion> to play
     */
    public AnimatedImage(Animation<TextureRegion> animation) {
        super(new TextureRegionDrawable(animation.getKeyFrame(0))); // set initial frame
        this.animation = animation;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);
        ((TextureRegionDrawable) getDrawable()).setRegion(currentFrame);
    }
}
