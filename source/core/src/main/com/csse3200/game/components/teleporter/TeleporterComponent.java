package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component for an in-world teleporter prop. When the player is nearby and presses 'E',
 * a menu listing discovered rooms appears. Selecting a room transitions the game area.
 * 'ESC' closes the menu.
 */
public class TeleporterComponent extends Component {
    private static final float ACTIVATION_RADIUS = 2.5f; // increased from 2.0f
    private TeleporterMenuUI menuUI; // lazily created
    private boolean menuVisible = false;

    @Override
    public void update() {
        Entity player = ServiceLocator.getPlayer();
        if (player == null) return;

        if (menuVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || !isPlayerClose(player)) {
                hideMenu();
            }
            return;
        }

        if (isPlayerClose(player) && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            showMenu();
        }
    }

    private boolean isPlayerClose(Entity player) {
        Vector2 p = player.getPosition();
        Vector2 t = entity.getPosition();
        return p.dst2(t) <= ACTIVATION_RADIUS * ACTIVATION_RADIUS;
    }

    private void showMenu() {
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        if (ds == null) {
            Gdx.app.log("Teleporter", "DiscoveryService missing - cannot open menu");
            return;
        }
        if (menuUI == null) {
            menuUI = new TeleporterMenuUI();
            entity.addComponent(menuUI);
            menuUI.create(); // manual create since entity already created
        }
        menuUI.refresh();
        menuUI.setVisible(true);
        menuVisible = true;
        Gdx.app.log("Teleporter", "Menu opened");
    }

    private void hideMenu() {
        if (menuUI != null) {
            menuUI.setVisible(false);
        }
        menuVisible = false;
        Gdx.app.log("Teleporter", "Menu closed");
    }
}
