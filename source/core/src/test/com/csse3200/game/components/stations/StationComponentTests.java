package com.csse3200.game.components.stations;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.configs.benches.ComputerBenchConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.*;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class StationComponentTests {
    StationComponent stationComponent;
    Label buyPrompt;
    Entity weapon;
    InventoryComponent inventory;
    Entity player;
    @BeforeEach
    void beforeEach() {
        //Register services
        RenderService renderService = new RenderService();
        renderService.setStage(mock(Stage.class));
        ServiceLocator.registerRenderService(renderService);
        ServiceLocator.registerPhysicsService(new PhysicsService());

        //Make station, player, inventory
        BenchConfig config = new ComputerBenchConfig();
        stationComponent = new StationComponent(config);
        stationComponent.setPlayerNear(true);
        player = new Entity();
        inventory = new InventoryComponent(10000);
        player.addComponent(inventory);
        stationComponent.setPlayer(player);

        //Make weapon
        weapon = new Entity();
        weapon.addComponent(new WeaponsStatsComponent(10));

        //Assign player to the station, and weapon to the player
        player.getComponent(InventoryComponent.class).setCurrItem(weapon);


        //Make the buyPrompt
        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        buyPrompt = new Label("", labelStyle);
        stationComponent.setBuyPrompt(buyPrompt);

    }

    @Nested
    @DisplayName("CurrencyTests")
    class CurrencyTests {
        @Test
        void notEnoughMoneyShouldNotUpgrade() {

            player.getComponent(InventoryComponent.class).addProcessor(-player.getComponent(InventoryComponent.class).getProcessor()); //Make the player broke
            stationComponent.upgrade();
            assertEquals("You are broke! Fries in the bag!", stationComponent.getBuyPrompt().getText().toString());

        }

        @Test
        void moneyGetsDecreasedWhenUpgrading() {
            int prevMoney = player.getComponent(InventoryComponent.class).getProcessor();
            stationComponent.upgrade();
            int currMoney = player.getComponent(InventoryComponent.class).getProcessor();
            assertEquals(prevMoney - stationComponent.getPrice(), currMoney);
        }

        @Test
        void moneyDoesntGoDownWhenFullyUpgraded() {
            int maxUpgrade = weapon.getComponent(WeaponsStatsComponent.class).getMaxUpgradeStage();
            for (int i = 1; i < maxUpgrade; i++) {
                stationComponent.upgrade();
            }

            int prevMoney = player.getComponent(InventoryComponent.class).getProcessor();
            stationComponent.upgrade();
            int currMoney = player.getComponent(InventoryComponent.class).getProcessor();
            assertEquals(currMoney, prevMoney);


        }

        @Test
        void moneyDoesntGoDownWhenNotWeapon() {
            Entity notWeapon = new Entity();
            player.getComponent(InventoryComponent.class).setCurrItem(notWeapon);
            int prevMoney = player.getComponent(InventoryComponent.class).getProcessor();
            stationComponent.upgrade();
            int currMoney = player.getComponent(InventoryComponent.class).getProcessor();
            assertEquals(currMoney, prevMoney);
        }

    }

    @Nested
    @DisplayName("StationCollisionTests")
    class CollisionTests {
        @Test
        void collideShouldUpdateLabel() {
            //Make two mock fixtures and call collide and check if the label appears
            assertEquals("", stationComponent.getBuyPrompt().getText().toString());
            Fixture me = mock(Fixture.class);
            Fixture other = mock(Fixture.class);
            Body body = mock(Body.class);
            when(other.getBody()).thenReturn(body);
            when(body.getUserData()).thenReturn(player);
            BodyUserData userData = new BodyUserData();
            userData.entity = player;
            player.addComponent(new PlayerActions());
            when(other.getBody().getUserData()).thenReturn(userData);
            stationComponent.onCollisionStart(me, other);

            assertEquals("Press E to upgrade weapon for " + stationComponent.getPrice(), stationComponent.getBuyPrompt().getText().toString());
        }
    }

    @Nested
    @DisplayName("StationUpgradeTests")
    class UpgradeTests {

        @Test
        void upgradeShouldCallUpgradeOnItem() {
            //Test the upgrading calls upgrade and the text changes
            stationComponent.upgrade();

            assertEquals("Item has been upgraded", stationComponent.getBuyPrompt().getText().toString());
        }

        @Test
        void upgradeIncreasesDamage() {
            //Check if the damage is increased when upgrading
            int initialDamage = weapon.getComponent(WeaponsStatsComponent.class).getBaseAttack();
            stationComponent.upgrade();
            assertTrue(initialDamage <  weapon.getComponent(WeaponsStatsComponent.class).getBaseAttack());
        }

        @Test
        void upgradeOnNotWeaponDoesNothing() {
            Entity notWeapon = new Entity();
            player.getComponent(InventoryComponent.class).setCurrItem(notWeapon);
            stationComponent.upgrade();
            assertEquals("Not a weapon!", stationComponent.getBuyPrompt().getText().toString());
        }

        @Test
        void upgradeOnMaxUpgradeDoesNothing() {
            int maxUpgrade = weapon.getComponent(WeaponsStatsComponent.class).getMaxUpgradeStage();
            for (int i = 1; i < maxUpgrade; i++) {
                stationComponent.upgrade();
            }
            int prevDamage = weapon.getComponent(WeaponsStatsComponent.class).getBaseAttack();
            stationComponent.upgrade();
            int currDamage = weapon.getComponent(WeaponsStatsComponent.class).getBaseAttack();
            assertEquals(currDamage, prevDamage);
            assertEquals("Weapon is fully upgraded already!", stationComponent.getBuyPrompt().getText().toString());
        }
    }

}
