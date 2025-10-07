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
 * Renders the pool table surface, balls, aim guide and cue,
 * and handles pointer-based aiming. Pure UI: no physics types leak in.
 * <p>
 * Inputs:
 * - {@link PowerProvider} supplies current cue power (0..1).
 * - {@link GuideCapper} clamps the guide length in pixels based on physics raycasts.
 */
public final class PoolTable extends Widget {
    // ------------------------------------------------------------
    // Tuning constants (all visual)
    // ------------------------------------------------------------
    private static final float BALL_RADIUS_FRACTION = 0.035f; // ball radius (px) = min(w,h) * this
    private static final float GUIDE_MIN_LEN_BALLS = 3.0f;   // min guide len in "ball radii"
    private static final float GUIDE_MAX_LEN_BALLS = 12.0f;  // max guide len in "ball radii"
    private static final float GUIDE_STEP_BALLS = 0.9f;   // dotted spacing in "ball radii"
    private static final float DOT_SIZE_MIN_PX = 3f;
    private static final float DOT_SIZE_MAX_PX = 6f;
    private static final float GUIDE_ALPHA_MIN = 0.25f;
    private static final float GUIDE_ALPHA_MAX = 0.9f;
    private static final float CUE_KICK_DECAY = 3f;
    private final Texture tableTex;
    private final TextureRegion[] ballTextures; // index 1..15 expected
    private final Texture cueTex;               // texture for cue stick
    private final TextureRegion cueBallTex;
    private final Texture whitePx;              // local 1x1 white for dots
    // Bridges
    private final PowerProvider powerProvider;
    private final GuideCapper guideCapper;
    // State (normalised table coordinates [0..1])
    private final Vector2 cueBall = new Vector2();
    // Aiming
    private final Vector2 aimStart = new Vector2();
    private final Vector2 aimEnd = new Vector2();
    private final Vector2 aimDir = new Vector2();
    private Vector2[] balls = new Vector2[0];
    private boolean aiming = false;
    private boolean showGuide = true;
    private boolean showCue = true;
    // Cue kickback anim (0..1)
    private float cueKickT = 0f;

    // ------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------
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

        // Pointer-driven aiming
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

    private static Texture makeWhite() {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.WHITE);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    /**
     * Must be called by owner to release local resources.
     */
    public void dispose() {
        whitePx.dispose();
    }

    public void setCueVisible(boolean v) {
        this.showCue = v;
    }

    // ------------------------------------------------------------
    // External API (UI-only)
    // ------------------------------------------------------------
    public void setGuideVisible(boolean v) {
        this.showGuide = v;
    }

    /**
     * Cue ball position in normalised [0..1] table coords.
     */
    public void setCueBall(Vector2 normPos) {
        cueBall.set(MathUtils.clamp(normPos.x, 0f, 1f), MathUtils.clamp(normPos.y, 0f, 1f));
    }

    /**
     * Object balls in normalised [0..1] table coords.
     */
    public void setObjectBalls(Vector2[] normPositions) {
        this.balls = (normPositions != null) ? normPositions : new Vector2[0];
    }

    /**
     * Returns a copy of the current aim direction (normalised).
     */
    public Vector2 getAimDir() {
        return aimDir.cpy();
    }

    /**
     * Triggers a brief cue kickback animation.
     */
    public void kickbackCue(Interpolation interp) {
        cueKickT = 1f;
    }

    // ------------------------------------------------------------
    // Scene2D
    // ------------------------------------------------------------
    @Override
    public void act(float delta) {
        super.act(delta);
        if (cueKickT > 0f) {
            cueKickT -= delta * CUE_KICK_DECAY;
            if (cueKickT < 0f) cueKickT = 0f;
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // Table surface
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
        batch.draw(tableTex, getX(), getY(), getWidth(), getHeight());

        // Common measures
        final float ballPx = Math.min(getWidth(), getHeight()) * BALL_RADIUS_FRACTION;
        final float cbx = getX() + cueBall.x * getWidth();
        final float cby = getY() + cueBall.y * getHeight();

        // Balls (simple draw; index 1..15)
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

        // Cue ball
        batch.draw(cueBallTex, cbx - ballPx / 2f, cby - ballPx / 2f, ballPx, ballPx);

        // Guide + Cue
        if (!aimDir.isZero(1e-4f)) {
            drawGuideAndCue(batch, ballPx, cbx, cby);
        }

        // Restore color
        batch.setColor(1, 1, 1, 1);
    }

    // ------------------------------------------------------------
    // Aiming internals
    // ------------------------------------------------------------
    public void beginAim(float x, float y) {
        aiming = true;
        aimStart.set(localToNorm(x, y));
        aimEnd.set(aimStart);
        computeDir();
    }

    public void updateAim(float x, float y) {
        if (!aiming) return;
        aimEnd.set(localToNorm(x, y));
        computeDir();
    }

    public void finishAim() {
        aiming = false;
    }

    private void computeDir() {
        Vector2 target = aiming ? aimEnd : aimStart;
        aimDir.set(target).sub(cueBall).nor();
        if (Float.isNaN(aimDir.x) || Float.isNaN(aimDir.y)) aimDir.setZero();
    }

    private Vector2 localToNorm(float x, float y) {
        float nx = MathUtils.clamp(x / getWidth(), 0f, 1f);
        float ny = MathUtils.clamp(y / getHeight(), 0f, 1f);
        return new Vector2(nx, ny);
    }

    // ------------------------------------------------------------
    // Rendering helpers
    // ------------------------------------------------------------
    private void drawGuideAndCue(Batch batch, float ballPx, float cbx, float cby) {
        // live power (0..1)
        float power = MathUtils.clamp(powerProvider != null ? powerProvider.get() : 0.5f, 0f, 1f);

        // Desired dotted length in pixels
        float desiredLenPx = MathUtils.lerp(ballPx * GUIDE_MIN_LEN_BALLS,
                ballPx * GUIDE_MAX_LEN_BALLS, power);

        // Clamp via physics if available
        float clampedLenPx = (showGuide && guideCapper != null)
                ? guideCapper.capGuideLenPx(new Vector2(cueBall), new Vector2(aimDir).nor(), desiredLenPx, ballPx)
                : desiredLenPx;

        // Guide styling
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

        // Cue stick
        if (showCue) {
            Sprite cueSprite = getCueSprite(ballPx, cbx, cby);
            cueSprite.draw(batch);
        }
    }

    private Sprite getCueSprite(float ballPx, float cbx, float cby) {
        float cueLen = ballPx * 7f;
        float cueH = ballPx * 0.5f;
        float kick = (float) Math.pow(cueKickT, 2) * ballPx * 0.7f;

        float offCenter = ballPx * 0.55f + kick + cueLen * 0.5f;

        // the cue center position
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

    /**
     * Supplies current cue power in [0..1].
     */
    public interface PowerProvider {
        float get();
    }

    /**
     * Caps the guide length (pixels) given:
     * - cuePosNorm: cue ball position in [0..1] table space,
     * - dirNorm: unit aim direction in [0..1] space,
     * - desiredLenPx: requested guide length in pixels,
     * - ballPx: cue ball radius in pixels (for stable px↔m conversion).
     */
    public interface GuideCapper {
        float capGuideLenPx(Vector2 cuePosNorm, Vector2 dirNorm, float desiredLenPx, float ballPx);
    }
}