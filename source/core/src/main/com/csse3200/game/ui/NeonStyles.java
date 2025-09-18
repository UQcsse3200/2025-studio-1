package com.csse3200.game.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

/**
 * Builds a neon, rounded button style for Scene2D.
 */
public class NeonStyles {
    private final float fillAlpha;
    private final BitmapFont font = new BitmapFont();
    private Texture tUp, tOver, tDown;

    /**
     * Creates a style helper with a default fill alpha of 0.70.
     */
    public NeonStyles() {
        this(0.70f);
    }

    /**
     * Creates a style helper with a custom inner fill alpha.
     */
    public NeonStyles(float fillAlpha) {
        this.fillAlpha = Math.max(0f, Math.min(1f, fillAlpha));
    }

    /**
     * Builds a {@link TextButtonStyle} with rounded corners and a neon rim.
     */
    public TextButtonStyle buttonRounded() {
        // Panel fill colours
        Color fillIdle = new Color(0f, 0.18f, 0.28f, fillAlpha);
        Color fillHover = new Color(0.00f, 0.38f, 0.62f, fillAlpha);
        Color fillDown = new Color(0.25f, 0.08f, 0.28f, fillAlpha);

        // Rim colours
        Color rimCyan = new Color(0.00f, 0.90f, 1.00f, 1f);
        Color rimPink = new Color(1.00f, 0.22f, 0.68f, 1f);

        fillHover.a = Math.min(1f, fillHover.a + 0.30f);

        int radius = 22;
        int border = 4;
        int innerR = radius - border;
        int split = radius + border;

        TextButtonStyle s = new TextButtonStyle();
        s.up = makeRounded(fillIdle, rimCyan, radius, innerR, border, split, true);
        s.over = makeRounded(fillHover, rimCyan, radius, innerR, border, split, true);
        s.down = makeRounded(fillDown, rimPink, radius, innerR, border, split, true);

        // Font and font colours
        s.font = font;
        s.fontColor = new Color(0.70f, 0.95f, 1f, 1f);
        s.overFontColor = Color.WHITE;
        s.downFontColor = new Color(1f, 0.80f, 0.92f, 1f);
        return s;
    }

    /**
     * Creates a rounded NinePatchDrawable with a neon rim and translucent fill.
     */
    private NinePatchDrawable makeRounded(
            Color fill, Color rim, int radius, int innerR, int border, int split, boolean flattenCenter) {

        int w = 256, h = 96;
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();

        // Draw outer rim ring (rounded)
        drawRound(pm, 0, 0, w, h, radius, rim);

        // Hollow out inside
        Pixmap.Blending old = pm.getBlending();
        pm.setBlending(Pixmap.Blending.None);
        pm.setColor(0, 0, 0, 0);
        drawRound(pm, border, border, w - 2 * border, h - 2 * border, innerR, new Color(0, 0, 0, 0));
        pm.setBlending(old);

        // Rounded inner fill
        drawRound(pm, border, border, w - 2 * border, h - 2 * border, innerR, fill);

        if (flattenCenter) {
            pm.setColor(fill);
            pm.fillRectangle(split, border, w - 2 * split, h - 2 * border);
        }

        Texture tex = new Texture(pm);
        tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pm.dispose();

        if (tUp == null) tUp = tex;
        else if (tOver == null) tOver = tex;
        else tDown = tex;

        NinePatch nine = new NinePatch(new TextureRegion(tex), split, split, split, split);
        return new NinePatchDrawable(nine);
    }

    /**
     * Fills a rounded rectangle into the given Pixmap.
     */
    private static void drawRound(Pixmap pm, int x, int y, int w, int h, int r, Color c) {
        pm.setColor(c);
        pm.fillRectangle(x + r, y, w - 2 * r, h);
        pm.fillRectangle(x, y + r, w, h - 2 * r);
        pm.fillCircle(x + r, y + r, r);
        pm.fillCircle(x + w - r - 1, y + r, r);
        pm.fillCircle(x + r, y + h - r - 1, r);
        pm.fillCircle(x + w - r - 1, y + h - r - 1, r);
    }

    /**
     * Frees textures created by this class and the font used in the style.
     */
    public void dispose() {
        if (tUp != null) tUp.dispose();
        if (tOver != null) tOver.dispose();
        if (tDown != null) tDown.dispose();
        font.dispose();
    }
}
