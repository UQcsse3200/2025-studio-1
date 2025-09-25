package com.csse3200.game.components.minigames.slots;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.csse3200.game.components.Component;

/** Press '9' to toggle the Slots game UI. */
public class SlotsHotkeyComponent extends Component {
    private final SlotsDisplay display;

    public SlotsHotkeyComponent(SlotsDisplay display) {
        this.display = display;
    }

    @Override
    public void create() {
        super.create();

        // Chain our adapter into whatever input processor is already active
        var current = Gdx.input.getInputProcessor();
        InputMultiplexer mux = (current instanceof InputMultiplexer)
                ? (InputMultiplexer) current
                : new InputMultiplexer(current);

        mux.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.NUM_9 || keycode == Input.Keys.NUM_9) {
                    // Toggle the display
                    if (displayIsVisible()) display.hide(); else display.show();
                    return true; // handled
                }
                return false;
            }
        });

        Gdx.input.setInputProcessor(mux);
    }

    private boolean displayIsVisible() {
        // If you track visibility with a flag, read it; otherwise infer from root visibility:
        return display != null && display.getStage() != null
                && display.getStage().getRoot().isVisible();
    }
}
