package com.csse3200.game.services;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.ArmourEquipComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Avatar;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Armour;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ArmourFactory;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.files.SaveGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;


/**
 * save load service that will extract all information about the current game state and will add it to save file to
 * be loaded.
 */
public class SaveLoadService {
    private static final Logger logger = LoggerFactory.getLogger(SaveLoadService.class);
    private String path;

    /**
     * Load a save file from local storage and rebuild the area and the current
     * players stats.
     */
    public static SaveGame.GameState load(String filePath) {
        SaveGame.GameState savedGame;
        if (SaveGame.loadGame(filePath) != null) {
            savedGame = SaveGame.loadGame(filePath);
            setAvatar(savedGame.getPlayer().avatar);
        } else {
            logger.error("failed to load in game");
            savedGame = null;
        }
        return savedGame;
    }

    /**
     * sets all the players stats based on the information from the save file json
     *
     * @param playerStats
     */
    public static void loadPlayer(SaveGame.information playerStats,
                                  ArrayList<String> savedArmour,
                                  ArrayList<SaveGame.itemInInven> inventory) {

        ServiceLocator.getPlayer().getComponent(
                CombatStatsComponent.class).setMaxHealth(playerStats.maxHealth);
        ServiceLocator.getPlayer().getComponent(
                CombatStatsComponent.class).setHealth(playerStats.currentHealth);

        ServiceLocator.getPlayer().getComponent(
                StaminaComponent.class).setStamina(playerStats.stamina);
        ServiceLocator.getPlayer().getComponent(
                InventoryComponent.class).setProcessor(playerStats.processor);
        ServiceLocator.getPlayer().getComponent(
                InventoryComponent.class).setKeycardLevel(playerStats.keyCardLevel);
        ServiceLocator.getPlayer().getComponent(
                AmmoStatsComponent.class).setAmmo(playerStats.ammoReserve);
        ServiceLocator.getPlayer().setPosition(playerStats.playerPos);

        if (!savedArmour.isEmpty()) getArmourfromsave(savedArmour);
        loadPlayerInventory(inventory);

    }

    private static void getArmourfromsave(ArrayList<String> savedArmour) {
        Entity newArmour;
        ArmourEquipComponent armourEquip =
                ServiceLocator.getPlayer().getComponent(ArmourEquipComponent.class);
        // sets the attachments
        for (Armour armour : Armour.values()) {
            if (savedArmour.contains(armour.getConfig().texturePath)) {

                newArmour = ArmourFactory.createArmour(armour);
                newArmour.create();
                armourEquip.setItem(newArmour);
                logger.info("armour found and equipped");
            }
        }
    }

    /**
     * loads plays items into their inventory
     *
     * @param inventory from the json file to be loaded
     */
    private static void loadPlayerInventory(ArrayList<SaveGame.itemInInven> inventory) {
        Entity itemEntity;
        InventoryComponent loadedInventory =
                ServiceLocator.getPlayer().getComponent(InventoryComponent.class);

        for (SaveGame.itemInInven item : inventory) {
            if (item == null) continue;
            itemEntity = null;

            switch (item.type) {
                case RANGED, MELEE -> itemEntity = getWeapon(item);
                case CONSUMABLE -> itemEntity = getConsumable(item.texture, item.count);
            }
            if (itemEntity != null) {
                itemEntity.getComponent(ItemComponent.class).setCount(item.count);
                loadedInventory.addItem(itemEntity);
            }
        }
    }

    /**
     * Save the current GameArea to local storage (saves/slotX.json).
     */
    public boolean save(String slot, GameArea gameArea) {
        Entity player = new Entity();
        if (ServiceLocator.getGameArea() != null) {
            player = ServiceLocator.getPlayer();
        } else {
            // if can't find through service locator will attempt hard check
            for (Entity entity : gameArea.getEntities()) {
                if (entity.hasComponent(InventoryComponent.class)) {
                    player = entity;
                }
            }
        }


        Set<String> discovered = ServiceLocator.getDiscoveryService().getDiscovered();
        SaveGame.GameState gamestate = new SaveGame.GameState();
        ArrayList<String> armours = new ArrayList<>();

        //will check that armour both exists and is not null
        if (player.hasComponent(ArmourEquipComponent.class) &&
                player.getComponent(ArmourEquipComponent.class).currentlyEquippedArmour != null
        ) {
            Set<Entity> armourSet = player.getComponent(ArmourEquipComponent.class).currentlyEquippedArmour.keySet();
            String armourString;
            for (Entity e : armourSet) {
                armourString = e.getComponent(ItemComponent.class).getTexture();
                armours.add(armourString);
            }
        }
        //sets game state
        gamestate.setPlayer(player);
        gamestate.setLoadedInventory(player.getComponent(InventoryComponent.class));
        gamestate.setArea(ServiceLocator.getGameArea().toString());
        gamestate.setWave(ServiceLocator.getGameArea().currentWave());
        gamestate.setAreasVisited(discovered);
        gamestate.setArmour(armours);
        gamestate.setDifficulty(ServiceLocator.getDifficulty().toString());

        path = "saves" + File.separator + slot + ".json";

        SaveGame.saveGame(gamestate, path);
        return true;
    }


    /**
     * helper method for turning items from savefile into entities
     *
     * @param item
     * @return Weapon
     */
    private static Entity getWeapon(SaveGame.itemInInven item) {
        Entity weaponEntity = null;
        boolean addLaser = false;
        boolean addBullet = false;
        // sets the attachments
        if (item.Attachments != null) {
            if (item.Attachments.contains("laser")) addLaser = true;
            if (item.Attachments.contains("bullet")) addBullet = true;
        }
        for (Weapons weapon : Weapons.values()) {
            if (item.texture.equals(weapon.getConfig().texturePath)) {
                weaponEntity = WeaponsFactory.createWeaponWithAttachment(weapon, addLaser, addBullet);
            }
        }
        if (weaponEntity == null) return null;
        if (item.type == ItemTypes.RANGED) {
            weaponEntity.getComponent(MagazineComponent.class).setCurrentAmmo(item.ammo);
        }

        // while loop to ensure upgrade matches with save
        while (weaponEntity.getComponent(WeaponsStatsComponent.class).getUpgradeStage() < item.upgradeStage) {
            weaponEntity.getComponent(WeaponsStatsComponent.class).upgrade();
        }
        return weaponEntity;
    }

    private static Entity getConsumable(String texture, int count) {
        Entity fromSave = null;
        for (Consumables consumable : Consumables.values()) {
            if (texture.equals(consumable.getConfig().texturePath)) {
                fromSave = ConsumableFactory.createConsumable(consumable);
                fromSave.getComponent(ItemComponent.class).setCount(count);
            }
        }
        return fromSave;
    }

    private static Avatar setAvatar(String texture) {
        for (Avatar avatar : AvatarRegistry.getAll()) {
            if (avatar.texturePath().equals(texture)) AvatarRegistry.set(avatar);
        }
        return null;
    }

}