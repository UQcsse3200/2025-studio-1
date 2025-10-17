package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Teleporter behaviour:
 *  - Idle: only a static frame (TeleporterIdleRenderComponent) is shown (no animation running).
 *  - Player presses E within radius -> menu shows discovered destinations.
 *  - Selecting a destination plays the teleporter atlas animation for a short delay (TELEPORT_DELAY).
 *  - After delay, area transition occurs, animation stops, static frame returns.
 *  (No scaling/zoom effects are applied during activation.)
 */
public class TeleporterComponent extends Component {
    private static final float TELEPORT_DELAY = 0.65f; // seconds

    private TeleporterMenuUI menuUI;
    private boolean menuVisible;

    private static boolean escConsumedThisFrame;
    private boolean teleporting;
    private float teleportTimer;
    private String pendingDestination;
    private boolean playerInRange = false;

    private final Vector2 baseScale = new Vector2();
    private boolean baseScaleSet;

    private GameTime time;

    public static boolean wasEscConsumedThisFrame() { return escConsumedThisFrame; }
    public static void markEscConsumed() { escConsumedThisFrame = true; }
    public static void resetEscConsumed() { escConsumedThisFrame = false; }

    @Override
    public void create() {
        time = ServiceLocator.getTimeSource();
        if (menuUI == null) {
            menuUI = entity.getComponent(TeleporterMenuUI.class);
        }
        // Ensure animator is stopped and idle visible
        TeleporterIdleRenderComponent idle = entity.getComponent(TeleporterIdleRenderComponent.class);
        if (idle != null) idle.setVisible(true);
        AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc != null) arc.stopAnimation();

        entity.getEvents().addListener("interact", this::handleInteract);
        entity.getEvents().addListener("enteredInteractRadius", this::playerEnteredRange);
        entity.getEvents().addListener("exitedInteractRadius", this::playerExitedRange);
    }

    @Override
    public void update() {
        if (teleporting) {
            updateActivation();
        }

        // Sync internal state with UI component (user may press the UI Close button)
        if (menuUI != null && menuVisible && !menuUI.isVisible()) {
            menuVisible = false;
        }

        boolean esc = Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE);

        if (menuVisible && esc && playerInRange) { // only mark consumed when ESC actually closes teleporter menu
            markEscConsumed();
            hideMenu();
        }
    }

    // Again very messy way to do the labels but oh well
    private void playerEnteredRange() {
        playerInRange = true;
        showLabel();
    }

    private void playerExitedRange() {
        playerInRange = false;
        hideLabel();
        if (menuVisible) {
            hideMenu();
        }
    }

    private void handleInteract() {
        if (menuVisible) {
            hideMenu();
        } else {
            showMenu();
        }
    }

    private void showMenu() {
        hideLabel();
        if (teleporting) return;
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        if (ds == null) {
            Gdx.app.log("Teleporter", "DiscoveryService missing - cannot open menu");
            return;
        }
        if (menuUI == null) {
            Gdx.app.error("Teleporter", "TeleporterMenuUI not present on entity; cannot open menu");
            return;
        }
        menuUI.refresh();
        menuUI.setVisible(true);
        menuVisible = true;
        Gdx.app.log("Teleporter", "Menu opened");
    }

    private void hideMenu() {
        showLabel();
        if (menuUI != null) menuUI.setVisible(false);
        menuVisible = false;
        Gdx.app.log("Teleporter", "Menu closed");
    }

    private void showLabel() {
        Label interactLabel = ServiceLocator.getPrompt();
        interactLabel.setText("Press E to interact with Teleporter");
        interactLabel.setVisible(true);
    }

    private void hideLabel() {
        Label interactLabel = ServiceLocator.getPrompt();
        interactLabel.setVisible(false);
    }

    /** Called by TeleporterMenuUI when a destination is selected. */
    public void startTeleport(String destination) {
        if (teleporting || destination == null || destination.isEmpty()) return;
        pendingDestination = destination;
        teleporting = true;
        teleportTimer = TELEPORT_DELAY;
        hideMenu();

        if (!baseScaleSet) {
            baseScale.set(entity.getScale());
            baseScaleSet = true;
        }

        // Hide static frame
        TeleporterIdleRenderComponent idle = entity.getComponent(TeleporterIdleRenderComponent.class);
        if (idle != null) idle.setVisible(false);

        // Start activation animation
        AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc != null) {
            if (arc.hasAnimation("teleporter1")) {
                arc.startAnimation("teleporter1");
            } else if (arc.hasAnimation("teleporter1_fast")) {
                arc.startAnimation("teleporter1_fast");
            } else {
                Gdx.app.log("Teleporter", "No teleporter animation present – activation will be static");
            }
        }
        Gdx.app.log("Teleporter", "Activation started for " + destination);
    }

    private void updateActivation() {
        float dt = time != null ? time.getDeltaTime() : 1/60f;
        teleportTimer -= dt;
        // No scale/zoom effect: keep original scale the whole time.
        if (teleportTimer <= 0f) {
            performTeleport();
        }
    }

    private void performTeleport() {
        // Restore scale
        entity.setScale(baseScale.x, baseScale.y);
        // Stop animation
        AnimationRenderComponent arc = entity.getComponent(AnimationRenderComponent.class);
        if (arc != null) arc.stopAnimation();
        // Show idle static frame again
        TeleporterIdleRenderComponent idle = entity.getComponent(TeleporterIdleRenderComponent.class);
        if (idle != null) idle.setVisible(true);

        Gdx.app.log("Teleporter", "Teleporting to " + pendingDestination);
        var area = ServiceLocator.getGameArea();
        if (area != null) {
            area.transitionToArea(pendingDestination);
        }
        teleporting = false;
        pendingDestination = null;
    }
}
