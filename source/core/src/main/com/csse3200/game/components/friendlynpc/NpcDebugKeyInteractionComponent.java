package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/**
 * TEMPORARY: Keyboard-only interaction so you can test immediately.
 * - E to open menu (only if player is in proximity)
 * - 1 for "Instructions"
 * - 2 for "Lead me there"
 */
public class NpcDebugKeyInteractionComponent extends Component {
    private NpcTwoOptionMenuComponent menu;
    private NpcProximityGateComponent gate;

    @Override
    public void create() {
        menu = entity.getComponent(NpcTwoOptionMenuComponent.class);
        gate = entity.getComponent(NpcProximityGateComponent.class);
    }

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
