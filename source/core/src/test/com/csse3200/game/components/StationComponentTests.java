package com.csse3200.game.components;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.StringBuilder;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.extensions.GameExtension;
import com.csse3200.game.physics.*;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

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
        stationComponent = new StationComponent();
        stationComponent.playerNear = true;
        player = new Entity();
        inventory = mock(InventoryComponent.class);
        player.addComponent(inventory);
        stationComponent.player = player;

        //Make weapon
        weapon = new Entity();
        weapon.addComponent(new WeaponsStatsComponent(10));

        //Assign player to the station, and weapon to the player
        stationComponent.player = player;
        when(player.getComponent(InventoryComponent.class).getCurrItem()).thenReturn(weapon);


        //Make the buyPrompt
        BitmapFont font = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        buyPrompt = new Label("", labelStyle);
        stationComponent.buyPrompt = buyPrompt;
    }

    @Nested
    @DisplayName("StationCollisionTests")
    class collisionTests {
        @Test
        void collideShouldUpdateLabel() {
            //Make two mock fixtures and call collide and check if the label appears
            assertEquals("", stationComponent.buyPrompt.getText().toString());
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

            assertEquals("Press E for upgrade", stationComponent.buyPrompt.getText().toString());
        }
    }

    @Nested
    @DisplayName("StationUpgradeTests")
    class upgradeTests {

        @Test
        void upgradeShouldCallUpgradeOnItem() {
            //Test the upgrading calls upgrade and the text changes
            stationComponent.upgrade();

            assertEquals("Item has been upgraded", stationComponent.buyPrompt.getText().toString());
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
            when(player.getComponent(InventoryComponent.class).getCurrItem()).thenReturn(notWeapon);
            stationComponent.upgrade();
            assertEquals("This can't be upgraded", stationComponent.buyPrompt.getText().toString());
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
            assertEquals("Item is already fully upgraded!", stationComponent.buyPrompt.getText().toString());
        }
    }

}
