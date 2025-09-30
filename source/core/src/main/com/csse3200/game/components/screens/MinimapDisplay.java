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
    private Image minimapImage;
    private boolean zoomedIn = false;
    private ScrollPane scrollPane;

    /**
     * Constructs a screen display bound to a game instance.
     *
     * @param game game instance for navigation (e.g., {@link #backMainMenu()})
     */
    public MinimapDisplay(GdxGame game) {
        super(game);
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

        // Ensure the minimap image sits above the dimmer (and HUD)
        root.toFront();

        addTitle(root, "Minimap", 2.0f, Color.WHITE, 24f);

        Texture minimapTexture = new Texture("images/minimap.png");
        minimapImage = new Image(new TextureRegionDrawable(minimapTexture));
        minimapImage.setSize(300, 300); // Adjust as needed

        // Wrap in a scroll pane for panning when zoomed in
        scrollPane = new ScrollPane(minimapImage, skin);
        scrollPane.setScrollingDisabled(true, true); // Disable by default

        Table minimapTable = new Table();
        minimapTable.setFillParent(true);
        minimapTable.add(scrollPane).center().width(300).height(300);

        root.addActor(minimapTable);

        // Click to zoom in
        minimapImage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!zoomedIn) {
                    // Calculate click position as a percentage of the minimap
                    float percentX = x / minimapImage.getWidth();
                    float percentY = y / minimapImage.getHeight();

                    // Zoom in the entire minimap image
                    float zoomedWidth = 600f;
                    float zoomedHeight = 600f;
                    minimapImage.setSize(zoomedWidth, zoomedHeight);
                    scrollPane.setScrollingDisabled(false, false);
                    zoomedIn = true;

                    // Scroll so the clicked spot is centered
                    // Calculate scroll position (scrollX/Y are 0-1)
                    float scrollX = percentX - (scrollPane.getWidth() / 2f) / zoomedWidth;
                    float scrollY = percentY - (scrollPane.getHeight() / 2f) / zoomedHeight;

                    // Clamp between 0 and 1
                    scrollX = Math.max(0f, Math.min(1f, scrollX));
                    scrollY = Math.max(0f, Math.min(1f, scrollY));

                    // Set scroll position
                    scrollPane.setScrollPercentX(scrollX);
                    scrollPane.setScrollPercentY(scrollY);
                }
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
        if (dimmer != null && dimmer.getStage() != null) {
            dimmer.remove();
            dimmer = null;
        }
        if (minimapImage != null) {
            minimapImage.remove();
            minimapImage = null;
        }
        if (scrollPane != null) {
            scrollPane.remove();
            scrollPane = null;
        }
        super.dispose();
    }
}