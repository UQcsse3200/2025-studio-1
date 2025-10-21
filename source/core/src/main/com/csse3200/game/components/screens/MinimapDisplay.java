package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.csse3200.game.GdxGame;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.math.Vector2;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimap overlay shown above the main game and HUD.
 * <p>
 * Renders a full-screen dimmer and a centered minimap image with actions:
 * <ul>
 *   <li><b>Scroll</b> - The minimap can be zoomed in or out with the scroll wheel.</li>
 *   <li><b>Arrow keys</b> - The minimap can be moved left or right with the left and right
 *   arrow keys respectively</li>
 *   <li><b>Touch Drag</b> - The minimap can be moved in either direction horizontally by
 *   clicking on the screen and dragging.</li>
 *   <li><b>Resume</b> - triggers the entity's {@code "resume"} event, that returns to the game.</li>
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
    private Table minimapTable;
    private Minimap minimap;
    private final Map<String, Texture> textures = new HashMap<>();

    /** Default image width for a room in pixels. */
    public static final int IMAGE_WIDTH = 1280;

    /**
     * Constructs a screen display bound to a game instance and minimap.
     *
     * @param game    game instance for navigation (e.g., {@link #backMainMenu()})
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
        minimap.zoom(-15);

        minimapTable = new Table();
        minimapTable.setFillParent(true);

        root.add(minimapTable).center().expand().fill();
        logger.debug("Created minimap actor");

        renderMinimapImages();
        minimapTable.setTouchable(Touchable.enabled);
        stage.setScrollFocus(minimapTable);

        minimapTable.addListener(new InputListener() {
            private float lastX;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lastX = x;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                float deltaX = x - lastX;

                minimap.pan(new Vector2(-deltaX, 0));

                clampMinimapPosition(-(deltaX), true);
                renderMinimapImages();

                lastX = x;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                zoom(x, y, amountY);
                return true;
            }
        });
    }

    /**
     * Zooms in on the minimap by 25% and renders the zoomed in map
     */
    public void zoomIn() {
        float currentScale = minimap.getScale();
        if (currentScale < 5.0f) {
            minimap.zoom(25f);
        }
        renderMinimapImages();
    }

    /**
     * Zooms out the minimap by 25% and renders the zoomed out map
     */
    public void zoomOut() {
        float currentScale = minimap.getScale();
        if (currentScale > 0.2f) {
            minimap.zoom(-25f);
        }
        renderMinimapImages();
    }

    /**
     * Pans the minimap by the coordinates given and renders it
     *
     * @param direction The direction of panning
     */
    public void pan(String direction) {
        float panDistance = 0f;
        if (direction.equals("left")) {
            panDistance = -(IMAGE_WIDTH * minimap.getScale());
            minimap.pan(new Vector2(panDistance, 0));
        }
        if (direction.equals("right")) {
            panDistance = IMAGE_WIDTH * minimap.getScale();
            minimap.pan(new Vector2(panDistance, 0));
        }

        clampMinimapPosition(panDistance, false);
        renderMinimapImages();
    }

    /**
     * Clamps the minimap so that it cannot be dragged out of screen and be lost.
     *
     * @param panDistance    The distance by which the minimap was panned.
     * @param isTouchDragged True if the pan was caused by dragging with mouse false otherwise
     */
    void clampMinimapPosition(float panDistance, boolean isTouchDragged) {
        Map<Vector2, String> visibleRooms = minimap.render();
        if (visibleRooms == null) {
            return;
        }
        if (panDistance > 0.0f && isTouchDragged && visibleRooms.size() >= 2) {
            return;
        }
        if (panDistance < 0.0f && isTouchDragged && visibleRooms.size() >= 2) {
            return;
        }
        if (panDistance < 0.0f && visibleRooms.size() < 3) {
            minimap.pan(new Vector2(-panDistance, 0));
        } else if (panDistance > 0.0f && visibleRooms.size() < 2) {
            minimap.pan(new Vector2(-panDistance, 0));
        }
    }

    /**
     * Renders each of the rooms that need to be displayed in the minimap
     */
    void renderMinimapImages() {
        // Clear old images
        minimapTable.clearChildren();

        // Render all visible rooms as images
        Map<Vector2, String> visibleRooms = minimap.render();
        if (visibleRooms == null) {
            return;
        }

        for (Map.Entry<Vector2, String> entry : visibleRooms.entrySet()) {
            String imagePath = entry.getValue();
            Vector2 screenPos = entry.getKey();

            // Reuse texture if already loaded
            Texture texture = textures.computeIfAbsent(imagePath, Texture::new);

            Image roomImage = new Image(new TextureRegionDrawable(texture));
            roomImage.setScale(minimap.getScale(), minimap.getScale());

            // Set position so the center is at (screenPos.x, screenPos.y)
            roomImage.setPosition(
                    screenPos.x - Minimap.IMAGE_WIDTH * minimap.getScale() / 2,
                    screenPos.y - Minimap.IMAGE_HEIGHT * minimap.getScale() / 2
            );
            minimapTable.addActor(roomImage);
        }
    }

    /**
     * Zooms in or out on the minimap by 25% depending on the scroll direction
     * and renders the zoomed in map at the position of the cursor
     *
     * @param x       x coordinate of the mouse cursor
     * @param y       y coordinate of the mouse cursor
     * @param amountY The scroll direction, negative is scroll down i.e., zoom out and
     *                positive is scroll up i.e., zoom in
     */
    void zoom(float x, float y, float amountY) {
        float oldScale = minimap.getScale();

        // Scroll up = zoom in; Scroll down = zoom out
        float percentChange;
        if (amountY > 0) {
            percentChange = 25f;
        } else if (amountY < 0) {
            percentChange = -25f;
        } else {
            percentChange = 0;
        }

        minimap.zoom(percentChange);
        float newScale = minimap.getScale();

        // Zoom in on cursor
        Vector2 mousePos = new Vector2(x, y);
        Vector2 centre = minimap.getCentre();

        Vector2 beforeZoomWorld = mousePos.cpy()
                .sub(minimapTable.getWidth() / 2f, minimapTable.getHeight() / 2f)
                .scl(1 / oldScale)
                .add(centre);

        Vector2 afterZoomWorld = mousePos.cpy()
                .sub(minimapTable.getWidth() / 2f, minimapTable.getHeight() / 2f)
                .scl(1 / newScale)
                .add(centre);

        Vector2 panOffset = beforeZoomWorld.cpy().sub(afterZoomWorld);
        minimap.pan(panOffset.scl(newScale));

        renderMinimapImages();

        logger.info("Minimap zoomed in to point {} x, {} y", x, y);
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
        minimap = null;
        if (stage != null) {
            if (stage.getScrollFocus() == minimapTable) {
                stage.setScrollFocus(null);
            }
            if (stage.getKeyboardFocus() == minimapTable) {
                stage.setKeyboardFocus(null);
            }
        }
        if (dimmer != null && dimmer.getStage() != null) {
            dimmer.remove();
            dimmer = null;
        }
        if (minimapTable != null) {
            minimapTable.clear();
            minimapTable = null;
        }
        // Dispose cached textures
        for (Texture tex : textures.values()) {
            if (tex != null) {
                tex.dispose();
            }
        }
        textures.clear();
        super.dispose();
    }
}