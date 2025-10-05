package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.csse3200.game.components.Component;
import com.csse3200.game.services.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * Bulletproof Dialogue Display Component
 * This version handles all possible errors gracefully
 */
public class DialogueDisplay extends Component {

    private static final float DIALOGUE_WIDTH = 800f;
    private static final float DIALOGUE_HEIGHT = 150f;
    private static final float PADDING = 25f;

    private final List<Texture> disposableTextures = new ArrayList<>();
    private Table dialogueTable;
    private Label dialogueLabel;

    private String speaker = "";
    private String[] lines = new String[0];
    private int index = -1; // -1 means not started
    private boolean clickEnabled = false; // Only enable click listening once in update()

    @Override
    public void create() {
        super.create();
        createDialogueBox();
        if (entity != null) {
            NpcDialogueDataComponent data = entity.getComponent(NpcDialogueDataComponent.class);
            if (data != null) {
                bindData(data);
            }
        }
        if (dialogueTable != null) {
            hide();
            enableClickToNext();
        }
    }

    /**
     * Create and display dialogue box with maximum error handling
     */
    private void createDialogueBox() {
        // Get stage with error checking
        Stage stage = null;
        try {
            stage = ServiceLocator.getRenderService().getStage();
            if (stage == null) {
                System.err.println("CRITICAL: Stage is null!");
                return;
            }
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to get stage - " + e.getMessage());
            return;
        }

        // Load dialogue image with error handling
        Texture dialogueTexture = null;
        try {
            dialogueTexture = ServiceLocator.getResourceService()
                    .getAsset("images/NpcDialogue.png", Texture.class);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load dialogue image - " + e.getMessage());
            return;
        }

        // Create dialogue table with fixed size
        dialogueTable = new Table();

        // Fixed size (adjust these values as needed)
        float fixedWidth = 600f;   // Fixed width
        float fixedHeight = 150f;  // Fixed height

        dialogueTable.setSize(fixedWidth, fixedHeight);

        // Calculate position (bottom center of screen)
        float x = (stage.getWidth() - fixedWidth) / 2;
        float y = 120f;
        dialogueTable.setPosition(x, y);

        // Set background
        dialogueTable.setBackground(new TextureRegionDrawable(new TextureRegion(dialogueTexture)));

        // Create label with maximum error handling
        dialogueLabel = createBulletproofLabel();

        if (dialogueLabel == null) {
            System.err.println("CRITICAL: Failed to create any label!");
            return;
        }

        // Layout
        dialogueTable.add(dialogueLabel).expand().fill().pad(PADDING);

        // Add to stage
        stage.addActor(dialogueTable);
        dialogueTable.toFront();

        // Set sample text
        //dialogueLabel.setText("DIALOGUE BOX TEST - SUCCESS!");
        dialogueTable.setVisible(false);
        dialogueLabel.setColor(Color.BLACK);
    }

    /**
     * Create label
     */
    private Label createBulletproofLabel() {
        Label label = null;

        try {
            BitmapFont font = new BitmapFont();
            Label.LabelStyle style = new Label.LabelStyle();
            style.font = font;
            style.fontColor = Color.BLACK;

            label = new Label("", style);

            if (label != null) {
                label.setWrap(true);
                label.setAlignment(Align.center);
                label.setColor(Color.BLACK);
            }
        } catch (Exception e) {
            System.err.println("Failed to create label: " + e.getMessage());
            return null;
        }

        return label;
    }

    @Override
    public void update() {
        //Make sure to enable the listener only once
        if (!clickEnabled) {
            enableClickToNext();
            clickEnabled = true;
        }
    }

    @Override
    public void dispose() {
        super.dispose();

        if (dialogueLabel != null) dialogueLabel.remove();
        if (dialogueTable != null) dialogueTable.remove();

        for (Texture texture : disposableTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        disposableTextures.clear();
    }

    /**
     *Sets the dialogue text on the label (without changing visibility).
     * @param text the content to display; {@code null} is treated as empty
     */
    public void setText(String text) {
        try {
            if (dialogueLabel != null) {
                dialogueLabel.setText(text == null ? "" : text);
                dialogueLabel.setAlignment(Align.center);
            }
        } catch (Exception e) {
            System.err.println("setText failed: " + e.getMessage());
        }
    }

    public void show() {
        try {
            if (dialogueTable != null) dialogueTable.setVisible(true);
        } catch (Exception e) {
            System.err.println("show failed: " + e.getMessage());
        }
    }

    public void hide() {
        try {
            if (dialogueTable != null) dialogueTable.setVisible(false);
        } catch (Exception e) {
            System.err.println("hide failed: " + e.getMessage());
        }
    }

    /**
     * Binds the dialogue data to the internal cache of this component; does not display it immediately.
     * @param data the {@link NpcDialogueDataComponent} to read lines from
     */
    public void bindData(NpcDialogueDataComponent data) {
        try {
            if (data != null && !data.isEmpty()) {
                this.speaker = data.getName() == null ? "" : data.getName();
                this.lines = data.getLines();
                this.index = -1;
            } else {
                this.speaker = "";
                this.lines = new String[0];
                this.index = -1;
            }
        } catch (Exception e) {
            System.err.println("bindData failed: " + e.getMessage());
        }
    }

    public void showFirst() {
        try {
            if (dialogueTable != null && lines.length > 0) {
                this.index = 0;
                setLine(this.index);
                dialogueTable.setVisible(true);
            }
        } catch (Exception e) {
            System.err.println("showFirst failed: " + e.getMessage());
        }
    }

    /**
     *Shows the first line from the provided dialogue data.
     * @param data the {@link NpcDialogueDataComponent} to read lines from
     */
    public void showFirst(NpcDialogueDataComponent data) {
        try {
            bindData(data);
            showFirst();
        } catch (Exception e) {
            System.err.println("showFirst(data) failed: " + e.getMessage());
        }
    }

    public void nextOrHide() {
        try {
            if (dialogueTable != null && dialogueTable.isVisible()) {
                if (lines.length == 0) {
                    hide();
                } else {
                    if (index + 1 < lines.length) {
                        index = index + 1;
                        setLine(index);
                    } else {
                        hide();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("nextOrHide failed: " + e.getMessage());
        }
    }

    /**
     * Enable the "click to next sentence" listener for the dialog Table (only needs to be enabled once).
     */
    public void enableClickToNext() {
        try {
            if (dialogueTable != null) {
                dialogueTable.setTouchable(Touchable.enabled);
                dialogueTable.clearListeners(); // Prevent duplicate overlap
                dialogueTable.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        try {
                            nextOrHide();
                        } catch (Exception e) {
                            System.err.println("clicked->nextOrHide failed: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("enableClickToNext failed: " + e.getMessage());
        }
    }

    /**
     * Renders the i-th dialogue line using the current speaker prefix.
     * @param i zero-based index of the line to render
     */
    private void setLine(int i) {
        try {
            if (i >= 0 && i < lines.length) {
                String body = lines[i] == null ? "" : lines[i];
                setText((speaker == null || speaker.isEmpty()) ? body : (speaker + ": " + body));
            }
        } catch (Exception e) {
            System.err.println("setLine failed: " + e.getMessage());
        }
    }
}