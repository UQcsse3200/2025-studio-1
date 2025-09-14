package com.csse3200.game.components.enemy;

import com.badlogic.gdx.audio.Sound;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EnemyDeathRewardComponentTest {

    @Mock
    ResourceService resourceService;
    @Mock
    Sound mockSound;


    @Test
    @DisplayName("Awards processor to player when enemy dies")
    void awardsProcessorOnDeath() {

        ServiceLocator.registerResourceService(resourceService);
        lenient().when(resourceService.getAsset("sounds/ammo_replenished.mp3", Sound.class))
                .thenReturn(mockSound);
        int startingProcessor = 10;
        int reward = 15;

        Entity player = new Entity().addComponent(new InventoryComponent(startingProcessor))
                        .addComponent(new AmmoStatsComponent(12));
        player.create();
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(5))
                .addComponent(new EnemyDeathRewardComponent(reward, inventory));
        enemy.create();

        // Kill enemy
        enemy.getComponent(CombatStatsComponent.class).setHealth(0);

        assertEquals(startingProcessor + reward, inventory.getProcessor(), "Player should receive reward processor on enemy death");
    }

    @Test
    @DisplayName("Reward only applied once even if setHealth(0) called repeatedly")
    void rewardOnlyOnce() {

        ServiceLocator.registerResourceService(resourceService);
        lenient().when(resourceService.getAsset("sounds/ammo_replenished.mp3", Sound.class))
                .thenReturn(mockSound);
        int startingProcessor = 5;
        int reward = 20;

        Entity player = new Entity().addComponent(new InventoryComponent(startingProcessor))
                .addComponent(new AmmoStatsComponent(12));
        player.create();
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(3))
                .addComponent(new EnemyDeathRewardComponent(reward, inventory));
        enemy.create();

        CombatStatsComponent stats = enemy.getComponent(CombatStatsComponent.class);
        stats.setHealth(0); // first death triggers reward
        stats.setHealth(0); // should not trigger again

        assertEquals(startingProcessor + reward, inventory.getProcessor(), "Player should only be rewarded once");
    }

    @Test
    @DisplayName("Awards processor to player when enemy dies")
    void awardsAmmoOnDeath() {

        ServiceLocator.registerResourceService(resourceService);
        when(resourceService.getAsset("sounds/ammo_replenished.mp3", Sound.class))
                .thenReturn(mockSound);
        int startingAmmo = 50;
        int reward = 30;

        Entity player = new Entity().addComponent(new InventoryComponent(startingAmmo))
                .addComponent(new AmmoStatsComponent(startingAmmo));

        AmmoStatsComponent ammo = player.getComponent(AmmoStatsComponent.class);
        player.create();
        InventoryComponent inventory = player.getComponent(InventoryComponent.class);

        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(5))
                .addComponent(new EnemyDeathRewardComponent(reward, inventory));
        enemy.create();

        // Guarantee reload
        enemy.getComponent(EnemyDeathRewardComponent.class).rewardGuaranteedReload();

        assertEquals(startingAmmo + reward, ammo.getAmmo(), "Player should receive reward processor on enemy death");
    }

    @Test
    @DisplayName("Handles null player inventory without crashing")
    void handlesNullInventory() {
        Entity enemy = new Entity()
                .addComponent(new CombatStatsComponent(4))
                .addComponent(new EnemyDeathRewardComponent(50, null));
        enemy.create();

        // Should not throw when enemy dies
        assertDoesNotThrow(() -> enemy.getComponent(CombatStatsComponent.class).setHealth(0));
    }
}
