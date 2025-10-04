package com.csse3200.game.files;


import com.badlogic.gdx.utils.Array;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * class for saving all necassary components of a game so
 * that it can be retrieved by the save load service
 */
public class SaveGame {
    private static final Logger logger = LoggerFactory.getLogger(SaveGame.class);

    public static GameState loadGame(String fileName) {
        return FileLoader.readClass(GameState.class, fileName);
    }

    public static void saveGame(GameState gameState,String fileName) {
        FileLoader.writeClass(gameState, fileName, FileLoader.Location.LOCAL);
    }



    public static class GameState {
        private Entity player;
        private InventoryComponent inventory;
        private GameArea savedArea;



        public void setInventory(InventoryComponent inventory) {
            for (Entity items: inventory.getInventory()){
//                if (items.getId())

                this.inventory = inventory;
        }
        }

        public void setPlayer(Entity player) {
            this.player = player;
        }

//        public void saveEntities(List<Entity> entities) {
//            for (Entity entity : entities) {
//                if (entity != null) {
//                    logger.info("Saving entity: {}", entity);
//                    this.entities.add(entity);
//                }
//            }
//        }

        public void setArea(GameArea area) {
            this.savedArea = area;
        }

        private void getPlayerInventory() {
            this.inventory.get(0).hasComponent(ConsumableComponent.class);
            if (this.inventory.get(0).hasComponent(WeaponsStatsComponent.class)) {
                WeaponsStatsComponent weapon = this.inventory.get(0).getComponent(WeaponsStatsComponent.class);
                weapon.getUpgradeStage();
            }
        }


    }




}
