package com.csse3200.game.components.stations;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.benches.BenchConfig;
import com.csse3200.game.entities.configs.benches.ComputerBenchConfig;
import com.csse3200.game.entities.configs.benches.HealthBenchConfig;
import com.csse3200.game.entities.configs.benches.SpeedBenchConfig;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(GameExtension.class)
public class StationComponentTests {
    StationComponent stationComponent;
    Label buyPrompt;
    Entity weapon;
    InventoryComponent inventory;
    Entity player;

    StationComponent healthStationComponent;
    StationComponent speedStationComponent;

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
        player.addComponent(new CombatStatsComponent(100));
        player.addComponent(new PlayerActions());
        stationComponent.setPlayer(player);

        //Make weapon
        weapon = new Entity();
        weapon.addComponent(new WeaponsStatsComponent(10));

        //Assign player to the station, and weapon to the player
        player.getComponent(InventoryComponent.class).setCurrItem(weapon);
        ServiceLocator.registerPlayer(player);

        //Make the buyPrompt
        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        buyPrompt = new Label("", labelStyle);
        stationComponent.setBuyPrompt(buyPrompt);

        healthStationComponent = new StationComponent(new HealthBenchConfig());
        healthStationComponent.setPlayer(player);
        healthStationComponent.setPlayerNear(true);
        healthStationComponent.setBuyPrompt(buyPrompt);

        speedStationComponent = new StationComponent(new SpeedBenchConfig());
        speedStationComponent.setPlayer(player);
        speedStationComponent.setPlayerNear(true);
        speedStationComponent.setBuyPrompt(buyPrompt);
    }

    @Nested
    @DisplayName("Get and Set Tests")
    class getSetTests {
        @Test
        void shouldSetGetConfig() {
            stationComponent.setConfig(new SpeedBenchConfig());
            assertTrue(stationComponent.getConfig() instanceof SpeedBenchConfig);
        }

        @Test
        void shouldSetGetPlayer() {
            assertNotNull(stationComponent.getPlayer());
        }

        @Test
        void shouldSetGetPlayerNear() {
            stationComponent.setPlayerNear(false);
            assertFalse(stationComponent.isPlayerNear());
            stationComponent.setPlayerNear(true);
            assertTrue(stationComponent.isPlayerNear());
        }


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

        @Test
        void shouldNotCollideWithNotPlayer() {
            stationComponent.setPlayerNear(false);

            assertEquals("", stationComponent.getBuyPrompt().getText().toString());
            Fixture me = mock(Fixture.class);
            Fixture other = mock(Fixture.class);
            Body body = mock(Body.class);
            when(other.getBody()).thenReturn(body);
            when(body.getUserData()).thenReturn(player);
            BodyUserData userData = new BodyUserData();
            //Not a player
            userData.entity = new Entity();
            when(other.getBody().getUserData()).thenReturn(userData);
            stationComponent.onCollisionStart(me, other);
            assertFalse(stationComponent.isPlayerNear());
        }

        @Test
        void onCollisionEndWorksForPlayer() {
            //Make two mock fixtures and call uncollide and check if the label is gone
            Fixture me = mock(Fixture.class);
            Fixture other = mock(Fixture.class);
            Body body = mock(Body.class);
            when(other.getBody()).thenReturn(body);
            when(body.getUserData()).thenReturn(player);
            BodyUserData userData = new BodyUserData();
            userData.entity = player;
            player.addComponent(new PlayerActions());
            when(other.getBody().getUserData()).thenReturn(userData);
            stationComponent.onCollisionEnd(me, other);
            assertEquals("", stationComponent.getBuyPrompt().getText().toString());
            assertFalse(stationComponent.isPlayerNear());
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
            assertTrue(initialDamage < weapon.getComponent(WeaponsStatsComponent.class).getBaseAttack());
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

    @Nested
    @DisplayName("HealthUpgradeTests")
    class HealthTests {
        @Test
        void interactShouldIncreaseHealth() {
            int prevHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            healthStationComponent.upgrade();
            int currHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            assertEquals(prevHealth * 2, currHealth);
        }

        @Test
        void shouldNotUpgradeTwice() {
            healthStationComponent.upgrade();
            int prevHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            healthStationComponent.upgrade();
            int currHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            assertEquals(prevHealth, currHealth);
        }

        @Test
        void insufficientFundsShouldNotUpgrade() {
            player.getComponent(InventoryComponent.class).setProcessor(0);
            int prevHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            healthStationComponent.upgrade();
            int currHealth = player.getComponent(CombatStatsComponent.class).getHealth();
            assertEquals(prevHealth, currHealth);
        }
    }

    @Nested
    @DisplayName("SpeedUpgradeTests")
    class SpeedTests {
        @Test
        void interactShouldIncreaseSpeed() {
            Entity newPlayer = new Entity();
            InventoryComponent newInventory = new InventoryComponent(10000);
            newPlayer.addComponent(newInventory);
            newPlayer.addComponent(new PlayerActions());
            speedStationComponent.setPlayer(newPlayer);

            float prevSpeed = newPlayer.getComponent(PlayerActions.class).getMaxSpeed().cpy().len();
            float prevCrouchSpeed = newPlayer.getComponent(PlayerActions.class).getCrouchSpeed().cpy().len();
            float prevSprintSpeed = newPlayer.getComponent(PlayerActions.class).getSprintSpeed().cpy().len();

            speedStationComponent.upgrade();

            float currSpeed = newPlayer.getComponent(PlayerActions.class).getMaxSpeed().len();
            float currCrouchSpeed = newPlayer.getComponent(PlayerActions.class).getCrouchSpeed().len();
            float currSprintSpeed = newPlayer.getComponent(PlayerActions.class).getSprintSpeed().len();


            assertTrue(currSpeed > prevSpeed);
            assertTrue(currCrouchSpeed > prevCrouchSpeed);
            assertTrue(currSprintSpeed > prevSprintSpeed);
        }

        @Test
        void shouldNotUpgradeTwice() {
            Entity newPlayer = new Entity();

            speedStationComponent.upgrade();

            Vector2 prevSpeed = player.getComponent(PlayerActions.class).getMaxSpeed();
            Vector2 prevCrouchSpeed = player.getComponent(PlayerActions.class).getCrouchSpeed();
            Vector2 prevSprintSpeed = player.getComponent(PlayerActions.class).getSprintSpeed();

            speedStationComponent.upgrade();
            Vector2 currSpeed = player.getComponent(PlayerActions.class).getMaxSpeed();
            Vector2 currCrouchSpeed = player.getComponent(PlayerActions.class).getCrouchSpeed();
            Vector2 currSprintSpeed = player.getComponent(PlayerActions.class).getSprintSpeed();
            assertEquals(currSpeed, prevSpeed);
            assertEquals(currCrouchSpeed, prevCrouchSpeed);
            assertEquals(currSprintSpeed, prevSprintSpeed);
        }

        @Test
        void insufficientFundsShouldNotUpgrade() {
            player.getComponent(InventoryComponent.class).setProcessor(0);

            Vector2 prevSpeed = player.getComponent(PlayerActions.class).getMaxSpeed();
            Vector2 prevCrouchSpeed = player.getComponent(PlayerActions.class).getCrouchSpeed();
            Vector2 prevSprintSpeed = player.getComponent(PlayerActions.class).getSprintSpeed();

            speedStationComponent.upgrade();

            Vector2 currSpeed = player.getComponent(PlayerActions.class).getMaxSpeed();
            Vector2 currCrouchSpeed = player.getComponent(PlayerActions.class).getCrouchSpeed();
            Vector2 currSprintSpeed = player.getComponent(PlayerActions.class).getSprintSpeed();

            assertEquals(currSpeed, prevSpeed);
            assertEquals(currCrouchSpeed, prevCrouchSpeed);
            assertEquals(currSprintSpeed, prevSprintSpeed);
        }
    }

    @AfterEach
    void afterEach() {
        ServiceLocator.clear();
    }
}
