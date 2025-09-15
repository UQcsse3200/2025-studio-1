package com.csse3200.game.components;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.components.screens.ShopScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.components.shop.ShopManager;
import com.csse3200.game.components.shop.CatalogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShopComponent extends Component {
    private static final Logger logger = LoggerFactory.getLogger(ShopComponent.class);

    private final ForestGameArea area;
    private final ShopManager manager;

    private Entity uiEntity;
    private boolean open;

    public ShopComponent(ForestGameArea area, ShopManager manager) {
        this.area = area;
        this.manager = manager;
    }

    @Override
    public void create() {
        super.create();
        entity.getEvents().addListener("interact", this::open);
    }

    public void open() {
        if (open) {
            logger.debug("Shop already open");
            return;
        }
        logger.info("Opening shop UI");
        open = true;

        uiEntity = new Entity().addComponent(new ShopScreenDisplay(area, manager));
        area.spawnEntity(uiEntity);
        uiEntity.getEvents().addListener("closeShop", this::close);

        ShopScreenDisplay ui = uiEntity.getComponent(ShopScreenDisplay.class);
        Stage stage = (ui != null) ? ui.getStage() : null;
        if (stage != null) {
            uiEntity.addComponent(new InputDecorator(stage, 100));
        } else {
            logger.warn("ShopScreenDisplay/Stage not ready; InputDecorator not attached");
        }
    }

    public void close() {
        if (!open) return;
        logger.info("Closing shop UI");
        open = false;
        if (uiEntity != null) {
            uiEntity.dispose();
            uiEntity = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        close();
    }
}
