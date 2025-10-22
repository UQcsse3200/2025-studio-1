package com.csse3200.game.components.screens;

import com.badlogic.gdx.Input;
import com.csse3200.game.input.InputComponent;

/**
 * High-priority input component that closes the pause overlay when ESC is pressed.
 * Attached to the pause overlay entity so it is only active while the overlay is visible.
 */
public class PauseEscInputComponent extends InputComponent {
    public PauseEscInputComponent(int priority) {
        super(priority);
    }

    @Override
    public boolean keyPressed(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            // Trigger the pause overlay's resume/hide
            if (entity != null) {
                entity.getEvents().trigger("resume");
            }
            PauseMenuDisplay.markEscConsumed();
            return true; // consume ESC so other handlers don't reopen pause this frame
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keycode) {
        return false;
    }
}

