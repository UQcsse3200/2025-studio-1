package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * Debug/temporary key handler for the Guidance NPC.
 *
 * - Y to open menu (only if player is in proximity)
 * - 6 for "Instructions"
 */
public class NpcDebugKeyInteractionComponent extends Component {
    /** Controller for NPC menu/instruction actions. Must be present on the same entity. */
    private NpcTwoOptionMenuComponent menu;
    private NpcProximityGateComponent gate;

    /**
     * Called when the component is added to an entity.
     * Grabs references to required sibling components.
     */
    @Override
    public void create() {
        menu = entity.getComponent(NpcTwoOptionMenuComponent.class);
        gate = entity.getComponent(NpcProximityGateComponent.class);
    }

    /**
     * Per-frame input polling.
     * <ul>
     *   <li>If <b>Y</b> is pressed and the player is within the gate radius, opens the NPC menu.</li>
     *   <li>If <b>6</b> (top row or numpad) is pressed, shows the instruction text.</li>
     * </ul>
     * No-op if the required components are missing.
     */
    @Override
    public void update() {
        if (menu == null || gate == null) return;

        // Open menu
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            if (gate.isPlayerInRange()) {
                System.out.println("[GuideNPC] Y pressed: opening menu");
                menu.open();
            } else {
                System.out.println("[GuideNPC] Y pressed: player NOT in range");
            }
        }

        // Choose options (simple, for testing)
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_6)) {
            System.out.println("[GuideNPC] 6 pressed: instructions");
            menu.chooseInstructions();
        }
    }
}
