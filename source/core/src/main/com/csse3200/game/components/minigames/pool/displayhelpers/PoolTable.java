package com.csse3200.game.components.minigames.pool.displayhelpers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Renders the pool table surface, balls, cue, and guide.
 * Handles pointer-based aiming for the cue ball.
 * <p>
 * Inputs:
 * <ul>
 *   <li>{@link PowerProvider} supplies current cue power (0..1)</li>
 *   <li>{@link GuideCapper} clamps the guide length in pixels based on physics raycasts</li>
 * </ul>
 */
public final class PoolTable extends Widget {

    /**
     * Supplies the current cue power in the range [0..1].
     */
    public interface PowerProvider {
        /**
         * @return current cue power (0 to 1)
         */
        float get();
    }

    /**
     * Caps the guide length (in pixels) based on the current physics state.
     */
    public interface GuideCapper {
        /**
         * @param cuePosNorm   cue ball position in normalised [0..1] space
         * @param dirNorm      aim direction (unit vector)
         * @param desiredLenPx desired guide length in pixels
         * @param ballPx       cue ball radius in pixels
         * @return capped guide length in pixels
         */
        float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx);
    }

    // constants
    private static final float BALL_RADIUS_FRACTION = 0.035f;
    private static final float GUIDE_MIN_LEN_BALLS = 3.0f;
    private static final float GUIDE_MAX_LEN_BALLS = 12.0f;
    private static final float GUIDE_STEP_BALLS = 0.9f;
    private static final float DOT_SIZE_MIN_PX = 3f;
    private static final float DOT_SIZE_MAX_PX = 6f;
    private static final float GUIDE_ALPHA_MIN = 0.25f;
    private static final float GUIDE_ALPHA_MAX = 0.9f;
    private static final float CUE_KICK_DECAY = 3f;

    private final Texture tableTex;
    private final TextureRegion[] ballTextures;
    private final Texture cueTex;
    private final TextureRegion cueBallTex;
    private final Texture whitePx;

    // Bridges
    private final PowerProvider powerProvider;
    private final GuideCapper guideCapper;

    // State
    private final Vector2 cueBall = new Vector2();

    // Aiming
    private final Vector2 aimStart = new Vector2();
    private final Vector2 aimEnd = new Vector2();
    private final Vector2 aimDir = new Vector2();
    private Vector2[] balls = new Vector2[0];
    private boolean aiming = false;
    private boolean showGuide = true;
    private boolean showCue = true;

    // Cue kickback animation (0..1)
    private float cueKickT = 0f;

    /**
     * Constructs a new PoolTable.
     *
     * @param tableTex      texture for the table surface
     * @param ballTextures  textures for the object balls
     * @param cueTex        texture for the cue stick
     * @param cueBallTex    texture for the cue ball
     * @param powerProvider provides current cue power
     * @param guideCapper   caps guide length based on physics
     */
    public PoolTable(
            Texture tableTex,
            TextureRegion[] ballTextures,
            Texture cueTex,
            TextureRegion cueBallTex,
            PowerProvider powerProvider,
            GuideCapper guideCapper
    ) {
        this.tableTex = tableTex;
        this.ballTextures = ballTextures;
        this.cueTex = cueTex;
        this.cueBallTex = cueBallTex;
        this.powerProvider = powerProvider;
        this.guideCapper = guideCapper;
        this.whitePx = makeWhite();

        // Add pointer input listener for aiming
        addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent e, float x, float y, int pointer, int button) {
                beginAim(x, y);
                return true;
            }

            @Override
            public void touchDragged(InputEvent e, float x, float y, int pointer) {
                updateAim(x, y);
            }

            @Override
            public void touchUp(InputEvent e, float x, float y, int pointer, int button) {
                updateAim(x, y);
                finishAim();
            }

            @Override
            public boolean mouseMoved(InputEvent e, float x, float y) {
                updateAim(x, y);
                return false;
            }
        });
    }

    /**
     * Creates a 1x1 white texture used for drawing the guide dots.
     *
     * @return a new white pixel texture
     */
    private static Texture makeWhite() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Disposes of temporary textures created by this renderer.
     */
    public void dispose() {
        whitePx.dispose();
    }

    /**
     * Shows or hides the cue stick.
     *
     * @param v true to show, false to hide
     */
    public void setCueVisible(boolean v) {
        this.showCue = v;
    }

    /**
     * Shows or hides the aiming guide dots.
     *
     * @param v true to show, false to hide
     */
    public void setGuideVisible(boolean v) {
        this.showGuide = v;
    }

    /**
     * Sets the cue ball position in normalised [0..1] table coordinates.
     *
     * @param normPos cue ball position
     */
    public void setCueBall(Vector2 normPos) {
        cueBall.set(MathUtils.clamp(normPos.x, 0f, 1f), MathUtils.clamp(normPos.y, 0f, 1f));
    }

    /**
     * Sets the positions of all object balls in normalised [0..1] table coordinates.
     *
     * @param normPositions array of ball positions
     */
    public void setObjectBalls(Vector2[] normPositions) {
        this.balls = (normPositions != null) ? normPositions : new Vector2[0];
    }

    /**
     * @return a copy of the current aim direction (normalised)
     */
    public Vector2 getAimDir() {
        return aimDir.cpy();
    }

    /**
     * Starts a brief cue kickback animation.
     *
     * @param interp interpolation used for the animation
     */
    public void kickbackCue(Interpolation interp) {
        cueKickT = 1f;
    }

    /**
     * Updates cue animation timing.
     *
     * @param delta time since last frame
     */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (cueKickT > 0f) {
            cueKickT -= delta * CUE_KICK_DECAY;
            if (cueKickT < 0f) cueKickT = 0f;
        }
    }

    /**
     * Renders the table, balls, guide, and cue.
     *
     * @param batch       sprite batch used for drawing
     * @param parentAlpha parent alpha for blending
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
        batch.draw(tableTex, getX(), getY(), getWidth(), getHeight());

        final float ballPx = Math.min(getWidth(), getHeight()) * BALL_RADIUS_FRACTION;
        final float cbx = getX() + cueBall.x * getWidth();
        final float cby = getY() + cueBall.y * getHeight();

        final float drawSize = ballPx * 1.2f;
        if (balls != null) {
            for (int i = 0; i < balls.length; i++) {
                Vector2 b = balls[i];
                if (b == null) continue;
                float bx = getX() + b.x * getWidth() - drawSize / 2f;
                float by = getY() + b.y * getHeight() - drawSize / 2f;
                int id = i + 1;
                TextureRegion tex = (ballTextures != null && id < ballTextures.length && ballTextures[id] != null)
                        ? ballTextures[id] : cueBallTex;
                batch.draw(tex, bx, by, drawSize, drawSize);
            }
        }

        batch.draw(cueBallTex, cbx - ballPx / 2f, cby - ballPx / 2f, ballPx, ballPx);

        if (!aimDir.isZero(1e-4f)) {
            drawGuideAndCue(batch, ballPx, cbx, cby);
        }

        batch.setColor(1, 1, 1, 1);
    }

    /**
     * Begins aiming from the given pointer coordinates.
     *
     * @param x local x position
     * @param y local y position
     */
    public void beginAim(float x, float y) {
        aiming = true;
        aimStart.set(localToNorm(x, y));
        aimEnd.set(aimStart);
        computeDir();
    }

    /**
     * Updates the aim direction based on current pointer position.
     *
     * @param x local x position
     * @param y local y position
     */
    public void updateAim(float x, float y) {
        if (!aiming) return;
        aimEnd.set(localToNorm(x, y));
        computeDir();
    }

    /**
     * Finishes aiming interaction.
     */
    public void finishAim() {
        aiming = false;
    }

    /**
     * Recomputes the current aim direction based on start and end positions.
     */
    private void computeDir() {
        Vector2 target = aiming ? aimEnd : aimStart;
        aimDir.set(target).sub(cueBall).nor();
        if (Float.isNaN(aimDir.x) || Float.isNaN(aimDir.y)) aimDir.setZero();
    }

    /**
     * Converts local widget coordinates to normalised [0..1] table coordinates.
     *
     * @param x local x position
     * @param y local y position
     * @return normalised coordinate vector
     */
    private Vector2 localToNorm(float x, float y) {
        float nx = MathUtils.clamp(x / getWidth(), 0f, 1f);
        float ny = MathUtils.clamp(y / getHeight(), 0f, 1f);
        return new Vector2(nx, ny);
    }

    /**
     * Draws the aiming guide dots and cue stick.
     *
     * @param batch  sprite batch for rendering
     * @param ballPx cue ball radius in pixels
     * @param cbx    cue ball x position (pixels)
     * @param cby    cue ball y position (pixels)
     */
    private void drawGuideAndCue(Batch batch, float ballPx, float cbx, float cby) {
        float power = MathUtils.clamp(powerProvider != null ? powerProvider.get() : 0.5f, 0f, 1f);

        float desiredLenPx = MathUtils.lerp(ballPx * GUIDE_MIN_LEN_BALLS,
                ballPx * GUIDE_MAX_LEN_BALLS, power);

        float clampedLenPx = (showGuide && guideCapper != null)
                ? guideCapper.capGuideLenPx(new Vector2(cueBall), new Vector2(aimDir).nor(), desiredLenPx, ballPx)
                : desiredLenPx;

        float alpha = MathUtils.lerp(GUIDE_ALPHA_MIN, GUIDE_ALPHA_MAX, power);
        float dotSize = MathUtils.lerp(DOT_SIZE_MIN_PX, DOT_SIZE_MAX_PX, power);
        float step = ballPx * GUIDE_STEP_BALLS;
        int dots = Math.max(1, Math.round(clampedLenPx / step));

        if (showGuide) {
            batch.setColor(1, 1, 1, alpha);
            for (int i = 1; i <= dots; i++) {
                float d = step * i;
                float gx = cbx + aimDir.x * d;
                float gy = cby + aimDir.y * d;
                batch.draw(whitePx, gx - dotSize * 0.5f, gy - dotSize * 0.5f, dotSize, dotSize);
            }
            batch.setColor(1, 1, 1, 1);
        }

        if (showCue) {
            Sprite cueSprite = getCueSprite(ballPx, cbx, cby);
            cueSprite.draw(batch);
        }
    }

    /**
     * Builds and positions the cue sprite based on aim direction and kickback.
     *
     * @param ballPx cue ball radius in pixels
     * @param cbx    cue ball x position (pixels)
     * @param cby    cue ball y position (pixels)
     * @return a configured cue sprite ready for rendering
     */
    private Sprite getCueSprite(float ballPx, float cbx, float cby) {
        float cueLen = ballPx * 7f;
        float cueH = ballPx * 0.5f;
        float kick = (float) Math.pow(cueKickT, 2) * ballPx * 0.7f;

        float offCenter = ballPx * 0.55f + kick + cueLen * 0.5f;

        float cx = cbx - aimDir.x * offCenter;
        float cy = cby - aimDir.y * offCenter;

        float angleDeg = MathUtils.atan2(aimDir.y, aimDir.x) * MathUtils.radiansToDegrees;

        Sprite cueSprite = new Sprite(cueTex);
        cueSprite.setSize(cueLen, cueH);
        cueSprite.setOriginCenter();
        cueSprite.setRotation(angleDeg);
        cueSprite.setCenter(cx, cy);
        return cueSprite;
    }
}