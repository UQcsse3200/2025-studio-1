package com.csse3200.game.components.friendlynpc;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
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

    @Override
    public void create() {
        super.create();

        try {
            createDialogueBox();
        } catch (Exception e) {
            System.err.println("DialogueDisplay creation failed: " + e.getMessage());
            e.printStackTrace();
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
        dialogueLabel.setText("DIALOGUE BOX TEST - SUCCESS!");
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
}