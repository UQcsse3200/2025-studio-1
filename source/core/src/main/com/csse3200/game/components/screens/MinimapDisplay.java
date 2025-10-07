package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Vector2;

import java.util.Map;

/**
 * Minimap overlay shown above the main game and HUD.
 * <p>
 * Renders a full-screen dimmer and a centered minimap image with actions:
 * <ul>
 *   <li><b>Click on a spot</b> — triggers the entity's {@code "zoom in"} event,
 *   that is, the minimap zooms in on the clicked spot.</li>
 *   <li><b>Scroll</b> — Once zoomed in, the minimap is scrollable using the mouse scroll wheel.</li>
 *   <li><b>Resume</b> — triggers the entity's {@code "resume"} event, that returns to the game.</li>
 * </ul>
 * The dimmer is non-interactive (input disabled) so button clicks reach the minimap.
 * TAB is handled once to immediately resume gameplay.
 */
public class MinimapDisplay extends BaseScreenDisplay {
    /**
     * Full-screen dimmer image. Kept as a field so we can avoid adding duplicates
     * when the minimap overlay is opened multiple times and to remove it in {@link #dispose()}.
     */
    private Image dimmer;
    private ScrollPane scrollPane;
    private Table minimapTable;
    private Minimap minimap;
    private boolean zoomedIn = false;

    /**
     * Constructs a screen display bound to a game instance.
     *
     * @param game game instance for navigation (e.g., {@link #backMainMenu()})
     */
    public MinimapDisplay(GdxGame game) {
        super(game);
    }

    /**
     * Constructs a screen display bound to a game instance and minimap.
     *
     * @param game game instance for navigation (e.g., {@link #backMainMenu()})
     * @param minimap The minimap object made by {@link Minimap}
     */
    public MinimapDisplay(GdxGame game, Minimap minimap) {
        super(game);
        this.minimap = minimap;
    }

    /**
     * Builds the minimap UI:
     * <ol>
     *   <li>Add a full-screen, non-interactive dimmer (placed on the Stage)</li>
     *   <li>Bring {@code root} to the front so the minimap image sits above the dimmer/HUD</li>
     *   <li>Add title and zoom in listener</li>
     *   <li>Capture the first TAB key press to resume and remove the listeners</li>
     * </ol>
     *
     * @param root the fill-parent root table created by {@link BaseScreenDisplay}
     */
    @Override
    protected void buildUI(Table root) {
        // Dimmer using skin
        if (dimmer == null) {
            dimmer = new Image(skin.newDrawable("white", new Color(0, 0, 0, 0.7f)));
            dimmer.setFillParent(true);
            dimmer.setTouchable(Touchable.disabled); // Clicks outside the minimap aren't registered
        }

        // Ensure that the dimmer is set to the right stage
        if (dimmer.getStage() == null) {
            stage.addActor(dimmer);
        }
        logger.debug("Created translucent background");

        // Ensure the minimap image sits above the dimmer (and HUD)
        root.toFront();

        addTitle(root, "Minimap", 2.0f, Color.WHITE, 24f);

        // Minimap rendering
        minimap.open();

        minimapTable = new Table();
        minimapTable.setFillParent(true);

        // Wrap in a scroll pane for panning when zoomed in
        scrollPane = new ScrollPane(minimapTable, skin);
        scrollPane.setScrollingDisabled(true, true); // Disable by default

        root.add(scrollPane).center().width(minimapTable.getWidth()).height(minimapTable.getHeight());
        logger.debug("Created minimap actor");

        // Click to zoom in
        minimapTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!zoomedIn) {
                    zoomInTouch(x, y);
                } else {
                    zoomOutTouch();
                }
                return true;
            }
        });

        scrollPane.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                minimap.zoom(amountY * 10); // Zoom out/in by scroll
                minimapTable.clearChildren();
                renderMinimapImages();
                return true;
            }
        });

        // Resume on tab
        stage.addListener(new InputListener() {
            /** Prevents repeated TAB events. */
            private boolean handled = false;

            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.TAB && !handled) {
                    handled = true;
                    entity.getEvents().trigger("resume");
                    root.removeListener(this);
                    return true;
                }
                return false;
            }
        });
    }

    private void renderMinimapImages() {
        // Render all visible rooms as images
        Map<String, Vector2> visibleRooms = minimap.render();
        for (Map.Entry<String, Vector2> entry : visibleRooms.entrySet()) {
            String imagePath = entry.getKey();
            Vector2 screenPos = entry.getValue();
            Texture texture = new Texture(imagePath);
            Image roomImage = new Image(new TextureRegionDrawable(texture));
            roomImage.setSize(128, 72); // Thumbnail size, adjust as needed
            roomImage.setPosition(screenPos.x, screenPos.y);
            minimapTable.addActor(roomImage);
        }
    }

    private void zoomInTouch(float x, float y) {
        zoomedIn = true;
        // Calculate percent position
        float percentX = x / minimapTable.getWidth();
        float percentY = y / minimapTable.getHeight();
        // Zoom in backend
        minimap.zoom(100);
        scrollPane.setScrollingDisabled(false, false);

        // Pan so clicked spot is centered
        Vector2 panVector = new Vector2(
                (percentX - 0.5f) * minimapTable.getWidth(),
                (percentY - 0.5f) * minimapTable.getHeight()
        );
        minimap.pan(panVector);

        // Re-render minimap
        minimapTable.clearChildren();
        renderMinimapImages();

        logger.info("Minimap zoomed in to point {} x, {} y", x, y);
    }

    private void zoomOutTouch() {
        // Zoom out to show the entire minimap image
        minimap.reset();
        scrollPane.setScrollingDisabled(true, true);
        logger.info("Scrolling has been disabled for minimap");
        zoomedIn = false;

        // Re-render minimap
        minimapTable.clearChildren();
        renderMinimapImages();

        logger.info("Minimap zoomed out");
    }

    /**
     * High z-index to ensure the minimap overlay renders above other UI layers.
     *
     * @return draw order value for this UI component
     */
    @Override
    public float getZIndex() {
        return 100f;
    }

    /**
     * Removes the dimmer, minimap image and the scroll pane from the Stage
     * and calls the {@link BaseScreenDisplay} dispose.
     * Ensures no stale dimmer instances remain if the overlay is recreated later.
     */
    @Override
    public void dispose() {
        minimap.close();
        if (dimmer != null && dimmer.getStage() != null) {
            dimmer.remove();
            dimmer = null;
        }
        if (minimapTable != null) {
            minimapTable.clear();
            minimapTable = null;
        }
        if (scrollPane != null) {
            scrollPane.remove();
            scrollPane = null;
        }
        super.dispose();
    }
}