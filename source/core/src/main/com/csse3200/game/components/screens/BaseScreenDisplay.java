package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Abstract base for screen/overlay UI components.
 * <p>
 * Centralises common wiring for Scene2D-based screens:
 * <ul>
 *   <li>Creates and adds a fill-parent {@link Table} root to the stage</li>
 *   <li>Initialises shared {@link NeonStyles} for buttons</li>
 *   <li>Provides helpers for adding titles, body text, and buttons</li>
 *   <li>Manages auto-disposal of helper-created {@link Texture} assets</li>
 * </ul>
 * Subclasses implement {@link #buildUI(Table)} to construct their specific UI.
 */
public abstract class BaseScreenDisplay extends UIComponent {
    /**
     * Class-scoped logger for subclasses.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * Game reference used for screen navigation helpers.
     */
    protected final GdxGame game;

    /**
     * Root table added to the stage; fill-parent and centered.
     */
    protected Table root;
    /**
     * Shared style builder for rounded neon buttons.
     */
    protected NeonStyles neon;
    /**
     * Textures created via helpers and disposed automatically.
     */
    private final ArrayList<Texture> managedTextures = new ArrayList<>();

    /**
     * Constructs a screen display bound to a game instance.
     *
     * @param game game instance for navigation (e.g., {@link #backMainMenu()})
     */
    protected BaseScreenDisplay(GdxGame game) {
        this.game = game;
    }

    /**
     * Initialises common UI infrastructure and delegates to {@link #buildUI(Table)}.
     * <p>
     * Creates {@link #root}, adds it to the stage, initialises {@link #neon},
     * and then calls {@link #buildUI(Table)} for subclass-specific layout.
     */
    @Override
    public void create() {
        super.create();
        neon = new NeonStyles(0.70f);
        root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);
        buildUI(root);
    }

    /**
     * Subclasses implement their UI here using the provided {@code root} table.
     *
     * @param root a fill-parent, centered {@link Table} already added to the stage
     */
    protected abstract void buildUI(Table root);

    // ---------- Helpers ----------

    /**
     * Adds a title label using the skin's {@code "title"} style.
     *
     * @param to        table to add the title to
     * @param text      title text
     * @param fontScale scale for the title font
     * @param color     optional tint colour for the title (may be {@code null})
     * @param padBottom bottom padding applied after the title row
     * @return the created {@link Label}
     */
    protected Label addTitle(Table to, String text, float fontScale, Color color, float padBottom) {
        // Clone the style so we don't mutate the shared skin style
        Label.LabelStyle base = skin.get("title", Label.LabelStyle.class);
        Label.LabelStyle style = new Label.LabelStyle(base);
        if (color != null) style.fontColor = color;

        Label title = new Label(text, style);
        title.setFontScale(fontScale);

        to.add(title).colspan(2).center().padBottom(padBottom);
        to.row();
        return title;
    }

    /**
     * Adds a body label by cloning the skin's {@code "small"} style and forcing a white font colour.
     *
     * @param to        table to add the body text to
     * @param text      body text
     * @param fontScale scale for the body font
     * @param padBottom bottom padding applied after the body row
     * @return the created {@link Label}
     */
    protected Label addBody(Table to, String text, float fontScale, float padBottom) {
        Label.LabelStyle baseSmall = skin.get("small", Label.LabelStyle.class);
        Label.LabelStyle small = new Label.LabelStyle(baseSmall);
        small.fontColor = skin.getColor("white");
        Label lbl = new Label(text, small);
        lbl.setFontScale(fontScale);
        to.add(lbl).colspan(2).center().padBottom(padBottom);
        to.row();
        return lbl;
    }

    /**
     * Creates a rounded neon {@link TextButton} and binds the given action.
     *
     * @param text       button label
     * @param labelScale font scale applied to the label
     * @param onClick    action executed on {@link ChangeListener} event (may be {@code null})
     * @return the configured {@link TextButton}
     */
    protected TextButton button(String text, float labelScale, Runnable onClick) {
        TextButton b = new TextButton(text, neon.buttonRounded());
        b.getLabel().setFontScale(labelScale);
        b.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                logger.debug("{} clicked on {}", text, getClass().getSimpleName());
                if (onClick != null) onClick.run();
            }
        });
        return b;
    }

    /**
     * Creates a solid-colour {@link Texture} and tracks it for auto-disposal.
     *
     * @param c colour (including alpha)
     * @return the created {@link Texture}
     */
    protected Texture makeSolidTexture(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        managedTextures.add(t);
        return t;
    }

    /**
     * Creates an {@link Image} backed by a 1x1 solid-colour texture.
     * The underlying texture is tracked and disposed automatically.
     *
     * @param c colour (including alpha)
     * @return an {@link Image} using the generated texture
     */
    protected Image solidImage(Color c) {
        Texture t = makeSolidTexture(c);
        return new Image(new TextureRegionDrawable(new TextureRegion(t)));
    }

    /**
     * Convenience helper to navigate back to the main menu.
     * Subclasses can call this in button handlers.
     */
    protected void backMainMenu() {
        logger.debug("Switching to MAIN_MENU from {}", getClass().getSimpleName());
        game.setScreen(ScreenType.MAIN_MENU);
    }

    /**
     * Stage handles rendering of attached actors.
     */
    @Override
    public void draw(SpriteBatch batch) { /* Stage draws itself */ }

    /**
     * Default z-index; override in overlays that must sit above other UI.
     */
    @Override
    public float getZIndex() {
        return 2f;
    }

    /**
     * Removes the root table from the stage and disposes any helper-created textures.
     * Subclasses overriding this should remove any actors that reference textures
     * before calling {@code super.dispose()} to avoid rendering disposed resources.
     */
    @Override
    public void dispose() {
        if (root != null) root.remove();
        for (Texture t : managedTextures) if (t != null) t.dispose();
        managedTextures.clear();
        super.dispose();
    }
}
