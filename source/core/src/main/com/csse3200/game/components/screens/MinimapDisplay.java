package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.Texture;
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

import java.util.HashMap;
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
    private Table minimapTable;
    private Minimap minimap;
    private final Map<String, Texture> textures = new HashMap<>();

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

        root.add(minimapTable).center().expand().fill();
        logger.info("Created minimap actor");

        renderMinimapImages();

        minimapTable.addListener(new InputListener() {
            private float lastX, lastY;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                lastX = x;
                lastY = y;
                return true;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                float deltaX = x - lastX;
                float deltaY = y - lastY;

                minimap.pan(new Vector2(-deltaX, -deltaY)); // drag = pan

                minimapTable.clearChildren();
                renderMinimapImages();

                lastX = x;
                lastY = y;
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                zoom(x, y, amountY);
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
            Texture texture = textures.get(imagePath);
            if (texture == null) {
                texture = new Texture(imagePath);
                textures.put(imagePath, texture);
            }

            Image roomImage = new Image(new TextureRegionDrawable(texture));
            float imageWidth = roomImage.getWidth();
            float imageHeight = roomImage.getHeight();

            // Set position so the center is at (screenPos.x, screenPos.y)
            roomImage.setPosition(screenPos.x - imageWidth / 2, screenPos.y - imageHeight / 2);
            //roomImage.setSize(128, 72);
            minimapTable.addActor(roomImage);
        }
    }

    private void zoom(float x, float y, float amountY) {
        float oldScale = minimap.getScale();
        float percentChange = -amountY * 10f; // scroll up = zoom in
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

        minimapTable.clearChildren();
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