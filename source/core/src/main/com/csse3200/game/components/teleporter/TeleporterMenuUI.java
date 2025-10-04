package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.UIComponent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * UI displayed when interacting with a Teleporter. Lists discovered rooms and allows travel.
 * Hidden by default. Only shows rooms recorded in DiscoveryService.
 */
public class TeleporterMenuUI extends UIComponent {
    private Window window;
    private Table listTable;

    @Override
    public void create() {
        super.create();
        buildUI();
        setVisible(false);
    }

    private void buildUI() {
        window = new Window("Teleporter", skin);
        window.pad(10f);
        window.setSize(300f, 400f);
        window.setMovable(true);

        listTable = new Table();
        ScrollPane scroll = new ScrollPane(listTable, skin);
        Table container = new Table();
        container.add(scroll).grow();
        window.add(container).grow();

        // Close button
        window.row();
        TextButton closeBtn = new TextButton("Close (Esc)", skin);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        window.add(closeBtn).padTop(8f).growX();

        // center of screen
        window.setPosition(Gdx.graphics.getWidth() / 2f - window.getWidth() / 2f,
                Gdx.graphics.getHeight() / 2f - window.getHeight() / 2f);
        stage.addActor(window);
    }

    /** Refresh the list of discovered rooms */
    public void refresh() {
        listTable.clearChildren();
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        GameArea current = ServiceLocator.getGameArea();
        if (ds == null) return;
        Set<String> discovered = ds.getDiscovered();

        List<String> ordered = new ArrayList<>(discovered);
        ordered.sort(String::compareTo);

        if (ordered.isEmpty()) {
            listTable.add("No destinations discovered yet").pad(4f);
            return;
        }

        for (String key : ordered) {
            String display = capitalize(key);
            TextButton btn = new TextButton(display, skin);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (current != null) {
                        boolean ok = current.transitionToArea(display);
                        Gdx.app.log("TeleporterMenu", ok ? "Travelling to " + display : "Failed to travel to " + display);
                        if (ok) setVisible(false);
                    }
                }
            });
            listTable.row();
            listTable.add(btn).growX().pad(3f);
        }
    }

    private String capitalize(String k) {
        if (k == null || k.isEmpty()) return k;
        // naive separators for known multi-words
        switch (k) {
            case "mainhall": return "MainHall";
            case "server": return "Server";
            case "storage": return "Storage";
            case "shipping": return "Shipping";
            case "research": return "Research";
            case "elevator": return "Elevator";
            case "office": return "Office";
            case "security": return "Security";
            case "reception": return "Reception";
            case "forest": return "Forest";
            case "tunnel": return "Tunnel";
            default: return Character.toUpperCase(k.charAt(0)) + k.substring(1);
        }
    }

    public void setVisible(boolean visible) {
        if (window != null) {
            window.setVisible(visible);
            window.setTouchable(visible ? Touchable.enabled : Touchable.disabled);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Intentionally empty: global stage draw handled by Renderer.
    }
}
