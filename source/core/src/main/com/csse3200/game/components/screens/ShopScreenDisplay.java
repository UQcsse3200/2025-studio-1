package com.csse3200.game.components.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.csse3200.game.components.shop.CatalogEntry;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopAssets;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.ui.*;


public class ShopScreenDisplay extends UIComponent {
    private final ForestGameArea game;
    private final CatalogService catalog;
    private final ShopAssets assets;
    private final ShopManager manager;

    public ShopScreenDisplay(ForestGameArea area, CatalogService catalog, ShopAssets assets, ShopManager manager) {
        this.game = area;
        this.catalog = catalog;
        this.assets = assets;
        this.manager = manager;
    }
    @Override
    public void create() {
        super.create();
        Table root = new Table();
        root.setFillParent(true);
        root.center();

        Label title = new Label("Shop", skin);
        title.setFontScale(2.0f);
        root.add(title).padBottom(20).row();

        Table grid = new Table();
        grid.defaults().pad(10);

        int columns = 4;
        int count = 0;
        for (CatalogEntry entry : catalog.list()) {
            // Use ShopAssets to get the actor (texture or fallback text)
            Actor actor = assets.getIconActor(entry.itemKey(), skin);

            // Make it clickable
            actor.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    manager.purchase(game.getPlayer(), entry.itemKey());
                }
            });

            grid.add(actor).size(120, 140);
            count++;
            if (count % columns == 0) grid.row();
        }

        root.add(grid).row();

        TextButton.TextButtonStyle style = skin.get("default", TextButton.TextButtonStyle.class);
        TextButton closeShopBtn = new TextButton("Close Shop", style);
        closeShopBtn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                entity.getEvents().trigger("closeShop");
            }
        });

        root.add(closeShopBtn).padTop(20).row();
        stage.addActor(root);

    }

    @Override
    public void draw(SpriteBatch batch) {
        // Stage draws everything
    }

}
