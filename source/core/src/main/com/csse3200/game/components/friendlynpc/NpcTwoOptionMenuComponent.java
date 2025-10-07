package com.csse3200.game.components.friendlynpc;

import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;

/**
 * A simple two-option interaction menu for NPCs.
 * Option 1: Shows an instruction line.
 * Option 2: Makes the NPC lead the player to a location.
 *
 * You can trigger open() when the player presses the interact key near the NPC.
 */
public class NpcTwoOptionMenuComponent extends Component {
    private String instruction =
            "Press E to collect weapons.\nPress F to summon your partner.\nPress Y to talk to me.\nStay near me to interact.";

    public void open() {
        DialogueDisplay ui = entity.getComponent(DialogueDisplay.class);
        if (ui != null) {
            ui.setText(
                    "[Guide Menu]\n" +
                    "1) Instructions - controls & tips\n" +
                    "2) Lead me there - escort to next objective\n" +
                    "Stay near me to interact.\n" +
                    "(Shortcut: 6 for quick tips)"
            );
            ui.show();
        } else {
            System.out.println("[GuideNPC] Open menu: (no DialogueDisplay attached)");
        }
    }

    public void chooseInstructions() {
        DialogueDisplay ui = entity.getComponent(DialogueDisplay.class);
        if (ui != null) {
            ui.setText(instruction);
            ui.show();
        }
    }

    public void chooseLead() {
        NpcLeadComponent lead = entity.getComponent(NpcLeadComponent.class);
        if (lead != null) {
            lead.begin();
        }
    }
}
