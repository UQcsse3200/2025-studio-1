package com.csse3200.game.components.teleporter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.csse3200.game.services.DiscoveryService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.NeonStyles;
import com.csse3200.game.ui.UIComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Teleporter menu styled to match Pause/Main menu theme (neon buttons, dimmed background).
 * Hidden by default and shown when player activates a teleporter.
 */
public class TeleporterMenuUI extends UIComponent {
    private Table panel;              // main panel (centered)
    private Table listTable;          // destination buttons container
    private ScrollPane scrollPane;    // scroll around listTable
    private Image dimmer;             // full-screen translucent background
    private NeonStyles neon;          // button styling helper
    private boolean visible;

    @Override
    public void create() {
        super.create();
        buildUI();
        setVisible(false);
    }

    private void buildUI() {
        neon = new NeonStyles(0.70f);

        // Dim background similar to PauseMenuDisplay
        dimmer = new Image(skin.newDrawable("white", new Color(0, 0, 0, 0.6f)));
        dimmer.setFillParent(true);
        dimmer.setTouchable(Touchable.disabled); // allow clicks through when hidden
        stage.addActor(dimmer);

        // Root panel
        panel = new Table();
        panel.setVisible(true);
        panel.center();
        panel.defaults().pad(6f);

        // Title using same style key as other menus
        Label.LabelStyle titleStyle = new Label.LabelStyle(skin.get("title", Label.LabelStyle.class));
        Label title = new Label("Teleporter", titleStyle);
        title.setFontScale(1.6f);
        panel.add(title).colspan(2).padBottom(12f).center();
        panel.row();

        // Scroll list
        listTable = new Table();
        listTable.top();
        scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        panel.add(scrollPane).grow().minWidth(320f).minHeight(280f).padBottom(10f);
        panel.row();

        // Buttons row (Close only for now)
        TextButton closeBtn = new TextButton("Close (Esc)", neon.buttonRounded());
        closeBtn.getLabel().setFontScale(1.2f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        panel.add(closeBtn).growX();

        // Panel background (subtle translucent panel) - using a skin drawable if exists else a solid
        // Attempt to reuse any provided drawable named "panel"; fallback to white tinted
        Drawable bg;
        try {
            bg = skin.getDrawable("panel");
        } catch (Exception e) {
            bg = skin.newDrawable("white", new Color(0f, 0.18f, 0.28f, 0.85f));
        }
        panel.setBackground(bg);

        // Size & position center screen
        panel.setSize(380f, 420f);
        panel.setPosition(
                (Gdx.graphics.getWidth() - panel.getWidth()) / 2f,
                (Gdx.graphics.getHeight() - panel.getHeight()) / 2f
        );
        stage.addActor(panel);

        // Ensure correct draw order (panel above dimmer)
        dimmer.toBack();
        panel.toFront();
    }

    /** Refresh list of discovered destinations, building neon-styled buttons. */
    public void refresh() {
        if (listTable == null) return;
        listTable.clearChildren();
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        if (ds == null) {
            listTable.add("Discovery unavailable").pad(4f);
            return;
        }
        Set<String> discovered = ds.getDiscovered();
        List<String> ordered = new ArrayList<>(discovered);
        ordered.sort(String::compareTo);

        if (ordered.isEmpty()) {
            listTable.add("No destinations discovered yet").pad(4f);
            return;
        }

        for (String key : ordered) {
            final String display = capitalize(key);
            TextButton btn = new TextButton(display, neon.buttonRounded());
            btn.getLabel().setFontScale(1.1f);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    TeleporterComponent teleporter = entity.getComponent(TeleporterComponent.class);
                    if (teleporter != null) {
                        teleporter.startTeleport(display);
                    } else {
                        Gdx.app.error("TeleporterMenuUI", "TeleporterComponent missing on entity");
                    }
                }
            });
            listTable.row();
            listTable.add(btn).growX().pad(3f);
        }
    }

    private String capitalize(String k) {
        if (k == null || k.isEmpty()) return k;
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

    public void setVisible(boolean makeVisible) {
        visible = makeVisible;
        if (panel != null) {
            panel.setVisible(makeVisible);
            panel.setTouchable(makeVisible ? Touchable.enabled : Touchable.disabled);
        }
        if (dimmer != null) dimmer.setVisible(makeVisible);
    }

    public boolean isVisible() {
        return visible;
    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage handles rendering. Nothing custom.
    }

    @Override
    public void dispose() {
        if (panel != null) panel.remove();
        if (dimmer != null) dimmer.remove();
        if (neon != null) neon.dispose();
        super.dispose();
    }
}
